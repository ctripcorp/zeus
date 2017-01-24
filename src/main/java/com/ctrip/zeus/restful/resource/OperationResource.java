package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.executor.TaskManager;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.message.queue.MessageQueue;
import com.ctrip.zeus.service.message.queue.MessageType;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.service.status.GroupStatusService;
import com.ctrip.zeus.service.status.StatusOffset;
import com.ctrip.zeus.service.status.StatusService;
import com.ctrip.zeus.service.task.constant.TaskOpsType;
import com.ctrip.zeus.status.entity.GroupServerStatus;
import com.ctrip.zeus.status.entity.GroupStatus;
import com.ctrip.zeus.status.entity.ServerStatus;
import com.ctrip.zeus.tag.PropertyBox;
import com.ctrip.zeus.task.entity.OpsTask;
import com.ctrip.zeus.task.entity.TaskResult;
import com.ctrip.zeus.util.MessageUtil;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicLongProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
@Component
@Path("/op")
public class OperationResource {

    @Resource
    StatusService statusService;
    @Resource
    private GroupStatusService groupStatusService;
    @Resource
    private GroupRepository groupRepository;
    @Resource
    private TaskManager taskManager;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private EntityFactory entityFactory;
    @Resource
    private ConfigHandler configHandler;
    @Resource
    private PropertyBox propertyBox;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private MessageQueue messageQueue;


    private static DynamicLongProperty apiTimeout = DynamicPropertyFactory.getInstance().getLongProperty("api.timeout", 15000L);
    private static DynamicBooleanProperty healthyOpsActivate = DynamicPropertyFactory.getInstance().getBooleanProperty("healthy.operation.active", false);

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * @api {get} /api/op/upServer: [OPS] Mark up a server
     * @apiDescription Mark up server action will take effect on a physical server. It will set `server` status to 'up' to all nesting SLB groups.
     * @apiName MarkUpServer
     * @apiGroup Operation
     * @apiParam {String} ip            server ip address
     * @apiSuccess {ServerStatus}       server status
     */
    @GET
    @Path("/upServer")
    @Authorize(name = "upDownServer")
    public Response upServer(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("ip") String ip) throws Exception {
        return serverOps(request, hh, ip, true);
    }

    /**
     * @api {get} /api/op/downServer: [OPS] Mark down a server
     * @apiDescription Mark down server action will take effect on a physical server. It will set `server` status to 'down' to all nesting SLB groups.
     * @apiName MarkDownServer
     * @apiGroup Operation
     * @apiParam {String} ip            server ip address
     * @apiSuccess {ServerStatus}       server status
     */
    @GET
    @Path("/downServer")
    @Authorize(name = "upDownServer")
    public Response downServer(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("ip") String ip) throws Exception {
        return serverOps(request, hh, ip, false);
    }

