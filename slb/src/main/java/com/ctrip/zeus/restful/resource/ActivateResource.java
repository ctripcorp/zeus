package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.exceptions.SlbValidatorException;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.executor.TaskManager;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.model.task.OpsTask;
import com.ctrip.zeus.model.task.TaskResult;
import com.ctrip.zeus.model.task.TaskResultList;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.auth.AuthService;
import com.ctrip.zeus.service.auth.ResourceDataType;
import com.ctrip.zeus.service.auth.ResourceOperationType;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.message.queue.MessageQueue;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.common.ErrorType;
import com.ctrip.zeus.service.model.common.ValidationContext;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.task.constant.TaskOpsType;
import com.ctrip.zeus.service.validate.SlbValidator;
import com.ctrip.zeus.tag.PropertyBox;
import com.ctrip.zeus.util.AssertUtils;
import com.ctrip.zeus.util.MessageUtil;
import com.ctrip.zeus.util.StringUtils;
import com.ctrip.zeus.util.UserUtils;
import com.google.common.base.Joiner;
import com.netflix.config.DynamicLongProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * Created by fanqq on 2015/3/20.
 */

@Component
@Path("/activate")
public class ActivateResource {
    @Resource
    private PropertyBox propertyBox;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private SlbValidator slbValidator;
    @Resource
    private DrRepository drRepository;
    @Resource
    private EntityFactory entityFactory;
    @Resource
    private TaskManager taskManager;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private MessageQueue messageQueue;
    @Resource
    private AuthService authService;
    @Resource
    private ValidationFacade validationFacade;
    @Resource
    private ConfigHandler configHandler;

    private static DynamicLongProperty apiTimeout = DynamicPropertyFactory.getInstance().getLongProperty("api.timeout", 30000L);

    /**
     * @api {post} /api/activate/slb: [Read] Activate Slb
     * @apiDescription Activate Slb by slbId
     * @apiName Activate Slb
     * @apiGroup Activate
     * @apiSuccess (Success 200) {String} message   success message
     * @apiParam (Parameter) {Integer[]} [slbId]           Slb ids to be activated
     * @apiParam (Parameter) {String[]} [slbName]          Slb names to be activated
     */
    @GET
    @Path("/slb")
    public Response activateSlb(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("slbId") List<Long> slbIds, @QueryParam("slbName") List<String> slbNames) throws Exception {
        List<Long> tmpSlbIds = new ArrayList<>();
        SlbValidateResponse validateResponse = null;
        if (slbIds != null && !slbIds.isEmpty()) {
            tmpSlbIds.addAll(slbIds);
        }
        if (slbNames != null && !slbNames.isEmpty()) {
            for (String slbName : slbNames) {
                tmpSlbIds.add(slbCriteriaQuery.queryByName(slbName));
            }
        }
        Long[] slbIdsArray = tmpSlbIds.toArray(new Long[tmpSlbIds.size()]);
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.ACTIVATE, ResourceDataType.Slb, tmpSlbIds.toArray(slbIdsArray));

        ModelStatusMapping<Slb> slbModelStatusMapping = entityFactory.getSlbsByIds(slbIdsArray);
        if (slbModelStatusMapping.getOfflineMapping() == null || slbModelStatusMapping.getOfflineMapping().size() == 0) {
            throw new ValidationException("Not Found Slb By Id." + "[[SlbIds=" + tmpSlbIds + "]]");
        }

        for (Long id : tmpSlbIds) {
            if (slbModelStatusMapping.getOfflineMapping().get(id) == null) {
                throw new ValidationException("Not Found Slb By Id." + id);
            }
            validateResponse = slbValidator.validate(slbModelStatusMapping.getOfflineMapping().get(id));
            if (!validateResponse.getSucceed()) {
                throw new SlbValidatorException("msg:" + validateResponse.getMsg() + "\nslbId:" + validateResponse.getSlbId()
                        + "\nip:" + validateResponse.getIp());
            }
        }
        List<OpsTask> tasks = new ArrayList<>();
        for (Long id : tmpSlbIds) {
            Slb slb = slbModelStatusMapping.getOfflineMapping().get(id);
            OpsTask task = new OpsTask();
            task.setSlbId(id);
            task.setOpsType(TaskOpsType.ACTIVATE_SLB);
            task.setTargetSlbId(id);
            task.setVersion(slb.getVersion());
            tasks.add(task);
        }

