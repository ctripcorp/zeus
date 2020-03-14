package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.executor.TaskManager;
import com.ctrip.zeus.executor.impl.ResultHandler;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.model.status.GroupServerStatus;
import com.ctrip.zeus.model.status.GroupStatus;
import com.ctrip.zeus.model.task.OpsTask;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.auth.AuthService;
import com.ctrip.zeus.service.auth.ResourceDataType;
import com.ctrip.zeus.service.auth.ResourceOperationType;
import com.ctrip.zeus.service.message.queue.MessageQueue;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.filter.FilterSet;
import com.ctrip.zeus.service.query.filter.QueryExecuter;
import com.ctrip.zeus.service.status.GroupStatusService;
import com.ctrip.zeus.service.task.constant.TaskOpsType;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.ctrip.zeus.tag.PropertyBox;
import com.ctrip.zeus.util.MessageUtil;
import com.ctrip.zeus.util.UserUtils;
import com.google.common.base.Joiner;
import com.netflix.config.DynamicLongProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * Created by zhoumy on 2015/8/6.
 */
@Component
@Path("/")
public class GroupMemberResource {
    @Autowired
    private GroupRepository groupRepository;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private EntityFactory entityFactory;
    @Resource
    private TaskManager taskManager;
    @Resource
    private DbLockFactory dbLockFactory;
    @Resource
    private AuthService authService;
    @Resource
    private PropertyBox propertyBox;
    @Resource
    private GroupStatusService groupStatusService;
    @Resource
    private MessageQueue messageQueue;

    private static DynamicLongProperty apiTimeout = DynamicPropertyFactory.getInstance().getLongProperty("api.timeout", 30000L);
    private static final int TIMEOUT = 1000;

