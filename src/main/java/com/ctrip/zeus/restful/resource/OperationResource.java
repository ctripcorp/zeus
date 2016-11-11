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

    @GET
    @Path("/upServer")
    @Authorize(name = "upDownServer")
    public Response upServer(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("ip") String ip) throws Exception {
        return serverOps(request, hh, ip, true);
    }

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

    @GET
    @Path("/upMember")
    @Authorize(name = "upDownMember")
    public Response upMember(@Context HttpServletRequest request,
                             @Context HttpHeaders hh,
                             @QueryParam("groupId") Long groupId,
                             @QueryParam("groupName") String groupName,
                             @QueryParam("ip") List<String> ips,
                             @QueryParam("batch") Boolean batch) throws Exception {
        List<String> _ips = new ArrayList<>();
        if (groupId == null) {
            if (groupName == null) {
                throw new ValidationException("Group Id or Name not found!");
            } else {
                groupId = groupCriteriaQuery.queryByName(groupName);
            }
        }
        batch = batch == null ? false : batch;

        return memberOps(request, hh, groupId, _ips, batch, true, TaskOpsType.MEMBER_OPS);
    }

    @GET
    @Path("/downMember")
    @Authorize(name = "upDownMember")
    public Response downMember(@Context HttpServletRequest request,
                               @Context HttpHeaders hh,
                               @QueryParam("groupId") Long groupId,
                               @QueryParam("groupName") String groupName,
                               @QueryParam("ip") List<String> ips,
                               @QueryParam("batch") Boolean batch) throws Exception {
        List<String> _ips = new ArrayList<>();
        if (groupId == null) {
            if (groupName == null) {
                throw new ValidationException("Group Id or Name not found!");
            } else {
                groupId = groupCriteriaQuery.queryByName(groupName);
            }
        }

        batch = batch == null ? false : batch;

        return memberOps(request, hh, groupId, _ips, batch, false, TaskOpsType.MEMBER_OPS);
    }

    @GET
    @Path("/pullIn")
    @Authorize(name = "upDownMember")
    public Response pullIn(@Context HttpServletRequest request,
                           @Context HttpHeaders hh,
                           @QueryParam("groupId") Long groupId,
                           @QueryParam("groupName") String groupName,
                           @QueryParam("ip") List<String> ips,
                           @QueryParam("batch") Boolean batch) throws Exception {
        List<String> _ips = new ArrayList<>();
        if (groupId == null) {
            if (groupName == null) {
                throw new ValidationException("Group Id or Name not found!");
            } else {
                groupId = groupCriteriaQuery.queryByName(groupName);
            }
        }
        batch = batch == null ? false : batch;
        return memberOps(request, hh, groupId, _ips, batch, true, TaskOpsType.PULL_MEMBER_OPS);
    }

    @GET
    @Path("/pullOut")
    @Authorize(name = "upDownMember")
    public Response pullOut(@Context HttpServletRequest request,
                            @Context HttpHeaders hh,
                            @QueryParam("groupId") Long groupId,
                            @QueryParam("groupName") String groupName,
                            @QueryParam("ip") List<String> ips,
                            @QueryParam("batch") Boolean batch) throws Exception {
        List<String> _ips = new ArrayList<>();
        if (groupId == null) {
            if (groupName == null) {
                throw new ValidationException("Group Id or Name not found!");
            } else {
                groupId = groupCriteriaQuery.queryByName(groupName);
            }
        }
        batch = batch == null ? false : batch;
        return memberOps(request, hh, groupId, _ips, batch, false, TaskOpsType.PULL_MEMBER_OPS);
    }

    @GET
    @Path("/raise")
    @Authorize(name = "upDownMember")
    public Response raise(@Context HttpServletRequest request,
                          @Context HttpHeaders hh,
                          @QueryParam("groupId") Long groupId,
                          @QueryParam("groupName") String groupName,
                          @QueryParam("ip") List<String> ips,
                          @QueryParam("batch") Boolean batch) throws Exception {
        List<String> _ips = new ArrayList<>();
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
            return memberOps(request, hh, groupId, _ips, batch, true, TaskOpsType.HEALTHY_OPS);
        } else {
            return healthyOps(hh, groupId, _ips, true);
        }
    }

    @GET
    @Path("/fall")
    @Authorize(name = "upDownMember")
    public Response fall(@Context HttpServletRequest request,
                         @Context HttpHeaders hh,
                         @QueryParam("groupId") Long groupId,
                         @QueryParam("groupName") String groupName,
                         @QueryParam("ip") List<String> ips,
                         @QueryParam("batch") Boolean batch) throws Exception {
        List<String> _ips = new ArrayList<>();
        if (groupId == null) {
            if (groupName == null) {
                throw new ValidationException("Group Id or Name not found!");
            } else {
                groupId = groupCriteriaQuery.queryByName(groupName);
            }
        }
        batch = batch == null ? false : batch;
        if (healthyOpsActivate.get()) {
            return memberOps(request, hh, groupId, _ips, batch, false, TaskOpsType.HEALTHY_OPS);
        } else {
            return healthyOps(hh, groupId, _ips, false);
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
                    ips.addAll(memberIps);
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

}


