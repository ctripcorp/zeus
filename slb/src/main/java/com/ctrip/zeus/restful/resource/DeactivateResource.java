package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.domain.GroupType;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.executor.TaskManager;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.model.task.OpsTask;
import com.ctrip.zeus.model.task.TaskResult;
import com.ctrip.zeus.model.task.TaskResultList;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.restful.message.view.ExtendedView;
import com.ctrip.zeus.restful.message.view.ViewDecorator;
import com.ctrip.zeus.service.auth.AuthService;
import com.ctrip.zeus.service.auth.ResourceDataType;
import com.ctrip.zeus.service.auth.ResourceOperationType;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.message.queue.MessageQueue;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.validation.DefaultTrafficPolicyValidator;
import com.ctrip.zeus.service.model.validation.DefaultVirtualServerValidator;
import com.ctrip.zeus.service.model.validation.TrafficPolicyValidator;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.TrafficPolicyQuery;
import com.ctrip.zeus.service.task.constant.TaskOpsType;
import com.ctrip.zeus.tag.PropertyBox;
import com.ctrip.zeus.tag.PropertyNames;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.tag.TagBox;
import com.ctrip.zeus.util.DateFormatUtils;
import com.ctrip.zeus.util.MessageUtil;
import com.ctrip.zeus.util.UserUtils;
import com.google.common.base.Joiner;
import com.netflix.config.DynamicLongProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.util.*;
/**
 * Created by fanqq on 2015/6/11.
 */

@Component
@Path("/deactivate")
public class DeactivateResource {
    @Resource
    private PropertyBox propertyBox;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private DefaultVirtualServerValidator virtualServerModelValidator;
    @Resource
    private TaskManager taskManager;
    @Resource
    private EntityFactory entityFactory;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private AuthService authService;
    @Resource
    private MessageQueue messageQueue;
    @Resource
    private ConfigHandler configHandler;
    @Resource
    private TrafficPolicyRepository trafficPolicyRepository;
    @Resource
    private TrafficPolicyQuery trafficPolicyQuery;
    @Resource
    private TrafficPolicyValidator trafficPolicyValidator;
    @Resource
    private DbLockFactory dbLockFactory;
    @Autowired
    private GroupRepository groupRepository;
    @Resource
    private PropertyService propertyService;

    private static DynamicLongProperty apiTimeout = DynamicPropertyFactory.getInstance().getLongProperty("api.timeout", 30000L);

