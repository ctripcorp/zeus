package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.exceptions.SlbValidatorException;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.executor.TaskManager;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.message.queue.MessageQueue;
import com.ctrip.zeus.service.message.queue.MessageType;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.handler.GroupValidator;
import com.ctrip.zeus.service.model.handler.TrafficPolicyValidator;
import com.ctrip.zeus.service.model.handler.VirtualServerValidator;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.task.constant.TaskOpsType;
import com.ctrip.zeus.service.validate.SlbValidator;
import com.ctrip.zeus.tag.PropertyBox;
import com.ctrip.zeus.task.entity.OpsTask;
import com.ctrip.zeus.task.entity.TaskResult;
import com.ctrip.zeus.task.entity.TaskResultList;
import com.ctrip.zeus.util.AssertUtils;
import com.google.common.base.Joiner;
import com.ctrip.zeus.util.MessageUtil;
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
    private EntityFactory entityFactory;
    @Resource
    private TaskManager taskManager;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private GroupValidator groupValidator;
    @Resource
    private VirtualServerValidator virtualServerValidator;
    @Resource
    private com.ctrip.zeus.service.model.handler.SlbValidator slbModelValidator;
    @Resource
    private TrafficPolicyValidator trafficPolicyValidator;
    @Resource
    private TrafficPolicyRepository trafficPolicyRepository;
    @Resource
    private MessageQueue messageQueue;
    @Resource
    private ConfigHandler configHandler;

    private static DynamicLongProperty apiTimeout = DynamicPropertyFactory.getInstance().getLongProperty("api.timeout", 15000L);

    @GET
    @Path("/slb")
    @Authorize(name = "activate")
    public Response activateSlb(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("slbId") List<Long> slbIds, @QueryParam("slbName") List<String> slbNames) throws Exception {
        List<Long> _slbIds = new ArrayList<>();
        SlbValidateResponse validateResponse = null;
        if (slbIds != null && !slbIds.isEmpty()) {
            _slbIds.addAll(slbIds);
        }
        if (slbNames != null && !slbNames.isEmpty()) {
            for (String slbName : slbNames) {
                _slbIds.add(slbCriteriaQuery.queryByName(slbName));
            }
        }
        ModelStatusMapping<Slb> slbModelStatusMapping = entityFactory.getSlbsByIds(_slbIds.toArray(new Long[]{}));
        if (slbModelStatusMapping.getOfflineMapping() == null || slbModelStatusMapping.getOfflineMapping().size() == 0) {
            throw new ValidationException("Not Found Slb By Id.");
        }
        slbModelValidator.validateForActivate(slbModelStatusMapping.getOfflineMapping().values().toArray(new Slb[]{}), true);
        for (Long id : _slbIds) {
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
        for (Long id : _slbIds) {
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
            propertyBox.set("status", "activated", "slb", _slbIds.toArray(new Long[_slbIds.size()]));
        } catch (Exception ex) {
        }

        String slbMessageData = MessageUtil.getMessageData(request, null, null,
                slbModelStatusMapping.getOfflineMapping().values().toArray(new Slb[]{}), null, true);

        for (Long slbId : _slbIds) {
            if (configHandler.getEnable("use.new,message.queue.producer", false)) {
                messageQueue.produceMessage(request.getRequestURI(), slbId, slbMessageData);
            } else {
                messageQueue.produceMessage(MessageType.ActivateSlb, slbId, slbMessageData);
            }
        }
        return responseHandler.handle(resultList, hh.getMediaType());
    }

    @GET
    @Path("/group")
    @Authorize(name = "activate")
    public Response activateGroup(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("groupId") List<Long> groupIds, @QueryParam("groupName") List<String> groupNames) throws Exception {
        Set<Long> _groupIds = new HashSet<>();

        if (groupIds != null && !groupIds.isEmpty()) {
            _groupIds.addAll(groupIds);
        }
        if (groupNames != null && !groupNames.isEmpty()) {
            for (String groupName : groupNames) {
                _groupIds.add(groupCriteriaQuery.queryByName(groupName));
            }
        }

        ModelStatusMapping<Group> groupMap = entityFactory.getGroupsByIds(_groupIds.toArray(new Long[_groupIds.size()]));

        _groupIds.removeAll(groupMap.getOfflineMapping().keySet());
        if (_groupIds.size() > 0) {
            throw new ValidationException("Groups with id (" + Joiner.on(",").join(_groupIds) + ") are not found.");
        }

        groupValidator.validateForActivate(groupMap.getOfflineMapping().values().toArray(new Group[]{}), false);

        // Fetch all related vs entities first to reduce IO
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
        ModelStatusMapping<VirtualServer> vsMap = entityFactory.getVsesByIds(vsIds.toArray(new Long[]{}));


        List<OpsTask> tasks = new ArrayList<>();
        for (Map.Entry<Long, Group> e : groupMap.getOfflineMapping().entrySet()) {
            Group offlineVersion = groupMap.getOfflineMapping().get(e.getKey());

            Set<Long> offlineRelatedVsIds = new HashSet<>();
            for (GroupVirtualServer gvs : offlineVersion.getGroupVirtualServers()) {
                VirtualServer vs = vsMap.getOnlineMapping().get(gvs.getVirtualServer().getId());
                if (vs == null) {
                    throw new ValidationException("Virtual server " + gvs.getVirtualServer().getId() + " is found deactivated.");
                }
                offlineRelatedVsIds.add(vs.getId());
            }

            Set<Long> onlineRelatedVsIds = new HashSet<>();
            Group onlineVersion = groupMap.getOnlineMapping().get(e.getKey());
            if (onlineVersion != null) {
                for (GroupVirtualServer gvs : onlineVersion.getGroupVirtualServers()) {
                    VirtualServer vs = vsMap.getOnlineMapping().get(gvs.getVirtualServer().getId());
                    if (vs == null) {
                        throw new Exception("Virtual server " + gvs.getVirtualServer().getId() + " is found deactivated of an online group. GroupId=" + e.getKey() + ".");
                    }
                    onlineRelatedVsIds.add(gvs.getVirtualServer().getId());
                }
            }

            Set<Long> tmp = new HashSet<>();
            tmp.addAll(offlineRelatedVsIds);
            tmp.addAll(onlineRelatedVsIds);

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
                                    .setCreateTime(new Date());
                            activateTasks.put(slbId, task);
                        }
                    }
                }
            }
            tasks.addAll(activateTasks.values());
        }
        List<Long> taskIds = taskManager.addTask(tasks);

        List<TaskResult> results = taskManager.getResult(taskIds, apiTimeout.get());

        TaskResultList resultList = new TaskResultList();
        for (TaskResult t : results) {
            resultList.addTaskResult(t);
        }
        resultList.setTotal(results.size());

        try {
            propertyBox.set("status", "activated", "group", groupMap.getOfflineMapping().keySet().toArray(new Long[groupMap.getOfflineMapping().size()]));
        } catch (Exception ex) {
        }

        String slbMessageData = MessageUtil.getMessageData(request,
                groupMap.getOfflineMapping().values().toArray(new Group[groupMap.getOfflineMapping().size()]), null, null, null, true);
        for (Long id : groupMap.getOfflineMapping().keySet()) {
            if (configHandler.getEnable("use.new,message.queue.producer", false)) {
                messageQueue.produceMessage(request.getRequestURI(), id, slbMessageData);
            } else {
                messageQueue.produceMessage(MessageType.ActivateGroup, id, slbMessageData);
            }
        }

        return responseHandler.handle(resultList, hh.getMediaType());
    }

    @GET
    @Path("/vs")
    @Authorize(name = "activate")
    public Response activateVirtualServer(@Context HttpServletRequest request,
                                          @Context HttpHeaders hh,
                                          @QueryParam("vsId") Long vsId) throws Exception {
        ModelStatusMapping<VirtualServer> vsMap = entityFactory.getVsesByIds(new Long[]{vsId});
        VirtualServer offlineVersion = vsMap.getOfflineMapping().get(vsId);
        if (offlineVersion == null) {
            throw new ValidationException("Cannot find vs by id " + vsId + ".");
        }

        virtualServerValidator.validateForActivate(vsMap.getOfflineMapping().values().toArray(new VirtualServer[]{}), true);

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

        List<Long> taskIds = taskManager.addTask(tasks);

        List<TaskResult> results = taskManager.getResult(taskIds, apiTimeout.get());

        TaskResultList resultList = new TaskResultList();
        for (TaskResult t : results) {
            resultList.addTaskResult(t);
        }
        resultList.setTotal(results.size());

        try {
            propertyBox.set("status", "activated", "vs", vsId);
        } catch (Exception ex) {
        }

        String slbMessageData = MessageUtil.getMessageData(request, null,
                new VirtualServer[]{vsMap.getOfflineMapping().get(vsId)}, null, null, true);
        if (configHandler.getEnable("use.new,message.queue.producer", false)) {
            messageQueue.produceMessage(request.getRequestURI(), vsId, slbMessageData);
        } else {
            messageQueue.produceMessage(MessageType.ActivateVs, vsId, slbMessageData);
        }

        return responseHandler.handle(resultList, hh.getMediaType());
    }

    @GET
    @Path("/policy")
    @Authorize(name = "activate")
    public Response activatePolicy(@Context HttpServletRequest request,
                                   @Context HttpHeaders hh,
                                   @QueryParam("policy") Long policyId) throws Exception {
        if (policyId == null || policyId <= 0) {
            throw new ValidationException("Invalidate Parameter policy.");
        }
        ModelStatusMapping<TrafficPolicy> trafficPolicyMap = entityFactory.getPoliciesByIds(new Long[]{policyId});
        if (trafficPolicyMap.getOfflineMapping().size() == 0) {
            throw new ValidationException("Not Found Policy By Id. Policy Id:" + policyId);
        }
        trafficPolicyValidator.validateForActivate(trafficPolicyMap.getOfflineMapping().values().toArray(new TrafficPolicy[]{}), true);

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

        String slbMessageData = MessageUtil.getMessageData(request, null, null, null, null, true);
        messageQueue.produceMessage(request.getRequestURI(), policyId, slbMessageData);

        return responseHandler.handle(resultList, hh.getMediaType());
    }
}