        List<Long> taskIds = taskManager.addTask(tasks);
        List<TaskResult> results = taskManager.getResult(taskIds, apiTimeout.get());

        TaskResultList resultList = new TaskResultList();
        for (TaskResult t : results) {
            resultList.addTaskResult(t);
        }
        resultList.setTotal(results.size());

        try {
            propertyBox.set("status", "activated", "slb", tmpSlbIds.toArray(new Long[tmpSlbIds.size()]));
        } catch (Exception ex) {
        }
        List<Slb> slbList = new ArrayList<>();
        slbList.addAll(slbModelStatusMapping.getOfflineMapping().values());
        slbList.addAll(slbModelStatusMapping.getOnlineMapping().values());
        String slbMessageData = MessageUtil.getMessageData(request, null, null, null,
                slbList.toArray(new Slb[]{}), null, true);

        for (Long slbId : tmpSlbIds) {
            messageQueue.produceMessage(request.getRequestURI(), slbId, slbMessageData);
        }
        return responseHandler.handle(resultList, hh.getMediaType());
    }


    /**
     * @api {post} /api/activate/group: [Read] Activate group
     * @apiDescription Activate group by group id or name
     * @apiName Activate Group
     * @apiGroup Activate
     * @apiSuccess (Success 200) {String} message   success message
     * @apiParam (Parameter) {Integer[]} [groupId]         Group ids to be activated
     * @apiParam (Parameter) {String[]} [groupName]        Group names to be activated
     * @apiParam (Parameter) {boolean} [force]             Skip validate or not
     */
    @GET
    @Path("/group")
    public Response activateGroup(@Context HttpServletRequest request, @Context HttpHeaders hh,
                                  @QueryParam("groupId") List<Long> groupIds,
                                  @QueryParam("force") Boolean force,
                                  @QueryParam("canary") Boolean canary,
                                  @QueryParam("groupName") List<String> groupNames) throws Exception {
        Set<Long> tmpGroupIds = new HashSet<>();
        if (groupIds != null && !groupIds.isEmpty()) {
            tmpGroupIds.addAll(groupIds);
        }
        if (groupNames != null && !groupNames.isEmpty()) {
            for (String groupName : groupNames) {
                tmpGroupIds.add(groupCriteriaQuery.queryByName(groupName));
            }
        }
        Long[] groupIdsArray = tmpGroupIds.toArray(new Long[tmpGroupIds.size()]);
        if (configHandler.getEnable("auth.with.force", false) && force != null && force) {
            authService.authValidateWithForce(UserUtils.getUserName(request), ResourceOperationType.ACTIVATE, ResourceDataType.Group, groupIdsArray);
        } else {
            authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.ACTIVATE, ResourceDataType.Group, groupIdsArray);
        }
        ModelStatusMapping<Group> groupMap = entityFactory.getGroupsByIds(groupIdsArray);
        tmpGroupIds.removeAll(groupMap.getOfflineMapping().keySet());
        if (tmpGroupIds.size() > 0) {
            throw new ValidationException("Groups with id (" + Joiner.on(",").join(tmpGroupIds) + ") are not found.");
        }
        Set<Long> vsIds = getVsIdsForActivateGroup(groupMap);
        ModelStatusMapping<VirtualServer> vsMap = entityFactory.getVsesByIds(vsIds.toArray(new Long[]{}));
        List<OpsTask> tasks = new ArrayList<>();
        for (Map.Entry<Long, Group> e : groupMap.getOfflineMapping().entrySet()) {
            Group offlineVersion = groupMap.getOfflineMapping().get(e.getKey());

            Set<Long> offlineRelatedVsIds = new HashSet<>();
            Map<Long, GroupVirtualServer> offlineGvs = new HashMap<>();
            for (GroupVirtualServer gvs : offlineVersion.getGroupVirtualServers()) {
                VirtualServer vs = vsMap.getOnlineMapping().get(gvs.getVirtualServer().getId());
                if (vs == null) {
                    throw new ValidationException("Virtual server " + gvs.getVirtualServer().getId() + " is found deactivated.");
                }
                offlineRelatedVsIds.add(vs.getId());
                offlineGvs.put(gvs.getVirtualServer().getId(), gvs);
            }
            Set<Long> onlineRelatedVsIds = new HashSet<>();
            Map<Long, GroupVirtualServer> onlineGvs = new HashMap<>();
            Group onlineVersion = groupMap.getOnlineMapping().get(e.getKey());
            if (onlineVersion != null) {
                for (GroupVirtualServer gvs : onlineVersion.getGroupVirtualServers()) {
                    VirtualServer vs = vsMap.getOnlineMapping().get(gvs.getVirtualServer().getId());
                    if (vs == null) {
                        throw new Exception("Virtual server " + gvs.getVirtualServer().getId() + " is found deactivated of an online group. GroupId=" + e.getKey() + ".");
                    }
                    onlineRelatedVsIds.add(gvs.getVirtualServer().getId());
                    onlineGvs.put(gvs.getVirtualServer().getId(), gvs);
                }
            }

            if (force == null || !force) {
                //PreCheck For Path Overlap.
                if (!configHandler.getEnable("ignore.path.overlap", false)) {
                    Map<Long, ValidationContext> result = validationFacade.validateGroupPathOverlap(offlineVersion, offlineRelatedVsIds.toArray(new Long[offlineRelatedVsIds.size()]));
                    for (Long vsId : result.keySet()) {
                        if (result.get(vsId).shouldProceed()) {
                            continue;
                        }
                        if (!result.get(vsId).getGroupErrorType(offlineVersion.getId()).contains(ErrorType.PATH_OVERLAP)) {
                            continue;
                        }
                        if (!onlineGvs.containsKey(vsId)) {
                            throw new ValidationException("New Group Virtual Server Has Path Overlap.Need Force Activate.VsId:" + vsId);
                        }
                        if (!StringUtils.equalsIgnoreCase(onlineGvs.get(vsId).getPath(), offlineGvs.get(vsId).getPath())
                                || !onlineGvs.get(vsId).getPriority().equals(offlineGvs.get(vsId).getPriority())) {
                            throw new ValidationException("Path Overlap Because of Changed Group Virtual Server Path Or Priority.VsId:" + vsId);
                        }
                    }
                }
            }

            Set<Long> tmp = new HashSet<>();
            tmp.addAll(offlineRelatedVsIds);
            tmp.addAll(onlineRelatedVsIds);
            if (canary != null && canary) {
                if (!(offlineRelatedVsIds.containsAll(onlineRelatedVsIds) && offlineRelatedVsIds.size() == onlineRelatedVsIds.size())) {
                    throw new ValidationException("Canary Version Should Has Same Gvses.");
                }
                for (Long vid : offlineRelatedVsIds){
                    if (!offlineGvs.get(vid).getPath().equalsIgnoreCase(onlineGvs.get(vid).getPath())
                            || !offlineGvs.get(vid).getPriority().equals(onlineGvs.get(vid).getPriority()) ){
                        throw new ValidationException("Canary Activate Should Not Change Path And Priority.");
                    }
                }
            }
            Map<Long, OpsTask> activateTasks = new HashMap<>();
            for (Long vsId : tmp) {
                VirtualServer vs = vsMap.getOnlineMapping().get(vsId);
                if (onlineRelatedVsIds.contains(vsId) && !offlineRelatedVsIds.contains(vsId)) {
                    for (Long slbId : vs.getSlbIds()) {
                        OpsTask task = new OpsTask();
                        task.setGroupId(e.getKey())
                                .setTargetSlbId(slbId)
                                .setSlbVirtualServerId(vsId)
                                .setOpsType(TaskOpsType.SOFT_DEACTIVATE_GROUP)
                                .setVersion(offlineVersion.getVersion())
                                .setSkipValidate(force == null ? false : force)
                                .setCreateTime(new Date());
                        tasks.add(task);
                    }
                } else {
                    for (Long slbId : vs.getSlbIds()) {
                        if (activateTasks.get(slbId) == null) {
                            OpsTask task = new OpsTask();
                            task.setGroupId(e.getKey())
                                    .setTargetSlbId(slbId)
                                    .setOpsType(TaskOpsType.ACTIVATE_GROUP)
                                    .setVersion(offlineVersion.getVersion())
                                    .setSkipValidate(force == null ? false : force)
                                    .setCreateTime(new Date());
                            if (canary != null && canary) {
                                task.setOpsType(TaskOpsType.ACTIVATE_GROUP_CANARY);
                            }
                            activateTasks.put(slbId, task);
                        }
                    }
                }
            }
            tasks.addAll(activateTasks.values());
        }
        List<Long> taskIds = taskManager.addAggTask(tasks);
        List<TaskResult> results = taskManager.getAggResult(taskIds, apiTimeout.get());
        TaskResultList resultList = new TaskResultList();
        for (TaskResult t : results) {
            resultList.addTaskResult(t);
        }
        resultList.setTotal(results.size());
        try {
            propertyBox.set("status", "activated", "group", groupMap.getOfflineMapping().keySet().toArray(new Long[groupMap.getOfflineMapping().size()]));
        } catch (Exception ex) {
        }


        for (Long id : groupMap.getOfflineMapping().keySet()) {
            List<Group> groupList = new ArrayList<>();
            groupList.add(groupMap.getOfflineMapping().get(id));
            if (groupMap.getOnlineMapping().get(id) != null) {
                groupList.add(groupMap.getOnlineMapping().get(id));
            }
            String slbMessageData = MessageUtil.getMessageData(request,
                    groupList.toArray(new Group[groupList.size()]), null, null, null, null, true);
            messageQueue.produceMessage(request.getRequestURI(), id, slbMessageData);
        }
        return responseHandler.handle(resultList, hh.getMediaType());
    }

    private Set<Long> getVsIdsForActivateGroup(ModelStatusMapping<Group> groupMap) throws Exception {
        Set<Long> vsIds = new HashSet<>();
        for (Map.Entry<Long, Group> e : groupMap.getOfflineMapping().entrySet()) {
            Group group = e.getValue();
            if (group == null) {
                throw new Exception("Unexpected offline group with null value. groupId=" + e.getKey() + ".");
            }
            AssertUtils.assertNotNull(group.getGroupVirtualServers(), "Group with id " + e.getKey() + " does not have any related virtual server.");
            for (GroupVirtualServer vs : group.getGroupVirtualServers()) {
                vsIds.add(vs.getVirtualServer().getId());
            }
        }
        for (Map.Entry<Long, Group> e : groupMap.getOnlineMapping().entrySet()) {
            Group group = e.getValue();
            if (group == null) {
                throw new Exception("Unexpected online group with null value. groupId=" + e.getKey() + ".");
            }
            AssertUtils.assertNotNull(group.getGroupVirtualServers(), "Group with id " + e.getKey() + " does not have any related virtual server.");
            for (GroupVirtualServer vs : group.getGroupVirtualServers()) {
                vsIds.add(vs.getVirtualServer().getId());
            }
        }
        return vsIds;
    }

    /**
     * @api {post} /api/activate/vs: [Read] Activate vs
     * @apiDescription Activate vs by vs id
     * @apiName Activate VS
     * @apiGroup Activate
     * @apiSuccess (Success 200) {String} message   success message
     * @apiParam (Parameter) {Integer[]} [vsId]         Vs ids to be activated
     */
    @GET
    @Path("/vs")
    public Response activateVirtualServer(@Context HttpServletRequest request,
                                          @Context HttpHeaders hh,
                                          @QueryParam("vsId") List<Long> vsIds) throws Exception {
        Long[] ids = vsIds.toArray(new Long[vsIds.size()]);
        ModelStatusMapping<VirtualServer> vsMap = entityFactory.getVsesByIds(ids);
        for (Long vsId : ids) {
            VirtualServer offlineVersion = vsMap.getOfflineMapping().get(vsId);
            if (offlineVersion == null) {
                throw new ValidationException("Cannot find vs by id " + vsId + ".");
            }
        }
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.ACTIVATE, ResourceDataType.Vs, ids);
        TaskResultList resultList = new TaskResultList();
        List<Long> taskIds = new ArrayList<>();
        for (Long vsId : ids) {
            VirtualServer offlineVersion = vsMap.getOfflineMapping().get(vsId);
            Set<Long> offlineRelatedSlbIds = new HashSet<>();
            offlineRelatedSlbIds.addAll(offlineVersion.getSlbIds());

            Set<Long> onlineRelatedSlbIds = new HashSet<>();
            VirtualServer onlineVersion = vsMap.getOnlineMapping().get(vsId);
            if (onlineVersion != null) {
                onlineRelatedSlbIds.addAll(onlineVersion.getSlbIds());
            }
            Set<Long> tmp = new HashSet<>();
            tmp.addAll(offlineRelatedSlbIds);
            tmp.addAll(onlineRelatedSlbIds);
            ModelStatusMapping<Slb> slbMap = entityFactory.getSlbsByIds(tmp.toArray(new Long[tmp.size()]));
            List<OpsTask> tasks = new ArrayList<>();
            for (Long slbId : tmp) {
                Slb slb = slbMap.getOnlineMapping().get(slbId);
                if (slb == null) {
                    if (offlineRelatedSlbIds.contains(slbId)) {
                        throw new ValidationException("Slb " + slbId + " is found deactivated.");
                    } else {
                        throw new Exception("Slb " + slbId + " is found deactivated of an online vs. VsId=" + vsId);
                    }
                }
                if (onlineRelatedSlbIds.contains(slbId) && !offlineRelatedSlbIds.contains(slbId)) {
                    OpsTask task = new OpsTask();
                    task.setSlbVirtualServerId(vsId)
                            .setTargetSlbId(slbId)
                            .setVersion(offlineVersion.getVersion())
                            .setOpsType(TaskOpsType.SOFT_DEACTIVATE_VS)
                            .setCreateTime(new Date());
                    tasks.add(task);
                } else {
                    OpsTask task = new OpsTask();
                    task.setSlbVirtualServerId(vsId)
                            .setTargetSlbId(slbId)
                            .setVersion(offlineVersion.getVersion())
                            .setOpsType(TaskOpsType.ACTIVATE_VS)
                            .setCreateTime(new Date());
                    tasks.add(task);
                }
            }
            taskIds.addAll(taskManager.addTask(tasks));
        }
        List<TaskResult> results = taskManager.getResult(taskIds, apiTimeout.get());
        for (TaskResult t : results) {
            resultList.addTaskResult(t);
        }
        resultList.setTotal(resultList.getTaskResults().size());
        try {
            propertyBox.set("status", "activated", "vs", ids);
        } catch (Exception ex) {
        }
        for (Long vsId : vsIds) {
            List<VirtualServer> vsList = new ArrayList<>();
            vsList.add(vsMap.getOfflineMapping().get(vsId));
            if (vsMap.getOnlineMapping().get(vsId) != null) {
                vsList.add(vsMap.getOnlineMapping().get(vsId));
            }
            String slbMessageData = MessageUtil.getMessageData(request, null, null,
                    vsList.toArray(new VirtualServer[vsList.size()]), null, null, true);
            messageQueue.produceMessage(request.getRequestURI(), vsId, slbMessageData);
        }
        return responseHandler.handle(resultList, hh.getMediaType());
    }

    /**
     * @api {post} /api/activate/policy: [Read] Activate policy
     * @apiDescription Activate policy by policy id
     * @apiName Activate Policy
     * @apiGroup Activate
     * @apiSuccess (Success 200) {String} message   success message
     * @apiParam (Parameter) {Integer[]} [policyId]        Policy ids to be activated
     * @apiParam (Parameter) {boolean} [force]             Skip validate or not
     */
    @GET
    @Path("/policy")
    public Response activatePolicy(@Context HttpServletRequest request,
                                   @Context HttpHeaders hh,
                                   @QueryParam("policyId") Long policyId,
                                   @QueryParam("force") Boolean force) throws Exception {
        if (policyId == null || policyId <= 0) {
            throw new ValidationException("Invalidate Parameter policy.");
        }
        ModelStatusMapping<TrafficPolicy> trafficPolicyMap = entityFactory.getPoliciesByIds(new Long[]{policyId});
        if (trafficPolicyMap.getOfflineMapping().size() == 0) {
            throw new ValidationException("Not Found Policy By Id. Policy Id:" + policyId);
        }
        if (force != null && force) {
            authService.authValidateWithForce(UserUtils.getUserName(request), ResourceOperationType.ACTIVATE, ResourceDataType.Policy, policyId);
        } else {
            authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.ACTIVATE, ResourceDataType.Policy, policyId);
        }
        TrafficPolicy toActivateObj = trafficPolicyMap.getOfflineMapping().get(policyId);
        TrafficPolicy activatedObj = trafficPolicyMap.getOnlineMapping().get(policyId);
        Set<Long> vsIds = new HashSet<>();
        Set<Long> offlineRelatedVsIds = new HashSet<>();
        Set<Long> onlineRelatedVsIds = new HashSet<>();
        for (PolicyVirtualServer pvs : toActivateObj.getPolicyVirtualServers()) {
            offlineRelatedVsIds.add(pvs.getVirtualServer().getId());
        }
        if (activatedObj != null) {
            for (PolicyVirtualServer pvs : activatedObj.getPolicyVirtualServers()) {
                onlineRelatedVsIds.add(pvs.getVirtualServer().getId());
            }
        }
        vsIds.addAll(offlineRelatedVsIds);
        vsIds.addAll(onlineRelatedVsIds);
        ModelStatusMapping<VirtualServer> vsMap = entityFactory.getVsesByIds(vsIds.toArray(new Long[]{}));
        for (PolicyVirtualServer pvs : toActivateObj.getPolicyVirtualServers()) {
            if (!vsMap.getOnlineMapping().containsKey(pvs.getVirtualServer().getId())) {
                throw new ValidationException("Virtual server " + pvs.getVirtualServer().getId() + " is not activated. PolicyId :" + policyId);
            }
        }
        List<OpsTask> tasks = new ArrayList<>();
        Map<Long, OpsTask> activateTasks = new HashMap<>();
        for (Long vsId : vsIds) {
            VirtualServer vs = vsMap.getOnlineMapping().get(vsId);
            if (onlineRelatedVsIds.contains(vsId) && !offlineRelatedVsIds.contains(vsId)) {
                for (Long slbId : vs.getSlbIds()) {
                    OpsTask task = new OpsTask();
                    task.setPolicyId(policyId)
                            .setTargetSlbId(slbId)
                            .setSlbVirtualServerId(vsId)
                            .setOpsType(TaskOpsType.SOFT_DEACTIVATE_POLICY)
                            .setVersion(toActivateObj.getVersion())
                            .setSkipValidate(force == null ? false : force)
                            .setCreateTime(new Date());
                    tasks.add(task);
                }
            } else {
                for (Long slbId : vs.getSlbIds()) {
                    if (activateTasks.get(slbId) == null) {
                        OpsTask task = new OpsTask();
                        task.setPolicyId(policyId)
                                .setTargetSlbId(slbId)
                                .setOpsType(TaskOpsType.ACTIVATE_POLICY)
                                .setVersion(toActivateObj.getVersion())
                                .setSkipValidate(force == null ? false : force)
                                .setCreateTime(new Date());
                        activateTasks.put(slbId, task);
                    }
                }
            }
        }
        tasks.addAll(activateTasks.values());
        List<Long> taskIds = taskManager.addTask(tasks);
        List<TaskResult> results = taskManager.getResult(taskIds, apiTimeout.get());
        TaskResultList resultList = new TaskResultList();
        for (TaskResult t : results) {
            resultList.addTaskResult(t);
        }
        resultList.setTotal(results.size());
        try {
            propertyBox.set("status", "activated", "policy", policyId);
        } catch (Exception ex) {
        }
        List<TrafficPolicy> policies = new ArrayList<>();
        policies.addAll(trafficPolicyMap.getOfflineMapping().values());
        policies.addAll(trafficPolicyMap.getOnlineMapping().values());
        String slbMessageData = MessageUtil.getMessageData(request, null, policies.toArray(new TrafficPolicy[policies.size()]),
                null, null, null, true);
        messageQueue.produceMessage(request.getRequestURI(), policyId, slbMessageData);
        return responseHandler.handle(resultList, hh.getMediaType());
    }

    @GET
    @Path("/dr")
    public Response activateDr(@Context HttpServletRequest request,
                               @Context HttpHeaders hh,
                               @QueryParam("drId") Long drId,
                               @QueryParam("force") Boolean force) throws Exception {
        if (drId == null || drId <= 0) {
            throw new ValidationException("Invalidate Parameter Dr.");
        }
        ModelStatusMapping<Dr> drMap = entityFactory.getDrsByIds(new Long[]{drId});
        if (drMap.getOfflineMapping().size() == 0) {
            throw new ValidationException("Not Found Dr By Id. Dr Id:" + drId);
        }
        if (force != null && force) {
            authService.authValidateWithForce(UserUtils.getUserName(request), ResourceOperationType.ACTIVATE, ResourceDataType.Dr, drId);
        } else {
            authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.ACTIVATE, ResourceDataType.Dr, drId);
        }
        Dr toActivateObj = drMap.getOfflineMapping().get(drId);
        Dr activatedObj = drMap.getOnlineMapping().get(drId);

        Set<Long> groupIds = new HashSet<>();
        Set<Long> vsIds = new HashSet<>();

        Set<Long> offlineRelatedVsIds = new HashSet<>();
        Set<Long> onlineRelatedVsIds = new HashSet<>();

        for (DrTraffic traffic : toActivateObj.getDrTraffics()) {
            groupIds.add(traffic.getGroup().getId());
            for (Destination des : traffic.getDestinations()) {
                offlineRelatedVsIds.add(des.getVirtualServer().getId());
            }
        }
        if (activatedObj != null) {
            for (DrTraffic traffic : activatedObj.getDrTraffics()) {
                groupIds.add(traffic.getGroup().getId());
                for (Destination des : traffic.getDestinations()) {
                    onlineRelatedVsIds.add(des.getVirtualServer().getId());
                }
            }
        }
        vsIds.addAll(offlineRelatedVsIds);
        vsIds.addAll(onlineRelatedVsIds);

        ModelStatusMapping<Group> groupMap = entityFactory.getGroupsByIds(groupIds.toArray(new Long[]{}));
        for (DrTraffic traffic : toActivateObj.getDrTraffics()) {
            if (!groupMap.getOnlineMapping().containsKey(traffic.getGroup().getId())) {
                throw new ValidationException("Group " + traffic.getGroup().getId() + " is not activated. DrId :" + drId);
            }
        }

        ModelStatusMapping<VirtualServer> vsMap = entityFactory.getVsesByIds(vsIds.toArray(new Long[]{}));
        for (DrTraffic traffic : toActivateObj.getDrTraffics()) {
            for (Destination des : traffic.getDestinations()) {
                if (!vsMap.getOnlineMapping().containsKey(des.getVirtualServer().getId())) {
                    throw new ValidationException("Virtual Server " + des.getVirtualServer().getId() + " is not activated. DrId :" + drId);
                }
            }
        }

        List<OpsTask> tasks = new ArrayList<>();
        Map<Long, OpsTask> activateTasks = new HashMap<>();
        for (Long vsId : vsIds) {
            VirtualServer vs = vsMap.getOnlineMapping().get(vsId);
            if (onlineRelatedVsIds.contains(vsId) && !offlineRelatedVsIds.contains(vsId)) {
                for (Long slbId : vs.getSlbIds()) {
                    OpsTask task = new OpsTask();
                    task.setDrId(drId)
                            .setTargetSlbId(slbId)
                            .setSlbVirtualServerId(vsId)
                            .setOpsType(TaskOpsType.SOFT_DEACTIVATE_DR)
                            .setVersion(toActivateObj.getVersion())
                            .setSkipValidate(force == null ? false : force)
                            .setCreateTime(new Date());
                    tasks.add(task);
                }
            } else {
                for (Long slbId : vs.getSlbIds()) {
                    if (activateTasks.get(slbId) == null) {
                        OpsTask task = new OpsTask();
                        task.setDrId(drId)
                                .setTargetSlbId(slbId)
                                .setOpsType(TaskOpsType.ACTIVATE_DR)
                                .setVersion(toActivateObj.getVersion())
                                .setSkipValidate(force == null ? false : force)
                                .setCreateTime(new Date());
                        activateTasks.put(slbId, task);
                    }
                }
            }
        }

        tasks.addAll(activateTasks.values());

        TaskResultList resultList = new TaskResultList();
        //if there is no dr traffic
        if (tasks.size() == 0) {
            drRepository.updateActiveStatus(new IdVersion[]{new IdVersion(toActivateObj.getId(), toActivateObj.getVersion())});
        } else {
            List<Long> taskIds = taskManager.addTask(tasks);
            List<TaskResult> results = taskManager.getResult(taskIds, apiTimeout.get());
            for (TaskResult t : results) {
                resultList.addTaskResult(t);
            }
            resultList.setTotal(results.size());
        }
        try {
            propertyBox.set("status", "activated", "dr", drId);
        } catch (Exception ex) {
        }

        List<Dr> drs = new ArrayList<>();
        drs.addAll(drMap.getOfflineMapping().values());
        drs.addAll(drMap.getOnlineMapping().values());
        String slbMessageData = MessageUtil.getMessageData(request, null, null, drs.toArray(new Dr[drs.size()]), null, null, null, true);

        messageQueue.produceMessage(request.getRequestURI(), drId, slbMessageData);
        return responseHandler.handle(resultList, hh.getMediaType());
    }
}