    /**
     * @api {post} /api/members: [Read] Get group servers
     * @apiDescription Get group servers by group id ,ip,mode
     * @apiName Members
     * @apiGroup Member
     * @apiSuccess (Success 200) {String} message   success message
     * @apiParam (Parameter) {Integer} [groupId]              Group id for query
     * @apiParam (Parameter) {String} [ip]           Ip for query
     * @apiParam {string=online,offline,redundant(online&offline)} [mode]   query snapshot versions by mode
     */
    @GET
    @Path("/members")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response list(@Context HttpHeaders hh,
                         @Context final HttpServletRequest request,
                         @QueryParam("groupId") final Long groupId,
                         @QueryParam("ip") final String ip,
                         @QueryParam("mode") String mode) throws Exception {
        if (groupId == null) {
            throw new ValidationException("Query parameter groupId is required.");
        }
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.READ, ResourceDataType.Group, groupId);
        final SelectionMode selectionMode = SelectionMode.getMode(mode);
        if (selectionMode == SelectionMode.REDUNDANT) {
            throw new ValidationException("Redundant mode is not supported.");
        }
        IdVersion[] groupFilter = new QueryExecuter.Builder<IdVersion>()
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return ip != null;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        return groupCriteriaQuery.queryByGroupServer(ip);
                    }
                }).build(IdVersion.class).run(new ResultHandler<IdVersion, IdVersion>() {
                    @Override
                    public IdVersion[] handle(Set<IdVersion> result) throws Exception {
                        if (result == null) return groupCriteriaQuery.queryByIdAndMode(groupId, selectionMode);
                        Set<IdVersion> filteredResult = new HashSet<>();
                        for (IdVersion key : groupCriteriaQuery.queryByIdAndMode(groupId, selectionMode)) {
                            if (result.contains(key)) {
                                filteredResult.add(key);
                            }
                        }
                        return filteredResult.toArray(new IdVersion[filteredResult.size()]);
                    }
                });
        GroupServerList groupServerList = new GroupServerList().setGroupId(groupId);
        if (groupFilter.length == 0) {
            return responseHandler.handle(groupServerList, hh.getMediaType());
        }
        List<Group> groups = groupRepository.list(groupFilter);
        if (groups.size() > 0) {
            groupServerList.setVersion(groups.get(0).getVersion());
            for (GroupServer groupServer : groups.get(0).getGroupServers()) {
                groupServerList.addGroupServer(groupServer);
            }
        }
        groupServerList.setTotal(groupServerList.getGroupServers().size());
        return responseHandler.handle(groupServerList, hh.getMediaType());
    }

    /**
     * @api {post} /api/group/addMember: [Write] Add group server
     * @apiDescription See [Update group server](#api-Member-UpdateMember) for object description
     * @apiName AddMember
     * @apiGroup Member
     * @apiSuccess (Success 200) {String} message   success message
     * @apiParam (Parameter) {boolean} [online=false]               add group servers to its offline (and online) version
     * @apiParam (RequestEntity) {long} group-id                    the group to be modified
     * @apiParam (RequestEntity) {GroupServer[]} group-servers      group servers to be added
     * @apiParam (GroupServer) {Integer} port               server port
     * @apiParam (GroupServer) {String} ip                  server ip
     * @apiParam (GroupServer) {String} host-name server    host name
     * @apiParam (GroupServer) {Integer} [weight]           proxying weight
     * @apiParam (GroupServer) {Integer} [max-fails=0]      exclude server from proxying if max_fails count exceeds the latch for fails_timeout interval, disabled if values 0
     * @apiParam (GroupServer) {Integer} [fails-timeout=0]  disabled by default
     */
    @POST
    @Path("/group/addMember")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addMember(@Context HttpHeaders hh,
                              @Context HttpServletRequest request,
                              @QueryParam("online") Boolean online,
                              String groupServerList) throws Exception {

        if (online == null) online = false;
        GroupServerList gsl = parseGroupServer(hh.getMediaType(), groupServerList);
        if (gsl.getGroupId() == null)
            throw new ValidationException("Group id is required.");

        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Group, gsl.getGroupId());

        Group group = groupRepository.getById(gsl.getGroupId());
        if (group == null)
            throw new ValidationException("Group with id " + gsl.getGroupId() + " does not exist.");
        for (GroupServer groupServer : gsl.getGroupServers()) {
            group.getGroupServers().add(groupServer);
        }
        if (online) {
            onlineGroupUpdate(group);
        } else {
            DistLock lock = dbLockFactory.newLock(group.getId() + "_updateGroup");
            lock.lock(TIMEOUT);
            try {
                groupRepository.update(group, true);
                try {
                    if (groupCriteriaQuery.queryByIdAndMode(group.getId(), SelectionMode.ONLINE_EXCLUSIVE).length == 1) {
                        propertyBox.set("status", "toBeActivated", "group", group.getId());
                    }
                } catch (Exception ex) {
                }
                addHealthProperty(group.getId());
            } finally {
                lock.unlock();
            }
        }

        String[] ips = new String[gsl.getGroupServers().size()];
        for (int i = 0; i < ips.length; i++) {
            ips[i] = gsl.getGroupServers().get(i).getIp();
        }

        String slbMessageData = MessageUtil.getMessageData(request, new Group[]{group}, null, null, null, ips, true);
        messageQueue.produceMessage(request.getRequestURI(), group.getId(), slbMessageData);

        return responseHandler.handle("Successfully added group servers to group with id " + gsl.getGroupId() + ".", hh.getMediaType());
    }

    /**
     * @api {post} /api/group/updateMember: [Write] Update group server
     * @apiDescription UpdateMember only incrementally affects group servers in the list, other members would remain unchanged.
     * @apiName UpdateMember
     * @apiGroup Member
     * @apiSuccess (Success 200) {String} message   success message
     * @apiParam (Parameter) {boolean} [online=false]               add group servers to its offline (and online) version
     * @apiParam (RequestEntity) {long} group-id                    the group to be modified
     * @apiParam (RequestEntity) {GroupServer[]} group-servers      group servers to be modified
     * @apiParam (GroupServer) {Integer} port               server port
     * @apiParam (GroupServer) {String} ip                  server ip
     * @apiParam (GroupServer) {String} host-name server    host name
     * @apiParam (GroupServer) {Integer} [weight]           proxying weight
     * @apiParam (GroupServer) {Integer} [max-fails=0]      exclude server from proxying if max_fails count exceeds the latch for fails_timeout interval, disabled if values 0
     * @apiParam (GroupServer) {Integer} [fails-timeout=0]  disabled by default
     * @apiParamExample {json} Sample Request:
     *  {
     *    "group-id" : 1,
     *    "group-servers" : [ {
     *      "port" : 8080,
     *      "ip" : "127.0.0.1",
     *      "host-name" : "PC1",
     *      "weight" : 5,
     *      "max-fails" : 0,
     *      "fail-timeout" : 0
     *    }, {
     *      "port" : 8080,
     *      "ip" : "127.0.0.1",
     *      "host-name" : "PC2",
     *      "weight" : 5,
     *      "max-fails" : 0,
     *      "fail-timeout" : 0
     *    } ]
     *  }
     */
    @POST
    @Path("/group/updateMember")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateMember(@Context HttpHeaders hh,
                                 @Context HttpServletRequest request,
                                 @QueryParam("online") Boolean online,
                                 String groupServerList) throws Exception {
        if (online == null) online = false;
        GroupServerList gsl = parseGroupServer(hh.getMediaType(), groupServerList);
        if (gsl.getGroupId() == null)
            throw new ValidationException("Group id is required.");
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Group, gsl.getGroupId());


        Group group = groupRepository.getById(gsl.getGroupId());
        if (group == null)
            throw new ValidationException("Group with id " + gsl.getGroupId() + " does not exist.");
        Map<String, GroupServer> groupServers = new HashMap<>();
        for (GroupServer gs : group.getGroupServers()) {
            groupServers.put(gs.getIp() + ":" + gs.getPort(), gs);
        }
        // replace old values with new ones
        for (GroupServer gs : gsl.getGroupServers()) {
            String key = gs.getIp() + ":" + gs.getPort();
            if (groupServers.containsKey(key))
                groupServers.put(key, gs);
        }
        group.getGroupServers().clear();
        for (GroupServer gs : groupServers.values()) {
            group.getGroupServers().add(gs);
        }

        if (online) {
            onlineGroupUpdate(group);
        } else {
            DistLock lock = dbLockFactory.newLock(group.getId() + "_updateGroup");
            lock.lock(TIMEOUT);
            try {
                groupRepository.update(group, true);
                try {
                    if (groupCriteriaQuery.queryByIdAndMode(group.getId(), SelectionMode.ONLINE_EXCLUSIVE).length == 1) {
                        propertyBox.set("status", "toBeActivated", "group", group.getId());
                    }
                } catch (Exception ex) {
                }
                addHealthProperty(group.getId());
            } finally {
                lock.unlock();
            }
        }

        String[] ips = new String[gsl.getGroupServers().size()];
        for (int i = 0; i < ips.length; i++) {
            ips[i] = gsl.getGroupServers().get(i).getIp();
        }
        String slbMessageData = MessageUtil.getMessageData(request, new Group[]{group}, null, null, null, ips, true);
        messageQueue.produceMessage(request.getRequestURI(), group.getId(), slbMessageData);

        return responseHandler.handle("Successfully updated group servers to group with id " + gsl.getGroupId() + ".", hh.getMediaType());
    }

    /**
     * @api {get} /api/group/removeMember: [Write] Remove group server
     * @apiName RemoveMember
     * @apiGroup Member
     * @apiSuccess (Success 200) {String} message   success message
     * @apiParam {boolean} [online=false]   add group servers to its offline (and online) version
     * @apiParam {long} groupId             the group to be modified
     * @apiParam {string[]} ip              group servers to be removed
     */
    @GET
    @Path("/group/removeMember")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response removeMember(@Context HttpHeaders hh, @Context HttpServletRequest request,
                                 @QueryParam("groupId") Long groupId,
                                 @QueryParam("ip") List<String> ips,
                                 @QueryParam("online") Boolean online) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Group, groupId);

        if (online == null) online = false;
        if (groupId == null) {
            throw new ValidationException("Group id parameter is required.");
        }
        Group group = groupRepository.getById(groupId);
        if (group == null) {
            throw new ValidationException("Group with id " + groupId + " does not exist.");
        }
        Map<String, GroupServer> groupServers = new HashMap<>();
        for (GroupServer gs : group.getGroupServers()) {
            groupServers.put(gs.getIp(), gs);
        }
        for (String ip : ips) {
            groupServers.remove(ip);
        }
        group.getGroupServers().clear();
        for (GroupServer gs : groupServers.values()) {
            group.getGroupServers().add(gs);
        }

        if (online) {
            onlineGroupUpdate(group);
        } else {
            DistLock lock = dbLockFactory.newLock(group.getId() + "_updateGroup");
            lock.lock(TIMEOUT);
            try {
                groupRepository.update(group, true);
                try {
                    if (groupCriteriaQuery.queryByIdAndMode(group.getId(), SelectionMode.ONLINE_EXCLUSIVE).length == 1) {
                        propertyBox.set("status", "toBeActivated", "group", group.getId());
                    }
                } catch (Exception ex) {
                }
                addHealthProperty(groupId);
            } finally {
                lock.unlock();
            }
        }
        String slbMessageData = MessageUtil.getMessageData(request, new Group[]{group}, null, null, null, ips.toArray(new String[ips.size()]), true);
        messageQueue.produceMessage(request.getRequestURI(), group.getId(), slbMessageData);

        return responseHandler.handle("Successfully removed " + Joiner.on(",").join(ips) + " from group with id " + groupId + ".", hh.getMediaType());
    }

    private GroupServerList parseGroupServer(MediaType mediaType, String groupServerList) throws Exception {
        GroupServerList gsl;
        if (MediaType.APPLICATION_XML_TYPE.equals(mediaType)) {
            gsl = ObjectJsonParser.parse(groupServerList, GroupServerList.class);
        } else {
            try {
                gsl = ObjectJsonParser.parse(groupServerList, GroupServerList.class);
            } catch (Exception e) {
                throw new Exception("Group member list cannot be parsed.");
            }
        }
        return gsl;
    }

    //case 1: group only has offline version , throw ValidationException.
    //case 2: group has same online/offline version, update offline and activate group.
    //case 3: group has different online/offline versions, throw ValidationException.
    private void onlineGroupUpdate(Group group) throws Exception {
        Long groupId = group.getId();
        Group offGroup = null;
        Group onGroup = null;
        DistLock lock = dbLockFactory.newLock(groupId + "_updateGroup");
        lock.lock(TIMEOUT);
        try {
            ModelStatusMapping<Group> groupMap = entityFactory.getGroupsByIds(new Long[]{groupId});
            if (groupMap.getOnlineMapping().get(groupId) == null) {
                throw new ValidationException("Group only has offline version . GroupId:" + groupId);
            }
            if (groupMap.getOnlineMapping().get(groupId).getVersion().equals(groupMap.getOfflineMapping().get(groupId).getVersion())) {
                groupRepository.update(group, true);
                ModelStatusMapping<Group> mapping = entityFactory.getGroupsByIds(new Long[]{groupId});
                offGroup = mapping.getOfflineMapping().get(groupId);
                onGroup = mapping.getOnlineMapping().get(groupId);
            } else {
                throw new ValidationException("Online/Offline group version is different. Please activate group first.GroupId:"
                        + groupId + ";OnlineVersion:" + groupMap.getOnlineMapping().get(groupId).getVersion()
                        + ";OfflineVersion:" + groupMap.getOfflineMapping().get(groupId).getVersion());
            }
        } finally {
            lock.unlock();
        }

        if (offGroup != null && onGroup != null) {
            Set<Long> vsIds = new HashSet<>();
            for (GroupVirtualServer gvs : offGroup.getGroupVirtualServers()) {
                vsIds.add(gvs.getVirtualServer().getId());
            }
            ModelStatusMapping<VirtualServer> vsMaping = entityFactory.getVsesByIds(vsIds.toArray(new Long[]{}));
            if (vsMaping.getOnlineMapping().size() == 0) {
                throw new ValidationException("Related vs is not activated.VsIds: " + vsIds);
            }
            List<OpsTask> tasks = new ArrayList<>();
            for (VirtualServer vs : vsMaping.getOnlineMapping().values()) {
                for (Long slbId : vs.getSlbIds()) {
                    OpsTask task = new OpsTask();
                    task.setCreateTime(new Date())
                            .setGroupId(groupId)
                            .setTargetSlbId(slbId)
                            .setOpsType(TaskOpsType.ACTIVATE_GROUP)
                            .setSkipValidate(true)
                            .setVersion(offGroup.getVersion());
                    tasks.add(task);
                }
            }
            List<Long> taskIds = taskManager.addTask(tasks);
            taskManager.getResult(taskIds, apiTimeout.get());
        }
    }

    private void addHealthProperty(Long groupId) throws Exception {
        GroupStatus gs = groupStatusService.getOfflineGroupStatus(groupId);
        boolean health = true;
        boolean unhealth = true;
        for (GroupServerStatus gss : gs.getGroupServerStatuses()) {
            if (gss.getServer() && gss.getHealthy() && gss.getPull() && gss.getMember()) {
                unhealth = false;
            } else {
                health = false;
            }
        }
        if (health) {
            propertyBox.set("healthy", "healthy", "group", gs.getGroupId());
        } else if (unhealth) {
            propertyBox.set("healthy", "broken", "group", gs.getGroupId());
        } else {
            propertyBox.set("healthy", "unhealthy", "group", gs.getGroupId());
        }

    }
}