    private Response serverOps(HttpServletRequest request, HttpHeaders hh, String serverip, boolean up) throws Exception {
        Long[] groupIds = entityFactory.getGroupIdsByGroupServerIp(serverip, SelectionMode.REDUNDANT);

        if (groupIds == null || groupIds.length == 0) {
            throw new ValidationException("Not found Server Ip.");
        }
        ModelStatusMapping<Group> groupMap = entityFactory.getGroupsByIds(groupIds);
        Set<Long> vsIds = new HashSet<>();
        for (Long id : groupIds) {
            if (groupMap.getOnlineMapping().get(id) != null) {
                Group group = groupMap.getOnlineMapping().get(id);
                for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
                    vsIds.add(gvs.getVirtualServer().getId());
                }
            } else if (groupMap.getOfflineMapping().get(id) != null) {
                Group group = groupMap.getOfflineMapping().get(id);
                for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
                    vsIds.add(gvs.getVirtualServer().getId());
                }
            }
        }
        ModelStatusMapping<VirtualServer> vsMap = entityFactory.getVsesByIds(vsIds.toArray(new Long[]{}));
        Set<Long> slbIds = new HashSet<>();
        for (VirtualServer vs : vsMap.getOnlineMapping().values()) {
            slbIds.addAll(vs.getSlbIds());
        }
        for (VirtualServer vs : vsMap.getOfflineMapping().values()) {
            slbIds.addAll(vs.getSlbIds());
        }

        List<OpsTask> tasks = new ArrayList<>();
        for (Long slbId : slbIds) {
            OpsTask task = new OpsTask();
            task.setIpList(serverip);
            task.setOpsType(TaskOpsType.SERVER_OPS);
            task.setTargetSlbId(slbId);
            task.setUp(up);
            tasks.add(task);
        }
        List<Long> taskIds = taskManager.addTask(tasks);
        List<TaskResult> results = taskManager.getResult(taskIds, apiTimeout.get());
        boolean isSuccess = true;
        String failCause = "";
        for (TaskResult taskResult : results) {
            if (!taskResult.isSuccess()) {
                isSuccess = false;
                failCause += taskResult.toString();
            }
        }
        if (!isSuccess) {
            throw new Exception(failCause);
        }
        ServerStatus ss = new ServerStatus().setIp(serverip).setUp(statusService.getServerStatus(serverip));

        Long[] gids = entityFactory.getGroupIdsByGroupServerIp(serverip, SelectionMode.ONLINE_FIRST);

        List<Group> groups = groupRepository.list(gids);

        if (groups != null) {
            for (Group group : groups) {
                ss.addGroupName(group.getName());
            }
        }
        String slbMessageData = MessageUtil.getMessageData(request, groups.toArray(new Group[]{}), null, null, new String[]{serverip}, true);
        if (configHandler.getEnable("use.new,message.queue.producer", false)) {
            messageQueue.produceMessage(request.getRequestURI(), null, slbMessageData);
        } else {
            messageQueue.produceMessage(MessageType.OpsServer, null, slbMessageData);
        }

        return responseHandler.handle(ss, hh.getMediaType());
    }

    /**
     * @api {get} /api/op/upMember: [OPS] Mark up member(s)
     * @apiDescription Mark up group member action will take effect only on a single group. It will set `member` status to 'up' on the specified group.
     * @apiName MarkUpMember
     * @apiGroup Operation
     * @apiParam {Long} groupId         id of the target group whose member needs to be marked up
     * @apiParam {String} groupName     name of the target group whose member needs to be marked up
     * @apiParam {String[]} ip        group member ip address(es)
     * @apiParam {Boolean} batch        if multiple group members needs to be marked up, batch value must be explicitly set to true
     * @apiSuccess {GroupServerStatusList}   member statuses by group
     */
    @GET
    @Path("/upMember")
    @Authorize(name = "upDownMember")
    public Response upMember(@Context HttpServletRequest request,
                             @Context HttpHeaders hh,
                             @QueryParam("groupId") Long groupId,
                             @QueryParam("groupName") String groupName,
                             @QueryParam("ip") List<String> ips,
                             @QueryParam("batch") Boolean batch) throws Exception {
        if (groupId == null) {
            if (groupName == null) {
                throw new ValidationException("Group Id or Name not found!");
            } else {
                groupId = groupCriteriaQuery.queryByName(groupName);
            }
        }
        batch = batch == null ? false : batch;

        return memberOps(request, hh, groupId, ips, batch, true, TaskOpsType.MEMBER_OPS);
    }

    /**
     * @api {get} /api/op/downMember: [OPS] Mark down member(s)
     * @apiDescription Mark down group member action will take effect only on a single group. It will set `member` status to 'down' on the specified group.
     * @apiName MarkDownMember
     * @apiGroup Operation
     * @apiParam {Long} groupId         id of the target group whose member needs to be marked down
     * @apiParam {String} groupName     name of the target group whose member needs to be marked down
     * @apiParam {String[]} ip        group member ip address(es)
     * @apiParam {Boolean} batch        if multiple group members needs to be marked down, batch value must be explicitly set to true
     * @apiSuccess {GroupServerStatusList}   member statuses by group
     */
    @GET
    @Path("/downMember")
    @Authorize(name = "upDownMember")
    public Response downMember(@Context HttpServletRequest request,
                               @Context HttpHeaders hh,
                               @QueryParam("groupId") Long groupId,
                               @QueryParam("groupName") String groupName,
                               @QueryParam("ip") List<String> ips,
                               @QueryParam("batch") Boolean batch) throws Exception {
        if (groupId == null) {
            if (groupName == null) {
                throw new ValidationException("Group Id or Name not found!");
            } else {
                groupId = groupCriteriaQuery.queryByName(groupName);
            }
        }

        batch = batch == null ? false : batch;

        return memberOps(request, hh, groupId, ips, batch, false, TaskOpsType.MEMBER_OPS);
    }

    /**
     * @api {get} /api/op/pullIn: [TARS] Pull in member(s)
     * @apiDescription Pull in group member action will take effect only on a single group. It will set `pull` status to 'up' on the specified group.
     * @apiName PullInMember
     * @apiGroup Operation
     * @apiParam {Long} groupId         id of the target group whose member needs to be pulled in
     * @apiParam {String} groupName     name of the target group whose member needs to be pulled in
     * @apiParam {String[]} ip        group member ip address(es)
     * @apiParam {Boolean} batch        if multiple group members needs to be pulled in, batch value must be explicitly set to true
     * @apiSuccess {GroupServerStatusList}   member statuses by group
     */
    @GET
    @Path("/pullIn")
    @Authorize(name = "upDownMember")
    public Response pullIn(@Context HttpServletRequest request,
                           @Context HttpHeaders hh,
                           @QueryParam("groupId") Long groupId,
                           @QueryParam("groupName") String groupName,
                           @QueryParam("ip") List<String> ips,
                           @QueryParam("batch") Boolean batch) throws Exception {
        if (groupId == null) {
            if (groupName == null) {
                throw new ValidationException("Group Id or Name not found!");
            } else {
                groupId = groupCriteriaQuery.queryByName(groupName);
            }
        }
        batch = batch == null ? false : batch;
        return memberOps(request, hh, groupId, ips, batch, true, TaskOpsType.PULL_MEMBER_OPS);
    }

    /**
     * @api {get} /api/op/pullOut: [TARS] Pull out member(s)
     * @apiDescription Pull out group member action will take effect only on a single group. It will set `pull` status to 'down' on the specified group.
     * @apiName PullOutMember
     * @apiGroup Operation
     * @apiParam {Long} groupId         id of the target group whose member needs to be pulled out
     * @apiParam {String} groupName     name of the target group whose member needs to be pulled out
     * @apiParam {String[]} ip        group member ip address(es)
     * @apiParam {Boolean} batch        if multiple group members needs to be pulled out, batch value must be explicitly set to true
     * @apiSuccess {GroupServerStatusList}   member statuses by group
     */
    @GET
    @Path("/pullOut")
    @Authorize(name = "upDownMember")
    public Response pullOut(@Context HttpServletRequest request,
                            @Context HttpHeaders hh,
                            @QueryParam("groupId") Long groupId,
                            @QueryParam("groupName") String groupName,
                            @QueryParam("ip") List<String> ips,
                            @QueryParam("batch") Boolean batch) throws Exception {
        if (groupId == null) {
            if (groupName == null) {
                throw new ValidationException("Group Id or Name not found!");
            } else {
                groupId = groupCriteriaQuery.queryByName(groupName);
            }
        }
        batch = batch == null ? false : batch;
        return memberOps(request, hh, groupId, ips, batch, false, TaskOpsType.PULL_MEMBER_OPS);
    }

    /**
     * @api {get} /api/op/raise: [HC] Raise member(s)
     * @apiDescription Raise group member action will take effect only on a single group. It will set `healthy` status to 'up' on the specified group.
     * @apiName RaiseMember
     * @apiGroup Operation
     * @apiParam {Long} groupId         id of the target group whose member needs to be pulled out
     * @apiParam {String} groupName     name of the target group whose member needs to be pulled out
     * @apiParam {String[]} ip        group member ip address(es)
     * @apiParam {Boolean} batch        if multiple group members needs to be pulled out, batch value must be explicitly set to true
     * @apiSuccess {GroupServerStatusList}   member statuses by group
     */
    @GET
    @Path("/raise")
    @Authorize(name = "upDownMember")
    public Response raise(@Context HttpServletRequest request,
                          @Context HttpHeaders hh,
                          @QueryParam("groupId") Long groupId,
                          @QueryParam("groupName") String groupName,
                          @QueryParam("ip") List<String> ips,
                          @QueryParam("batch") Boolean batch) throws Exception {
        if (groupId == null) {
            if (groupName == null) {
                throw new ValidationException("Group Id or Name not found!");
            } else {
                groupId = groupCriteriaQuery.queryByName(groupName);
            }
        }
        Group gp = groupRepository.getById(groupId);
        if (gp == null) {
            throw new ValidationException("Group Id or Name not found!");
        }

        batch = batch == null ? false : batch;
        if (healthyOpsActivate.get()) {
            return memberOps(request, hh, groupId, ips, batch, true, TaskOpsType.HEALTHY_OPS);
        } else {
            return healthyOps(hh, groupId, ips, true);
        }
    }

    /**
     * @api {get} /api/op/fall: [HC] Fall member(s)
     * @apiDescription Fall group member action will take effect only on a single group. It will set `healthy` status to 'down' on the specified group.
     * @apiName FallMember
     * @apiGroup Operation
     * @apiParam {Long} groupId         id of the target group whose member needs to be pulled out
     * @apiParam {String} groupName     name of the target group whose member needs to be pulled out
     * @apiParam {String[]} ip        group member ip address(es)
     * @apiParam {Boolean} batch        if multiple group members needs to be pulled out, batch value must be explicitly set to true
     * @apiSuccess {GroupServerStatusList}   member statuses by group
     */
    @GET
    @Path("/fall")
    @Authorize(name = "upDownMember")
    public Response fall(@Context HttpServletRequest request,
                         @Context HttpHeaders hh,
                         @QueryParam("groupId") Long groupId,
                         @QueryParam("groupName") String groupName,
                         @QueryParam("ip") List<String> ips,
                         @QueryParam("batch") Boolean batch) throws Exception {
        if (groupId == null) {
            if (groupName == null) {
                throw new ValidationException("Group Id or Name not found!");
            } else {
                groupId = groupCriteriaQuery.queryByName(groupName);
            }
        }
        batch = batch == null ? false : batch;
        if (healthyOpsActivate.get()) {
            return memberOps(request, hh, groupId, ips, batch, false, TaskOpsType.HEALTHY_OPS);
        } else {
            return healthyOps(hh, groupId, ips, false);
        }
    }

    @POST
    @Path("/uploadcerts")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Authorize(name = "uploadCerts")
    @Deprecated
    public void uploadCertAlias(@Context HttpServletRequest request,
                                @Context HttpServletResponse response,
                                @Context HttpHeaders hh,
                                @FormDataParam("cert") InputStream cert,
                                @FormDataParam("key") InputStream key,
                                @QueryParam("domain") String domain) throws Exception {
        RequestDispatcher dispatcher = request.getServletContext().getRequestDispatcher("/api/cert/upload");
        dispatcher.forward(request, response);
    }

    private Response healthyOps(HttpHeaders hh, Long groupId, List<String> ips, boolean b) throws Exception {
        statusService.updateStatus(groupId, ips, StatusOffset.HEALTHY, b);
        return responseHandler.handle(groupStatusService.getOfflineGroupStatus(groupId), hh.getMediaType());
    }

    private Response memberOps(HttpServletRequest request, HttpHeaders hh, Long groupId, List<String> memberIps, boolean batch, boolean up, String type) throws Exception {

        ModelStatusMapping<Group> groupMap = entityFactory.getGroupsByIds(new Long[]{groupId});
        if (groupMap.getOfflineMapping() == null || groupMap.getOfflineMapping().size() == 0) {
            throw new ValidationException("Not Found Group By Id.");
        }

        Set<String> groupMemberIps = new HashSet<>();
        for (GroupServer gs : groupMap.getOfflineMapping().get(groupId).getGroupServers()) {
            groupMemberIps.add(gs.getIp());
        }
        if (groupMap.getOnlineMapping().get(groupId) != null) {
            for (GroupServer gs : groupMap.getOnlineMapping().get(groupId).getGroupServers()) {
                groupMemberIps.add(gs.getIp());
            }
        }

        List<String> ips = new ArrayList<>();
        if (batch) {
            Group gp = groupMap.getOfflineMapping().get(groupId);
            List<GroupServer> servers = gp.getGroupServers();
            for (GroupServer gs : servers) {
                ips.add(gs.getIp());
            }
        } else if (memberIps != null && memberIps.size() > 0) {
            for (String ip : memberIps) {
                if (groupMemberIps.contains(ip)) {
                    ips.add(ip);
                }
            }
        }
        if (ips.size() == 0) {
            throw new ValidationException("Ip Param Is Null Or Invalidate Ip Param.");
        }

        List<GroupStatus> statuses = groupStatusService.getOfflineGroupsStatus(groupMap);
        GroupStatus status = null;
        if (statuses.size() > 0) {
            status = statuses.get(0);
        }

        boolean skipOps = false;
        if (status != null) {
            skipOps = true;
            for (GroupServerStatus gss : status.getGroupServerStatuses()) {
                if (ips.contains(gss.getIp())) {
                    if (type.equals(TaskOpsType.HEALTHY_OPS) && gss.getHealthy() != up) {
                        skipOps = false;
                    } else if (type.equals(TaskOpsType.PULL_MEMBER_OPS) && gss.getPull() != up) {
                        skipOps = false;
                    } else if (type.equals(TaskOpsType.MEMBER_OPS) && gss.getMember() != up) {
                        skipOps = false;
                    }
                }
            }
        }

        if (skipOps) {
            GroupStatus groupStatus = groupStatusService.getOfflineGroupStatus(groupId);
            logger.info("Group status equals the desired value.Do not need execute task.GroupId:" + groupId + " ips:"
                    + ips.toString() + " up:" + up + " type:" + type);
            return responseHandler.handle(groupStatus, hh.getMediaType());
        }

        StringBuilder sb = new StringBuilder();
        for (String ip : ips) {
            sb.append(ip).append(";");
        }

        Group onlineGroup = groupMap.getOnlineMapping().get(groupId);
        Group offlineGroup = groupMap.getOfflineMapping().get(groupId);
        Set<Long> vsIds = new HashSet<>();
        if (onlineGroup != null) {
            for (GroupVirtualServer gvs : onlineGroup.getGroupVirtualServers()) {
                vsIds.add(gvs.getVirtualServer().getId());
            }
        }
        for (GroupVirtualServer gvs : offlineGroup.getGroupVirtualServers()) {
            vsIds.add(gvs.getVirtualServer().getId());
        }

        Set<IdVersion> vsIdVersions = virtualServerCriteriaQuery.queryByIdsAndMode(vsIds.toArray(new Long[]{}), SelectionMode.ONLINE_FIRST);
        Set<Long> slbIds = slbCriteriaQuery.queryByVses(vsIdVersions.toArray(new IdVersion[]{}));

        List<OpsTask> tasks = new ArrayList<>();
        for (Long slbId : slbIds) {
            OpsTask task = new OpsTask();
            task.setTargetSlbId(slbId);
            task.setOpsType(type);
            task.setUp(up);
            task.setGroupId(groupId);
            task.setIpList(sb.toString());
            tasks.add(task);
        }
        List<Long> taskIds = taskManager.addTask(tasks);
        List<TaskResult> results = taskManager.getResult(taskIds, apiTimeout.get());
        for (TaskResult taskResult : results) {
            if (!taskResult.isSuccess()) {
                throw new Exception("Task Failed! Fail cause : " + taskResult.getFailCause());
            }
        }
        GroupStatus groupStatus = groupStatusService.getOfflineGroupStatus(groupId);

        String slbMessageData = MessageUtil.getMessageData(request, new Group[]{offlineGroup}, null, null, ips.toArray(new String[ips.size()]), true);
        if (configHandler.getEnable("use.new,message.queue.producer", false)) {
            messageQueue.produceMessage(request.getRequestURI(), groupId, slbMessageData);
        } else {
            if (type.equals(TaskOpsType.HEALTHY_OPS)) {
                messageQueue.produceMessage(MessageType.OpsHealthy, groupId, slbMessageData);
            } else if (type.equals(TaskOpsType.PULL_MEMBER_OPS)) {
                messageQueue.produceMessage(MessageType.OpsPull, groupId, slbMessageData);
            } else if (type.equals(TaskOpsType.MEMBER_OPS)) {
                messageQueue.produceMessage(MessageType.OpsMember, groupId, slbMessageData);
            }
        }
        return responseHandler.handle(groupStatus, hh.getMediaType());
    }

    @GET
    @Path("/fillStatusData")
    public Response fillData(@Context HttpServletRequest request,
                             @Context HttpHeaders hh) throws Exception {
        List<GroupStatus> gses = groupStatusService.getAllOfflineGroupsStatus();

        HashSet<Long> upHealthy = new HashSet<>();
        HashSet<Long> upUnhealthy = new HashSet<>();
        HashSet<Long> upBroken = new HashSet<>();
        HashSet<Long> healthHealthy = new HashSet<>();
        HashSet<Long> healthUnhealthy = new HashSet<>();
        HashSet<Long> healthBroken = new HashSet<>();
        HashSet<Long> pullInHealthy = new HashSet<>();
        HashSet<Long> pullInUnhealthy = new HashSet<>();
        HashSet<Long> pullInBroken = new HashSet<>();
        HashSet<Long> memberUpHealthy = new HashSet<>();
        HashSet<Long> memberUpUnhealthy = new HashSet<>();
        HashSet<Long> memberUpBroken = new HashSet<>();
        HashSet<Long> serverUpHealthy = new HashSet<>();
        HashSet<Long> serverUpUnhealthy = new HashSet<>();
        HashSet<Long> serverUpBroken = new HashSet<>();


        for (GroupStatus gs : gses) {
            int upCount = 0;
            int healthCount = 0;
            int pullInCount = 0;
            int memberUpCount = 0;
            int serverUpCount = 0;
            int allServerCount = gs.getGroupServerStatuses().size();

            for (GroupServerStatus gss : gs.getGroupServerStatuses()) {
                if (gss.getServer() && gss.getHealthy() && gss.getPull() && gss.getMember()) {
                    upCount += 1;
                    serverUpCount += 1;
                    healthCount += 1;
                    pullInCount += 1;
                    memberUpCount += 1;
                    continue;
                }
                if (gss.getServer()) {
                    serverUpCount += 1;
                }
                if (gss.getHealthy()) {
                    healthCount += 1;
                }
                if (gss.getPull()) {
                    pullInCount += 1;
                }
                if (gss.getMember()) {
                    memberUpCount += 1;
                }
            }
            if (upCount == allServerCount) {
                upHealthy.add(gs.getGroupId());
            } else if (upCount == 0) {
                upBroken.add(gs.getGroupId());
            } else {
                upUnhealthy.add(gs.getGroupId());
            }

            if (serverUpCount == allServerCount) {
                serverUpHealthy.add(gs.getGroupId());
            } else if (serverUpCount == 0) {
                serverUpBroken.add(gs.getGroupId());
            } else {
                serverUpUnhealthy.add(gs.getGroupId());
            }
            if (memberUpCount == allServerCount) {
                memberUpHealthy.add(gs.getGroupId());
            } else if (memberUpCount == 0) {
                memberUpBroken.add(gs.getGroupId());
            } else {
                memberUpUnhealthy.add(gs.getGroupId());
            }
            if (pullInCount == allServerCount) {
                pullInHealthy.add(gs.getGroupId());
            } else if (pullInCount == 0) {
                pullInBroken.add(gs.getGroupId());
            } else {
                pullInUnhealthy.add(gs.getGroupId());
            }
            if (healthCount == allServerCount) {
                healthHealthy.add(gs.getGroupId());
            } else if (healthCount == 0) {
                healthBroken.add(gs.getGroupId());
            } else {
                healthUnhealthy.add(gs.getGroupId());
            }
        }
        if (healthBroken.size() > 0) {
            propertyBox.set("healthCheckHealthy", "broken", "group", healthBroken.toArray(new Long[]{}));
        }
        if (healthHealthy.size() > 0) {
            propertyBox.set("healthCheckHealthy", "healthy", "group", healthHealthy.toArray(new Long[]{}));
        }
        if (healthUnhealthy.size() > 0) {
            propertyBox.set("healthCheckHealthy", "unhealthy", "group", healthUnhealthy.toArray(new Long[]{}));
        }
        if (pullInBroken.size() > 0) {
            propertyBox.set("pullHealthy", "broken", "group", pullInBroken.toArray(new Long[]{}));
        }
        if (pullInHealthy.size() > 0) {
            propertyBox.set("pullHealthy", "healthy", "group", pullInHealthy.toArray(new Long[]{}));
        }
        if (pullInUnhealthy.size() > 0) {
            propertyBox.set("pullHealthy", "unhealthy", "group", pullInUnhealthy.toArray(new Long[]{}));
        }
        if (memberUpBroken.size() > 0) {
            propertyBox.set("memberHealthy", "broken", "group", memberUpBroken.toArray(new Long[]{}));
        }
        if (memberUpHealthy.size() > 0) {
            propertyBox.set("memberHealthy", "healthy", "group", memberUpHealthy.toArray(new Long[]{}));
        }
        if (memberUpUnhealthy.size() > 0) {
            propertyBox.set("memberHealthy", "unhealthy", "group", memberUpUnhealthy.toArray(new Long[]{}));
        }
        if (serverUpBroken.size() > 0) {
            propertyBox.set("serverHealthy", "broken", "group", serverUpBroken.toArray(new Long[]{}));
        }
        if (serverUpHealthy.size() > 0) {
            propertyBox.set("serverHealthy", "healthy", "group", serverUpHealthy.toArray(new Long[]{}));
        }
        if (serverUpUnhealthy.size() > 0) {
            propertyBox.set("serverHealthy", "unhealthy", "group", serverUpUnhealthy.toArray(new Long[]{}));
        }
        if (upBroken.size() > 0) {
            propertyBox.set("healthy", "broken", "group", upBroken.toArray(new Long[]{}));
        }
        if (upHealthy.size() > 0) {
            propertyBox.set("healthy", "healthy", "group", upHealthy.toArray(new Long[]{}));
        }
        if (upUnhealthy.size() > 0) {
            propertyBox.set("healthy", "unhealthy", "group", upUnhealthy.toArray(new Long[]{}));
        }
        return responseHandler.handle("Success.", hh.getMediaType());
    }

}