    @GET
    @Path("/group")
    public Response deactivateGroup(@Context HttpServletRequest request,
                                    @Context HttpHeaders hh,
                                    @QueryParam("groupId") List<Long> groupIds,
                                    @QueryParam("groupName") List<String> groupNames) throws Exception {
        Set<Long> tmpGroupIds = new HashSet<>();
        if (groupIds != null && !groupIds.isEmpty()) {
            tmpGroupIds.addAll(groupIds);
        }
        if (groupNames != null && !groupNames.isEmpty()) {
            for (String groupName : groupNames) {
                Long groupId = groupCriteriaQuery.queryByName(groupName);
                if (groupId != null && !groupId.equals(0L)) {
                    tmpGroupIds.add(groupId);
                }
            }
        }
        Long[] groupIdsArray = tmpGroupIds.toArray(new Long[tmpGroupIds.size()]);
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.DEACTIVATE, ResourceDataType.Group, groupIdsArray);
        ModelStatusMapping<Group> groupMap = entityFactory.getGroupsByIds(groupIdsArray);
        tmpGroupIds.removeAll(groupMap.getOnlineMapping().keySet());
        if (tmpGroupIds.size() == groupIdsArray.length) {
            responseHandler.handle("All Groups are deactivated.", hh.getMediaType());
        } else if (tmpGroupIds.size() > 0) {
            throw new ValidationException("Groups with id (" + Joiner.on(",").join(tmpGroupIds) + ") are not activated.");
        }
        Set<Long> vsIds = new HashSet<>();
        for (Map.Entry<Long, Group> e : groupMap.getOnlineMapping().entrySet()) {
            Group group = e.getValue();
            if (group == null) {
                throw new Exception("Unexpected online group with null value. groupId=" + e.getKey() + ".");
            }
            for (GroupVirtualServer vs : group.getGroupVirtualServers()) {
                vsIds.add(vs.getVirtualServer().getId());
            }
        }
        ModelStatusMapping<VirtualServer> vsMap = entityFactory.getVsesByIds(vsIds.toArray(new Long[]{}));
        List<OpsTask> tasks = new ArrayList<>();
        Set<Long> policyIds = new HashSet<>();
        for (Map.Entry<Long, Group> e : groupMap.getOnlineMapping().entrySet()) {
            Group group = e.getValue();
            Set<Long> slbIds = new HashSet<>();
            for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
                VirtualServer vs = vsMap.getOnlineMapping().get(gvs.getVirtualServer().getId());
                if (vs == null) {
                    throw new ValidationException("Virtual server " + gvs.getVirtualServer().getId() + " is found deactivated.");
                }
                slbIds.addAll(vs.getSlbIds());
            }
            if (configHandler.getEnable("deactivate.related.policy", false)) {
                Set<IdVersion> idv = trafficPolicyQuery.queryByGroupId(group.getId());
                if (idv != null && idv.size() > 0) {
                    Long id = idv.iterator().next().getId();
                    ModelStatusMapping<TrafficPolicy> tpMap = entityFactory.getPoliciesByIds(new Long[]{id});
                    if (tpMap.getOnlineMapping().get(id) != null && tpMap.getOnlineMapping().get(id).getControls().size() == 2) {
                        for (TrafficControl trafficControl : tpMap.getOfflineMapping().get(id).getControls()) {
                            if (trafficControl.getGroup().getId().equals(group.getId()) && trafficControl.getWeight() <= 0) {
                                for (Long slbId : slbIds) {
                                    OpsTask task = new OpsTask();
                                    task.setPolicyId(id);
                                    task.setOpsType(TaskOpsType.DEACTIVATE_POLICY);
                                    task.setTargetSlbId(slbId);
                                    tasks.add(task);
                                }
                                policyIds.add(id);
                                break;
                            }
                        }
                    }
                }
            }
            for (Long slbId : slbIds) {
                OpsTask task = new OpsTask();
                task.setGroupId(e.getKey());
                task.setOpsType(TaskOpsType.DEACTIVATE_GROUP);
                task.setTargetSlbId(slbId);
                tasks.add(task);
            }
        }

        TaskResultList resultList = new TaskResultList();
        if (tasks.isEmpty()) {
            resultList.setTotal(0);
            return responseHandler.handle(resultList, hh.getMediaType());
        }
        List<Long> taskIds = taskManager.addAggTask(tasks);
        List<TaskResult> results = taskManager.getAggResult(taskIds, apiTimeout.get());

        for (TaskResult t : results) {
            resultList.addTaskResult(t);
        }
        resultList.setTotal(results.size());
        try {
            propertyBox.set("status", "deactivated", "group", groupMap.getOnlineMapping().keySet().toArray(new Long[groupMap.getOnlineMapping().size()]));
        } catch (Exception ex) {
        }
        setProperty("status", "deactivated", "policy", policyIds.toArray(new Long[policyIds.size()]));
        String slbMessageData = MessageUtil.getMessageData(request,
                groupMap.getOfflineMapping().values().toArray(new Group[groupMap.getOfflineMapping().size()]), null, null, null, null, true);
        for (Long id : groupMap.getOfflineMapping().keySet()) {
            messageQueue.produceMessage(request.getRequestURI(), id, slbMessageData);
        }
        return responseHandler.handle(resultList, hh.getMediaType());
    }

    private void setProperty(String pname, String pvalue, String type, Long[] policyIds) {
        try {
            if (policyIds.length > 0) {
                propertyBox.set(pname, pvalue, type, policyIds);
            }
        } catch (Exception ex) {
        }
    }

    @GET
    @Path("/vs")
    public Response deactivateVirtualServer(@Context HttpServletRequest request,
                                            @Context HttpHeaders hh,
                                            @QueryParam("vsId") Long vsId) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.DEACTIVATE, ResourceDataType.Vs, vsId);

        Set<IdVersion> relatedGroupIds = groupCriteriaQuery.queryByVsId(vsId);
        if (relatedGroupIds.size() > 0) {
            Set<Long> groupIds = new HashSet<>();
            for (IdVersion key : relatedGroupIds) {
                groupIds.add(key.getId());
            }
            relatedGroupIds.retainAll(groupCriteriaQuery.queryByIdsAndMode(groupIds.toArray(new Long[groupIds.size()]), SelectionMode.ONLINE_EXCLUSIVE));
            if (relatedGroupIds.size() > 0) {
                throw new ValidationException("Activated groups are found related to Vs[" + vsId + "].");
            }
        }

        ModelStatusMapping<VirtualServer> vsMap = entityFactory.getVsesByIds(new Long[]{vsId});
        if (vsMap.getOnlineMapping() == null || vsMap.getOnlineMapping().get(vsId) == null) {
            throw new ValidationException("Vs is not activated. VsId:" + vsId);
        }


        VirtualServer vs = vsMap.getOnlineMapping().get(vsId);
        virtualServerModelValidator.validateCanBeDeactive(vs);
        virtualServerModelValidator.validateCanBeDeactive(vsMap.getOnlineMapping().get(vsId));

        List<OpsTask> deactivatingTask = new ArrayList<>();
        for (Long slbId : vs.getSlbIds()) {
            OpsTask task = new OpsTask();
            task.setSlbVirtualServerId(vsId);
            task.setCreateTime(new Date());
            task.setOpsType(TaskOpsType.DEACTIVATE_VS);
            task.setTargetSlbId(slbId);
            deactivatingTask.add(task);
        }
        List<Long> taskIds = taskManager.addTask(deactivatingTask);

        List<TaskResult> results = taskManager.getResult(taskIds, apiTimeout.get());

        TaskResultList resultList = new TaskResultList();
        for (TaskResult t : results) {
            resultList.addTaskResult(t);
        }
        resultList.setTotal(results.size());

        try {
            propertyBox.set("status", "deactivated", "vs", vsId);
        } catch (Exception ex) {
        }

        String slbMessageData = MessageUtil.getMessageData(request, null, null,
                new VirtualServer[]{vsMap.getOfflineMapping().get(vsId)}, null, null, true);
        messageQueue.produceMessage(request.getRequestURI(), vsId, slbMessageData);

        return responseHandler.handle(resultList, hh.getMediaType());
    }

    @GET
    @Path("/soft/group")
    public Response softDeactivateGroup(@Context HttpServletRequest request,
                                        @Context HttpHeaders hh,
                                        @QueryParam("vsId") Long vsId,
                                        @QueryParam("groupId") Long groupId) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.DEACTIVATE, ResourceDataType.Group, groupId);

        IdVersion[] groupIdsOffline = groupCriteriaQuery.queryByIdAndMode(groupId, SelectionMode.OFFLINE_FIRST);
        if (groupIdsOffline.length == 0) {
            throw new ValidationException("Cannot find group by groupId-" + groupId + ".");
        }
        ModelStatusMapping<VirtualServer> vsMap = entityFactory.getVsesByIds(new Long[]{vsId});
        if (vsMap.getOnlineMapping() == null || vsMap.getOnlineMapping().get(vsId) == null) {
            throw new ValidationException("Vs is not activated.VsId:" + vsId);
        }
        VirtualServer vs = vsMap.getOnlineMapping().get(vsId);
        List<OpsTask> softDeactivatingTasks = new ArrayList<>();
        for (Long slbId : vs.getSlbIds()) {
            OpsTask task = new OpsTask();
            task.setSlbVirtualServerId(vsId);
            task.setCreateTime(new Date());
            task.setOpsType(TaskOpsType.SOFT_DEACTIVATE_GROUP);
            task.setTargetSlbId(slbId);
            task.setGroupId(groupId);
            task.setVersion(groupIdsOffline[0].getVersion());
            softDeactivatingTasks.add(task);
        }
        List<Long> taskIds = taskManager.addTask(softDeactivatingTasks);

        List<TaskResult> results = taskManager.getResult(taskIds, apiTimeout.get());

        TaskResultList resultList = new TaskResultList();
        for (TaskResult t : results) {
            resultList.addTaskResult(t);
        }
        resultList.setTotal(results.size());

        return responseHandler.handle(resultList, hh.getMediaType());
    }

    @GET
    @Path("/soft/vs")
    public Response activateVirtualServer(@Context HttpServletRequest request,
                                          @Context HttpHeaders hh,
                                          @QueryParam("vsId") Long vsId,
                                          @QueryParam("slbId") Long slbId) throws Exception {
        if (vsId == null || slbId == null) {
            throw new ValidationException("Query param vsId, slbId are required.");
        }
        ModelStatusMapping<VirtualServer> vsMap = entityFactory.getVsesByIds(new Long[]{vsId});
        VirtualServer offlineVersion = vsMap.getOfflineMapping().get(vsId);
        if (offlineVersion == null) {
            throw new ValidationException("Cannot find vs by id " + vsId + ".");
        }

        ModelStatusMapping<Slb> slbMap = entityFactory.getSlbsByIds(new Long[]{slbId});
        Slb slb = slbMap.getOnlineMapping().get(slbId);
        if (slb == null) {
            throw new Exception("Slb " + slbId + " is found deactivated.");
        }

        virtualServerModelValidator.validateCanBeDeactive(offlineVersion);
        virtualServerModelValidator.validateCanBeDeactive(vsMap.getOnlineMapping().get(vsId));

        List<OpsTask> tasks = new ArrayList<>();
        OpsTask task = new OpsTask();
        task.setSlbVirtualServerId(vsId)
                .setTargetSlbId(slbId)
                .setVersion(offlineVersion.getVersion())
                .setOpsType(TaskOpsType.SOFT_DEACTIVATE_VS)
                .setCreateTime(new Date());
        tasks.add(task);

        List<Long> taskIds = taskManager.addTask(tasks);

        List<TaskResult> results = taskManager.getResult(taskIds, apiTimeout.get());

        TaskResultList resultList = new TaskResultList();
        for (TaskResult t : results) {
            resultList.addTaskResult(t);
        }
        resultList.setTotal(results.size());

        return responseHandler.handle(resultList, hh.getMediaType());
    }

    @GET
    @Path("/slb")
    public Response deactivateSlb(@Context HttpServletRequest request,
                                  @Context HttpHeaders hh,
                                  @QueryParam("slbId") Long slbId) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.DEACTIVATE, ResourceDataType.Slb, slbId);

        ModelStatusMapping<VirtualServer> vsMap = entityFactory.getVsesBySlbIds(slbId);
        if (vsMap.getOnlineMapping() != null && vsMap.getOnlineMapping().size() > 0) {
            throw new ValidationException("Has Activated Vses Related to Slb[" + slbId + "]");
        }
        IdVersion idVersion = new IdVersion(slbId, 0);
        slbRepository.updateStatus(new IdVersion[]{idVersion});

        try {
            propertyBox.set("status", "deactivated", "slb", slbId);
        } catch (Exception ex) {
        }
        Slb slb = slbRepository.getById(slbId);
        String slbMessageData = MessageUtil.getMessageData(request, null, null, null,
                new Slb[]{slb}, null, true);
        messageQueue.produceMessage(request.getRequestURI(), slbId, slbMessageData);

        return responseHandler.handle(slb, hh.getMediaType());
    }

    @GET
    @Path("/soft/policy")
    public Response softDeactivatePolicy(@Context HttpServletRequest request,
                                         @Context HttpHeaders hh,
                                         @QueryParam("vsId") Long vsId,
                                         @QueryParam("policyId") Long policyId) throws Exception {
        TrafficPolicy policy = trafficPolicyRepository.getById(policyId);
        if (policy == null) {
            throw new ValidationException("Cannot find policy by Id-" + policyId + ".");
        }

        ModelStatusMapping<VirtualServer> vsMap = entityFactory.getVsesByIds(new Long[]{vsId});
        if (vsMap.getOnlineMapping() == null || vsMap.getOnlineMapping().get(vsId) == null) {
            throw new ValidationException("Vs is not activated.VsId:" + vsId);
        }

        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.DEACTIVATE, ResourceDataType.Policy,
                policyId);

        VirtualServer vs = vsMap.getOnlineMapping().get(vsId);
        List<OpsTask> softDeactivatingTasks = new ArrayList<>();
        for (Long slbId : vs.getSlbIds()) {
            OpsTask task = new OpsTask();
            task.setSlbVirtualServerId(vsId);
            task.setCreateTime(new Date());
            task.setOpsType(TaskOpsType.SOFT_DEACTIVATE_POLICY);
            task.setTargetSlbId(slbId);
            task.setPolicyId(policyId);
            task.setVersion(policy.getVersion());
            softDeactivatingTasks.add(task);
        }
        List<Long> taskIds = taskManager.addTask(softDeactivatingTasks);

        List<TaskResult> results = taskManager.getResult(taskIds, apiTimeout.get());

        TaskResultList resultList = new TaskResultList();
        for (TaskResult t : results) {
            resultList.addTaskResult(t);
        }
        resultList.setTotal(results.size());

        return responseHandler.handle(resultList, hh.getMediaType());
    }

    @GET
    @Path("/policy")
    public Response deactivatePolicy(@Context HttpServletRequest request, @Context HttpHeaders hh,
                                     @QueryParam("force") Boolean force,
                                     @QueryParam("policyId") List<Long> policyIds) throws Exception {

        //Get Policy Model
        ModelStatusMapping<TrafficPolicy> tpMap = entityFactory.getPoliciesByIds(policyIds.toArray(new Long[]{}));
        if (!tpMap.getOnlineMapping().keySet().containsAll(policyIds)) {
            throw new ValidationException("Have inactivated policy in " + Joiner.on(",").join(policyIds));
        }
        //Check Auth
        if (force != null && force) {
            authService.authValidateWithForce(UserUtils.getUserName(request), ResourceOperationType.DEACTIVATE, ResourceDataType.Policy,
                    policyIds.toArray(new Long[policyIds.size()]));
        } else {
            authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.DEACTIVATE, ResourceDataType.Policy,
                    policyIds.toArray(new Long[policyIds.size()]));
        }

        TaskResultList resultList = new TaskResultList();
        //Check Policy Is Online And VSes Of Policy.
        Set<Long> vsIds = new HashSet<>();
        for (Map.Entry<Long, TrafficPolicy> e : tpMap.getOnlineMapping().entrySet()) {
            TrafficPolicy policy = e.getValue();
            if (policy == null) {
                throw new Exception("Unexpected online group with null value. groupId=" + e.getKey() + ".");
            }
            for (PolicyVirtualServer vs : policy.getPolicyVirtualServers()) {
                vsIds.add(vs.getVirtualServer().getId());
            }
        }

        ModelStatusMapping<VirtualServer> vsMap = entityFactory.getVsesByIds(vsIds.toArray(new Long[]{}));

        for (Map.Entry<Long, TrafficPolicy> e : tpMap.getOnlineMapping().entrySet()) {
            TrafficPolicy policy = e.getValue();
            Set<Long> slbIds = new HashSet<>();
            Map<Long, String> relatedGroups = null;
            Long savedGroupId = null;
            Map<Long, Integer> pvsPriority = new HashMap<>();

            // Start Deactivate Policy
            List<OpsTask> tasks = new ArrayList<>();

            //Get Related Slb Ids By Vs Ids. Get PVS Priorities.
            for (PolicyVirtualServer pvs : policy.getPolicyVirtualServers()) {
                VirtualServer vs = vsMap.getOnlineMapping().get(pvs.getVirtualServer().getId());
                if (vs == null) {
                    throw new ValidationException("Virtual server " + pvs.getVirtualServer().getId() + " is found deactivated.");
                }
                slbIds.addAll(vs.getSlbIds());
                pvsPriority.put(pvs.getVirtualServer().getId(), pvs.getPriority());
            }

            //Validate For Deactivate. Skip If Force Is True.
            //Get Need Disabled GroupIds:relatedGroups
            //Get SavedGroupId.
            //Set PVS Priorities To Saved Group
            Group savedGroup = null;
            Map<Long, Group> updatedRelatedGroups = new HashMap<>();

            if (force == null || !force) {
                relatedGroups = trafficPolicyValidator.validatedForDeactivate(policy);

                if (relatedGroups != null && relatedGroups.size() > 0) {
                    for (TrafficControl tc : policy.getControls()) {
                        if (!relatedGroups.containsKey(tc.getGroup().getId())) {
                            savedGroupId = tc.getGroup().getId();
                            savedGroup = updateGroupPriority(request, savedGroupId, pvsPriority);
                        } else if (relatedGroups.get(tc.getGroup().getId()).equalsIgnoreCase(DefaultTrafficPolicyValidator.RELATED_GROUP_OPS_REMOVE_GVS)) {
                            Group tmp = removeGroupVirtualServer(request, tc.getGroup().getId(), pvsPriority);
                            updatedRelatedGroups.put(tmp.getId(), tmp);
                        }
                    }
                }
            }

            // AggTasks : DeactivatePolicy Deactivate Disabled Groups Activate SavedGroup
            for (Long slbId : slbIds) {
                OpsTask task = new OpsTask();
                task.setPolicyId(e.getKey());
                task.setOpsType(TaskOpsType.DEACTIVATE_POLICY);
                task.setTargetSlbId(slbId);
                tasks.add(task);
                if (relatedGroups != null && savedGroupId != null && savedGroup != null) {
                    for (Long gid : relatedGroups.keySet()) {
                        if (relatedGroups.get(gid).equalsIgnoreCase(DefaultTrafficPolicyValidator.RELATED_GROUP_OPS_REMOVE_GVS)) {
                            if (!updatedRelatedGroups.containsKey(gid)) {
                                continue;
                            }
                            for (PolicyVirtualServer pvs : policy.getPolicyVirtualServers()) {
                                if (!pvs.getVirtualServer().getSlbIds().contains(slbId)) continue;
                                OpsTask gTask = new OpsTask();
                                gTask.setGroupId(gid);
                                gTask.setOpsType(TaskOpsType.SOFT_DEACTIVATE_GROUP);
                                gTask.setSkipValidate(true);
                                gTask.setSlbVirtualServerId(pvs.getVirtualServer().getId());
                                gTask.setTargetSlbId(slbId);
                                tasks.add(gTask);
                            }
                            for (GroupVirtualServer gvs : updatedRelatedGroups.get(gid).getGroupVirtualServers()) {
                                for (Long sid : gvs.getVirtualServer().getSlbIds()) {
                                    OpsTask t = new OpsTask();
                                    t.setOpsType(TaskOpsType.ACTIVATE_GROUP);
                                    t.setGroupId(gid);
                                    t.setSkipValidate(true);
                                    t.setVersion(updatedRelatedGroups.get(gid).getVersion());
                                    t.setTargetSlbId(sid);
                                    tasks.add(t);
                                }
                            }
                        } else {
                            OpsTask gTask = new OpsTask();
                            gTask.setGroupId(gid);
                            gTask.setOpsType(TaskOpsType.DEACTIVATE_GROUP);
                            gTask.setTargetSlbId(slbId);
                            tasks.add(gTask);
                        }

                    }
                    OpsTask gTask = new OpsTask();
                    gTask.setGroupId(savedGroupId);
                    gTask.setVersion(savedGroup.getVersion());
                    gTask.setSkipValidate(true);
                    gTask.setOpsType(TaskOpsType.ACTIVATE_GROUP);
                    gTask.setTargetSlbId(slbId);
                    tasks.add(gTask);
                }
            }


            List<Long> taskIds = taskManager.addAggTask(tasks);
            List<TaskResult> results = taskManager.getAggResult(taskIds, apiTimeout.get());
            try {
                if (relatedGroups != null && relatedGroups.size() > 0) {
                    for (Long gid : relatedGroups.keySet()) {
                        Group tmpGroup = new Group();
                        tmpGroup.setId(gid);
                        if (relatedGroups.get(gid).equalsIgnoreCase(DefaultTrafficPolicyValidator.RELATED_GROUP_OPS_DEACTIVATE)) {
                            propertyBox.set("status", "deactivated", "group", gid);
                            String slbMessageData = MessageUtil.getMessageBuilder(request, true).bindUri(request.getRequestURI())
                                    .bindGroups(new Group[]{tmpGroup}).bindType("/api/deactivate/group").build();
                            messageQueue.produceMessage("/api/deactivate/group", tmpGroup.getId(), slbMessageData);
                        } else {
                            propertyBox.set("status", "activated", "group", gid);
                            String slbMessageData = MessageUtil.getMessageBuilder(request, true).bindUri(request.getRequestURI())
                                    .bindGroups(new Group[]{savedGroup}).bindType("/api/activate/group").build();
                            messageQueue.produceMessage("/api/activate/group", savedGroup.getId(), slbMessageData);
                        }
                    }
                }
                if (savedGroupId != null) {
                    propertyBox.set("migrationStatus", "done", "group", savedGroupId);
                    propertyBox.set("migrationDoneTime", DateFormatUtils.writeDate(new Date()), "group", savedGroupId);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                    propertyBox.set("migrationDoneWeek", format.format(calendar.getTime()), "group", savedGroupId);
                    Property p = propertyService.getProperty(PropertyNames.RELATED_APP_ID, savedGroupId, "group");
                    if (p != null) {
                        propertyBox.set(PropertyNames.LATEST_RELATED_APP_ID, p.getValue(), "group", savedGroupId);
                    }
                    propertyBox.clear(PropertyNames.RELATED_APP_ID, "group", savedGroupId);
                    String slbMessageData = MessageUtil.getMessageBuilder(request, true).bindUri(request.getRequestURI())
                            .bindGroups(new Group[]{savedGroup}).bindType("/api/activate/group").build();
                    messageQueue.produceMessage("/api/activate/group", savedGroup.getId(), slbMessageData);
                }
            } catch (Exception ex) {
            }
            for (TaskResult t : results) {
                resultList.addTaskResult(t);
            }
            resultList.setTotal(resultList.getTaskResults().size());
            List<Group> groups = new ArrayList<>();
            if (relatedGroups != null && relatedGroups.size() > 0) {
                for (Long id : relatedGroups.keySet()) {
                    groups.add(new Group().setId(id));
                }
            }
            String slbMessageData = MessageUtil.getMessageBuilder(request, true)
                    .bindGroups(groups.toArray(new Group[groups.size()])).bindPolicies(new TrafficPolicy[]{policy}).build();
            messageQueue.produceMessage(request.getRequestURI(), policy.getId(), slbMessageData);

        }

        try {
            propertyBox.set("status", "deactivated", "policy", tpMap.getOnlineMapping().keySet().toArray(new Long[tpMap.getOnlineMapping().size()]));
        } catch (Exception ex) {
        }

        return responseHandler.handle(resultList, hh.getMediaType());
    }


    private Group updateGroupPriority(HttpServletRequest request, Long groupId, Map<Long, Integer> policyPriorities) throws Exception {
        DistLock lock = dbLockFactory.newLock(groupId + "_updateGroup");
        lock.lock(5000);
        try {
            IdVersion[] idv = groupCriteriaQuery.queryByIdAndMode(groupId, SelectionMode.REDUNDANT);
            if (idv.length > 1 && idv[0].getVersion().equals(idv[1].getVersion())) {
                Group g = groupRepository.getById(groupId);
                for (GroupVirtualServer gvs : g.getGroupVirtualServers()) {
                    if (policyPriorities.get(gvs.getVirtualServer().getId()) != null && gvs.getPriority() < policyPriorities.get(gvs.getVirtualServer().getId())) {
                        gvs.setPriority(policyPriorities.get(gvs.getVirtualServer().getId()));
                    }
                }

                Group group;
                if (GroupType.VGROUP.toString().equals(g.getType())) {
                    g.setVirtual(true);
                    group = groupRepository.updateVGroup(g, true);
                } else {
                    group = groupRepository.update(g, true);
                }
                String slbMessageData = MessageUtil.getMessageBuilder(request, true).bindUri(request.getRequestURI())
                        .bindGroups(new Group[]{g}).bindType("/api/group/update").build();
                messageQueue.produceMessage("/api/group/update", g.getId(), slbMessageData);
                return group;
            } else {
                throw new Exception("Saved Group Need Tobe Activated.GroupId:" + groupId);
            }
        } finally {
            lock.unlock();
        }
    }

    private Group removeGroupVirtualServer(HttpServletRequest request, Long groupId, Map<Long, Integer> policyPriorities) throws Exception {
        DistLock lock = dbLockFactory.newLock(groupId + "_updateGroup");
        lock.lock(5000);
        try {
            IdVersion[] idv = groupCriteriaQuery.queryByIdAndMode(groupId, SelectionMode.REDUNDANT);
            if (idv.length > 1 && idv[0].getVersion().equals(idv[1].getVersion())) {
                Group g = groupRepository.getById(groupId);
                List<GroupVirtualServer> toRemove = new ArrayList<>();
                for (GroupVirtualServer gvs : g.getGroupVirtualServers()) {
                    if (policyPriorities.containsKey(gvs.getVirtualServer().getId())) {
                        toRemove.add(gvs);
                    }
                }
                g.getGroupVirtualServers().removeAll(toRemove);

                Group group;
                if (GroupType.VGROUP.toString().equals(g.getType())) {
                    g.setVirtual(true);
                    group = groupRepository.updateVGroup(g, true);
                } else {
                    group = groupRepository.update(g, true, true);
                }
                String slbMessageData = MessageUtil.getMessageBuilder(request, true).bindUri(request.getRequestURI())
                        .bindGroups(new Group[]{g}).bindType("/api/group/update").build();
                messageQueue.produceMessage("/api/group/update", g.getId(), slbMessageData);
                return group;
            } else {
                throw new Exception("Remove Group Virtual Server Need Tobe Activated.GroupId:" + groupId);
            }
        } finally {
            lock.unlock();
        }
    }
}
