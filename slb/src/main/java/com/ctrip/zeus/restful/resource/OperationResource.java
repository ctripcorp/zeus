package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.executor.TaskManager;
import com.ctrip.zeus.model.model.Group;
import com.ctrip.zeus.model.model.GroupServer;
import com.ctrip.zeus.model.model.GroupVirtualServer;
import com.ctrip.zeus.model.model.VirtualServer;
import com.ctrip.zeus.model.status.GroupServerStatus;
import com.ctrip.zeus.model.status.GroupStatus;
import com.ctrip.zeus.model.status.ServerStatus;
import com.ctrip.zeus.model.task.OpsTask;
import com.ctrip.zeus.model.task.TaskResult;
import com.ctrip.zeus.restful.message.OperationRequest;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.auth.AuthService;
import com.ctrip.zeus.service.auth.ResourceDataType;
import com.ctrip.zeus.service.auth.ResourceOperationType;
import com.ctrip.zeus.service.message.queue.MessageQueue;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.service.status.GroupStatusService;
import com.ctrip.zeus.service.status.StatusOffset;
import com.ctrip.zeus.service.status.StatusService;
import com.ctrip.zeus.service.task.constant.TaskOpsType;
import com.ctrip.zeus.tag.PropertyBox;
import com.ctrip.zeus.util.MessageUtil;
import com.ctrip.zeus.util.UserUtils;
import com.google.common.base.Joiner;
import com.netflix.config.DynamicLongProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;

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
    @Autowired
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
    private AuthService authService;
    @Resource
    private PropertyBox propertyBox;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private MessageQueue messageQueue;


    private static DynamicLongProperty apiTimeout = DynamicPropertyFactory.getInstance().getLongProperty("api.timeout", 30000L);

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
    public Response upServer(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("ip") String ip) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.OP_SERVER, ResourceDataType.Ip, ip);
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
    public Response downServer(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("ip") String ip) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.OP_SERVER, ResourceDataType.Ip, ip);
        return serverOps(request, hh, ip, false);
    }

    private Response serverOps(HttpServletRequest request, HttpHeaders hh, String serverip, boolean up) throws Exception {
        Long[] groupIds = entityFactory.getGroupIdsByGroupServerIp(serverip, SelectionMode.REDUNDANT);

        if (groupIds == null || groupIds.length == 0) {
            throw new ValidationException("Not found Server Ip, groups are null or empty search by serverIp. serverIp:" + serverip);
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
        StringBuilder failCause = new StringBuilder(128);
        for (TaskResult taskResult : results) {
            if (!taskResult.isSuccess()) {
                isSuccess = false;
                failCause.append(taskResult.toString());
            }
        }
        if (!isSuccess) {
            throw new Exception(failCause.toString());
        }
        ServerStatus ss = new ServerStatus().setIp(serverip).setUp(statusService.getServerStatus(serverip));

        Long[] gids = entityFactory.getGroupIdsByGroupServerIp(serverip, SelectionMode.ONLINE_FIRST);

        List<Group> groups = groupRepository.list(gids);
        Group[] groupArray = null;
        if (groups != null) {
            for (Group group : groups) {
                ss.addGroupName(group.getName());
            }
            groupArray = groups.toArray(new Group[]{});
        }
        String slbMessageData = MessageUtil.getMessageData(request, groupArray, null, null, null, new String[]{serverip}, true);
        messageQueue.produceMessage(request.getRequestURI(), null, slbMessageData);

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
    public Response upMember(@Context HttpServletRequest request,
                             @Context HttpHeaders hh,
                             @QueryParam("groupId") Long groupId,
                             @QueryParam("groupName") String groupName,
                             @QueryParam("ip") List<String> ips,
                             @QueryParam("batch") Boolean batch) throws Exception {
        if (groupId == null) {
            if (groupName == null) {
                throw new ValidationException("Group id or name must be provided.");
            } else {
                groupId = groupCriteriaQuery.queryByName(groupName);
            }
        }
        batch = batch == null ? false : batch;
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.OP_MEMBER, ResourceDataType.Group, groupId);

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
    public Response downMember(@Context HttpServletRequest request,
                               @Context HttpHeaders hh,
                               @QueryParam("groupId") Long groupId,
                               @QueryParam("groupName") String groupName,
                               @QueryParam("ip") List<String> ips,
                               @QueryParam("batch") Boolean batch) throws Exception {
        if (groupId == null) {
            if (groupName == null) {
                throw new ValidationException("Group id or name must be provided.");
            } else {
                groupId = groupCriteriaQuery.queryByName(groupName);
            }
        }
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.OP_MEMBER, ResourceDataType.Group, groupId);
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
    public Response pullIn(@Context HttpServletRequest request,
                           @Context HttpHeaders hh,
                           @QueryParam("groupId") Long groupId,
                           @QueryParam("groupName") String groupName,
                           @QueryParam("ip") List<String> ips,
                           @QueryParam("batch") Boolean batch) throws Exception {
        if (groupId == null) {
            if (groupName == null) {
                throw new ValidationException("Group id or name must be provided.");
            } else {
                groupId = groupCriteriaQuery.queryByName(groupName);
            }
        }
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.OP_PULL, ResourceDataType.Group, groupId);
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
    public Response pullOut(@Context HttpServletRequest request,
                            @Context HttpHeaders hh,
                            @QueryParam("groupId") Long groupId,
                            @QueryParam("groupName") String groupName,
                            @QueryParam("ip") List<String> ips,
                            @QueryParam("batch") Boolean batch) throws Exception {
        if (groupId == null) {
            if (groupName == null) {
                throw new ValidationException("Group id or name must be provided.");
            } else {
                groupId = groupCriteriaQuery.queryByName(groupName);
            }
        }
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.OP_PULL, ResourceDataType.Group, groupId);
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
    public Response raise(@Context HttpServletRequest request,
                          @Context HttpHeaders hh,
                          @QueryParam("groupId") Long groupId,
                          @QueryParam("groupName") String groupName,
                          @QueryParam("ip") List<String> ips,
                          @QueryParam("batch") Boolean batch) throws Exception {
        if (groupId == null) {
            if (groupName == null) {
                throw new ValidationException("Group id or name must be provided.");
            } else {
                groupId = groupCriteriaQuery.queryByName(groupName);
            }
        }
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.OP_HEALTH_CHECK, ResourceDataType.Group, groupId);

        Group gp = groupRepository.getById(groupId);
        if (gp == null) {
            throw new ValidationException("Group Id or Name not found!");
        }

        batch = batch == null ? false : batch;
        return memberOps(request, hh, groupId, ips, batch, true, TaskOpsType.HEALTHY_OPS);
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
    public Response fall(@Context HttpServletRequest request,
                         @Context HttpHeaders hh,
                         @QueryParam("groupId") Long groupId,
                         @QueryParam("groupName") String groupName,
                         @QueryParam("ip") List<String> ips,
                         @QueryParam("batch") Boolean batch) throws Exception {
        if (groupId == null) {
            if (groupName == null) {
                throw new ValidationException("Group id or name must be provided.");
            } else {
                groupId = groupCriteriaQuery.queryByName(groupName);
            }
        }
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.OP_HEALTH_CHECK, ResourceDataType.Group, groupId);
        batch = batch == null ? false : batch;
        return memberOps(request, hh, groupId, ips, batch, false, TaskOpsType.HEALTHY_OPS);
    }

    /*
     * @Description: batch set multiple status bits to be up or down
     * @return
     **/
    @POST
    @Path("/batchop")
    public Response batchOperation(@Context HttpServletRequest req,
                                   @Context HttpHeaders headers,
                                   @RequestBody List<OperationRequest> operations) throws Exception {
        if (operations == null || operations.size() <= 0) {
            throw new ValidationException("No job specified or request body is of bad format");
        }

        String userName = UserUtils.getUserName(req);
        boolean taskNeeded = false;
        Set<OperationRequest> request2Process = new HashSet<>();
        for (OperationRequest operationRequest : operations) {
            validateOperationRequest(operationRequest);

            // check authority
            ResourceOperationType operationType = ResourceOperationType.valueOf(operationRequest.getType().toUpperCase());
            authService.authValidate(userName, operationType, ResourceDataType.Group, operationRequest.getGroupId());

            Long groupId = operationRequest.getGroupId();
            GroupStatus groupStatus = groupStatusService.getOfflineGroupStatus(groupId);

            // decide whether this operation request should be processed.
            if (!shouldSkipBatchOperation(groupStatus, operationRequest.getIps(),
                    operationRequest.isUp(), Collections.singletonList(operationType))) {
                request2Process.add(operationRequest);
                if (!taskNeeded) {
                    taskNeeded = true;
                }
            }
        }

        if (!taskNeeded) {
            logger.info("all group servers' status are equal to their expected ones. No task executed");
            return responseHandler.handle("No task needed", headers.getMediaType());
        }

        // generate tasks
        List<OpsTask> opsTasks = new ArrayList<>();
        for (OperationRequest operationRequest : operations) {
            opsTasks.addAll(generateOpsTasks(operationRequest));
        }

        // add task and fetch results
        List<Long> taskIds = taskManager.addAggTask(opsTasks);
        List<TaskResult> results = taskManager.getAggResult(taskIds, apiTimeout.get());
        for (TaskResult taskResult : results) {
            if (!taskResult.isSuccess()) {
                throw new Exception("Task Failed! Fail cause : " + taskResult.getFailCause());
            }
        }

        List<GroupStatus> groupStatuses = new ArrayList<>(operations.size());
        Set<Long> seenGroupIds = new HashSet<>();
        for (OperationRequest operationRequest : operations) {
            if (!request2Process.contains(operationRequest)) {
                continue;
            }
            Long groupId = operationRequest.getGroupId();
            if (!seenGroupIds.contains(groupId)) {
                groupStatuses.add(groupStatusService.getOfflineGroupStatus(groupId));
                seenGroupIds.add(groupId);
            }

            List<String> ips = operationRequest.getIps();
            ModelStatusMapping<Group> statusMapping = entityFactory.getGroupsByIds(new Long[]{groupId});
            String type = getRequestURI(operationRequest);
            String slbMessageData = MessageUtil.getMessageBuilder(req, true).bindType(type).bindGroups(new Group[]{statusMapping.getOfflineMapping().get(groupId)}).bindIps(ips.toArray(new String[ips.size()])).build();
            messageQueue.produceMessage(getRequestURI(operationRequest), groupId, slbMessageData);
        }

        return responseHandler.handle(groupStatuses, headers.getMediaType());
    }

    private String getRequestURI(OperationRequest operationRequest) throws Exception {
        ResourceOperationType resourceOperationType = operationRequest.getResourceOperationType();
        switch (resourceOperationType) {
            case OP_HEALTH_CHECK:
                if (operationRequest.isUp()) {
                    return "/api/op/raise";
                } else {
                    return "/api/op/fall";
                }
            case OP_MEMBER:
                if (operationRequest.isUp()) {
                    return "/api/op/upMember";
                } else {
                    return "/api/op/downMember";
                }
            case OP_PULL:
                if (operationRequest.isUp()) {
                    return "/api/op/pullIn";
                } else {
                    return "/api/op/pullOut";
                }
            case OP_SERVER:
                if (operationRequest.isUp()) {
                    return "/api/op/upServer";
                } else {
                    return "/api/op/downServer";
                }
        }

        throw new RuntimeException("Invalid operation type");
    }

    private void validateOperationRequest(OperationRequest operationRequest) throws Exception {
        if (operationRequest == null) {
            throw new ValidationException("Invalid request body");
        }
        // validate groupId or groupName
        Long[] groupIds = new Long[1];
        if (operationRequest.getGroupId() != null && operationRequest.getGroupId() > 0) {
            groupIds[0] = operationRequest.getGroupId();
        } else if (operationRequest.getGroupName() != null) {
            Long temp = groupCriteriaQuery.queryByName(operationRequest.getGroupName());
            groupIds[0] = temp;
        }
        ModelStatusMapping<Group> groupModelStatusMapping = entityFactory.getGroupsByIds(groupIds);
        if (groupModelStatusMapping.getOfflineMapping() == null || !groupModelStatusMapping.getOfflineMapping().containsKey(groupIds[0])) {
            throw new ValidationException("Incorrect groupId or groupName");
        }

        ResourceOperationType resourceOperationType = operationRequest.getResourceOperationType();
        if (resourceOperationType == null || !ResourceOperationType.getStatusOperationSet().contains(resourceOperationType)) {
            throw new ValidationException("Invalid operation type for OperationRequest.");
        }
    }

    /*
     * @Description: return true if all specified status of the group server are same with up
     * @return
     **/
    private boolean shouldSkipBatchOperation(GroupStatus groupStatus, List<String> ips, boolean expectUp, List<ResourceOperationType> operationTypes) throws Exception {
        if (groupStatus == null || CollectionUtils.isEmpty(ips)) {
            return true;
        }
        Map<String, GroupServerStatus> serverStatusMap = new HashMap<>(groupStatus.getGroupServerStatuses().size());
        for (GroupServerStatus serverStatus : groupStatus.getGroupServerStatuses()) {
            serverStatusMap.put(serverStatus.getIp(), serverStatus);
        }

        for (String ip : ips) {
            GroupServerStatus targetServerStatus = serverStatusMap.get(ip);
            if (targetServerStatus == null) {
                throw new ValidationException("Cannot find server in group. Server's IP: " + ip + ", groupId: " + groupStatus.getGroupId());
            }

            // compare status bit in turn
            for (ResourceOperationType operationType : operationTypes) {
                if (!ResourceOperationType.getStatusOperationSet().contains(operationType)) {
                    continue;
                }
                switch (operationType) {
                    case OP_MEMBER:
                        if (expectUp != targetServerStatus.getMember()) {
                            return false;
                        }
                        break;
                    case OP_SERVER:
                        if (expectUp != targetServerStatus.getServer()) {
                            return false;
                        }
                        break;
                    case OP_HEALTH_CHECK:
                        if (expectUp != targetServerStatus.getHealthy()) {
                            return false;
                        }
                        break;
                    case OP_PULL:
                        if (expectUp != targetServerStatus.getPull()) {
                            return false;
                        }
                        break;
                }
            }
        }

        return true;
    }

    private List<OpsTask> generateOpsTasks(OperationRequest operationRequest) throws Exception {
        ResourceOperationType operationType = ResourceOperationType.valueOf(operationRequest.getType().toUpperCase());
        Long groupId = operationRequest.getGroupId();
        Set<Long> targetSlbIds = getSlbIdsByGroupId(groupId);

        List<OpsTask> tasks = new ArrayList<>(targetSlbIds.size());
        String taskOpsType = getTaskOpsType(operationType);
        for (Long targetSlbId : targetSlbIds) {
            OpsTask task = new OpsTask()
                    .setOpsType(taskOpsType)
                    .setTargetSlbId(targetSlbId)
                    .setGroupId(groupId)
                    .setUp(operationRequest.isUp())
                    .setIpList(Joiner.on(";").join(operationRequest.getIps()));
            tasks.add(task);
        }

        return tasks;
    }

    private String getTaskOpsType(ResourceOperationType operationType) {
        if (!ResourceOperationType.getStatusOperationSet().contains(operationType)) {
            return null;
        }
        switch (operationType) {
            case OP_SERVER:
                return TaskOpsType.SERVER_OPS;
            case OP_MEMBER:
                return TaskOpsType.MEMBER_OPS;
            case OP_PULL:
                return TaskOpsType.PULL_MEMBER_OPS;
            case OP_HEALTH_CHECK:
                return TaskOpsType.HEALTHY_OPS;
            default:
                return null;
        }
    }

    @POST
    @Path("/uploadcerts")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
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

    @GET
    @Path("/installcerts")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Deprecated
    public void installCertAlias(@Context HttpServletRequest request,
                                 @Context HttpServletResponse response,
                                 @Context HttpHeaders hh,
                                 @QueryParam("vsId") Long vsId,
                                 @QueryParam("certId") Long certId) throws Exception {
        RequestDispatcher dispatcher = request.getServletContext().getRequestDispatcher("/api/cert/localInstall");
        dispatcher.forward(request, response);
    }

    @GET
    @Path("/cert/batchInstall")
    @Deprecated
    public void uploadCertAlias(@Context HttpServletRequest request,
                                @Context HttpServletResponse response,
                                @Context HttpHeaders hh,
                                @QueryParam("slbId") Long slbId) throws Exception {
        RequestDispatcher dispatcher = request.getServletContext().getRequestDispatcher("/api/cert/localBatchInstall");
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

        List<OpsTask> tasks = new ArrayList<>();
        for (Long slbId : getSlbIdsByGroupId(groupId)) {
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
        String slbMessageData = MessageUtil.getMessageData(request, new Group[]{groupMap.getOfflineMapping().get(groupId)}, null,
                null, null, ips.toArray(new String[ips.size()]), true);
        messageQueue.produceMessage(request.getRequestURI(), groupId, slbMessageData);
        return responseHandler.handle(groupStatus, hh.getMediaType());
    }

    private Set<Long> getSlbIdsByGroupId(Long groupId) throws Exception {
        if (groupId == null) {
            return new HashSet<>();
        }
        ModelStatusMapping<Group> statusMapping = entityFactory.getGroupsByIds(new Long[]{groupId});
        Group onlineGroup = statusMapping.getOnlineMapping().get(groupId);
        Group offlineGroup = statusMapping.getOfflineMapping().get(groupId);
        Set<Long> vsIds = new HashSet<>();
        if (onlineGroup != null) {
            for (GroupVirtualServer gvs : onlineGroup.getGroupVirtualServers()) {
                vsIds.add(gvs.getVirtualServer().getId());
            }
        }
        if (offlineGroup != null) {
            for (GroupVirtualServer gvs : offlineGroup.getGroupVirtualServers()) {
                vsIds.add(gvs.getVirtualServer().getId());
            }
        }
        Set<IdVersion> vsIdVersions = virtualServerCriteriaQuery.queryByIdsAndMode(vsIds.toArray(new Long[]{}), SelectionMode.ONLINE_FIRST);
        return slbCriteriaQuery.queryByVses(vsIdVersions.toArray(new IdVersion[]{}));
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
        setProperties(healthBroken,"healthCheckHealthy", "broken");
        setProperties(healthHealthy,"healthCheckHealthy", "healthy");
        setProperties(healthUnhealthy,"healthCheckHealthy", "unhealthy");
        setProperties(pullInBroken,"pullHealthy", "broken");
        setProperties(pullInHealthy,"pullHealthy", "healthy");
        setProperties(pullInUnhealthy,"pullHealthy", "unhealthy");
        setProperties(memberUpBroken,"memberHealthy", "broken");
        setProperties(memberUpHealthy,"memberHealthy", "healthy");
        setProperties(memberUpUnhealthy,"memberHealthy", "unhealthy");
        setProperties(serverUpBroken,"serverHealthy", "broken");
        setProperties(serverUpHealthy,"serverHealthy", "healthy");
        setProperties(serverUpUnhealthy,"serverHealthy", "unhealthy");
        setProperties(upBroken,"healthy", "broken");
        setProperties(upHealthy,"healthy", "healthy");
        setProperties(upUnhealthy, "healthy", "unhealthy");
        return responseHandler.handle("Success.", hh.getMediaType());
    }

    private void setProperties(Set<Long> ids, String pName, String pValue) throws Exception {
        if (ids.size() > 0) {
            propertyBox.set(pName, pValue, "group", ids.toArray(new Long[]{}));
        }
    }
}


