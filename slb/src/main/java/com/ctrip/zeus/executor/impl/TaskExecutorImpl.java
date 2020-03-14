package com.ctrip.zeus.executor.impl;

import com.ctrip.zeus.exceptions.NginxProcessingException;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.executor.TaskExecutor;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.model.commit.Commit;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.model.nginx.NginxResponse;
import com.ctrip.zeus.model.status.UpdateStatusItem;
import com.ctrip.zeus.model.task.OpsTask;
import com.ctrip.zeus.service.build.BuildService;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.commit.CommitService;
import com.ctrip.zeus.service.commit.util.CommitType;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.common.ErrorType;
import com.ctrip.zeus.service.model.common.RulePhase;
import com.ctrip.zeus.service.model.common.ValidationContext;
import com.ctrip.zeus.service.nginx.NginxService;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.service.status.StatusOffset;
import com.ctrip.zeus.service.status.StatusService;
import com.ctrip.zeus.service.task.TaskService;
import com.ctrip.zeus.service.task.constant.TaskOpsType;
import com.ctrip.zeus.service.task.constant.TaskStatus;
import com.ctrip.zeus.service.version.ConfVersionService;
import com.ctrip.zeus.support.DefaultObjectJsonParser;
import com.ctrip.zeus.support.DefaultObjectJsonWriter;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.util.ObjectJsonWriter;
import com.ctrip.zeus.util.StringUtils;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

;

/**
 * Created by fanqq on 2015/7/31.
 */
@Component("taskExecutor")
public class TaskExecutorImpl implements TaskExecutor {

    @Resource
    private DbLockFactory dbLockFactory;
    @Autowired
    private GroupRepository groupRepository;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private VirtualServerRepository virtualServerRepository;
    @Resource
    private DrRepository drRepository;
    @Resource
    private EntityFactory entityFactory;
    @Resource
    private TaskService taskService;
    @Resource
    private BuildService buildService;
    @Resource
    private StatusService statusService;
    @Autowired
    private NginxService nginxService;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    private ConfVersionService confVersionService;
    @Resource
    private CommitService commitService;
    @Resource
    private TrafficPolicyRepository trafficPolicyRepository;
    @Resource
    private ValidationFacade validationFacade;
    @Resource
    private PropertyService propertyService;
    @Resource
    private ConfigHandler configHandler;
    @Resource
    private RuleRepository ruleRepository;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private static DynamicBooleanProperty writeEnable = DynamicPropertyFactory.getInstance().getBooleanProperty("write.enable", true);

    private HashMap<String, OpsTask> serverOps = new HashMap<>();
    private HashMap<Long, OpsTask> activateGroupOps = new HashMap<>();
    private HashMap<Long, OpsTask> deactivateGroupOps = new HashMap<>();
    private HashMap<Long, OpsTask> softDeactivateGroupOps = new HashMap<>();
    private HashMap<Long, OpsTask> activatePolicyOps = new HashMap<>();
    private HashMap<Long, OpsTask> deactivatePolicyOps = new HashMap<>();
    private HashMap<Long, OpsTask> softDeactivatePolicyOps = new HashMap<>();
    private HashMap<Long, OpsTask> activateDrOps = new HashMap<>();
    private HashMap<Long, OpsTask> deactivateDrOps = new HashMap<>();
    private HashMap<Long, OpsTask> softDeactivateDrOps = new HashMap<>();
    private HashMap<Long, OpsTask> activateVsOps = new HashMap<>();
    private HashMap<Long, OpsTask> deactivateVsOps = new HashMap<>();
    private HashMap<Long, OpsTask> softDeactivateVsOps = new HashMap<>();
    private HashMap<Long, OpsTask> activateSlbOps = new HashMap<>();
    private HashMap<Long, List<OpsTask>> memberOps = new HashMap<>();
    private HashMap<Long, List<OpsTask>> pullMemberOps = new HashMap<>();
    private HashMap<Long, List<OpsTask>> healthyOps = new HashMap<>();
    private HashMap<Long, List<OpsTask>> aggSubTasks = new HashMap<>();
    private HashMap<Long, List<OpsTask>> aggSyncVsActivateTasks = new HashMap<>();
    private HashMap<Long, OpsTask> aggTasks = new HashMap<>();

    private List<OpsTask> tasks = null;

    @Override
    public void execute(Long slbId) {
        DistLock buildLock = null;
        List<DistLock> resLocks = new ArrayList<>();
        boolean lockflag = false;
        long start = System.currentTimeMillis();
        try {
            buildLock = dbLockFactory.newLock("TaskWorker_" + slbId);
            if (lockflag = buildLock.tryLock()) {
                fetchTask(slbId);
                List<Long> resources = getResources();
                for (Long res : resources) {
                    DistLock resLock = dbLockFactory.newLock("TaskRes_" + res);
                    if (resLock.tryLock()) {
                        resLocks.add(resLock);
                    } else {
                        throw new Exception("Get Resources Failed! ResourceId : " + res);
                    }
                }
                //0. get pending tasks , if size == 0 return
                if (tasks.size() != 0) {
                    executeJob(slbId, tasks);
                }
            } else {
                logger.warn("TaskWorker get lock failed! TaskWorker:" + slbId);
            }
        } catch (Exception e) {
            logger.warn("Executor Job Failed! TaskWorker: " + slbId, e);
        } finally {
            taskExecutorLog(slbId, System.currentTimeMillis() - start);
            for (DistLock lock : resLocks) {
                lock.unlock();
            }
            if (lockflag) {
                buildLock.unlock();
            }
        }
    }

    private void fetchTask(Long slbId) {
        try {
            tasks = taskService.getPendingTasks(slbId);
        } catch (Exception e) {
            logger.warn("Task Executor get pending tasks failed! ", e);
        }
    }

    private void executeJob(Long slbId, List<OpsTask> pendingTasks) throws Exception {
        sortTaskData(pendingTasks);
        Slb onlineSlb = null;
        Long buildVersion = null;
        boolean needRollbackConf = false;
        try {
            //1. full access data from database and revise offline version by tasks
            ModelStatusMapping<Slb> slbMap;
            ModelStatusMapping<VirtualServer> vsMap;
            ModelStatusMapping<Group> groupMap;
            ModelStatusMapping<TrafficPolicy> tpMap;
            ModelStatusMapping<Dr> drMap;

            Set<Long> buildingVsIds;
            Set<Long> buildingGroupIds = new HashSet<>();
            Set<Long> buildingPolicyIds = new HashSet<>();
            Set<Long> buildingDrIds = new HashSet<>();
            Map<Long, List<Group>> groupReferrerOfBuildingVs = new HashMap<>();
            Map<Long, List<TrafficPolicy>> policyReferrerOfBuildingVs = new HashMap<>();
            //1.1 get all model data
            slbMap = mapSlbVersionAndRevise(slbId, activateSlbOps.values());
            onlineSlb = slbMap.getOnlineMapping().get(slbId);
            vsMap = mapVersionAndRevise(slbId, activateVsOps.values());
            Set<Long> relatedVsIds = new HashSet<>();
            relatedVsIds.addAll(vsMap.getOnlineMapping().keySet());
            relatedVsIds.addAll(vsMap.getOfflineMapping().keySet());
            groupMap = mapVersionAndRevise(relatedVsIds, activateGroupOps.values());
            tpMap = mapPolicyVersionAndRevise(relatedVsIds, activatePolicyOps.values());
            drMap = mapDrVersionAndRevise(relatedVsIds, activateDrOps.values());

            //2. merge data and get next online entities
            Map<Long, Group> nxOnlineGroups = getNextOnlineGroups(groupMap);
            Map<Long, VirtualServer> nxOnlineVses = getNextOnlineVses(vsMap, slbId);
            Slb nxOnlineSlb = getNextOnlineSlb(slbId, slbMap, onlineSlb);
            Map<Long, TrafficPolicy> nxOnlineTps = getNextOnlinePolicies(tpMap, nxOnlineSlb);
            Map<Long, Dr> nxOnlineDrs = getNextOnlineDrs(drMap);

            //3. find out vses which need build.
            //3.1 deactivate vs pre check
            if (deactivateVsOps.size() > 0) {
                deactivateVsPreCheck(vsMap.getOnlineMapping().keySet(), nxOnlineGroups, nxOnlineTps, nxOnlineDrs);
            }
            //3.2 find out vses which need build.
            Set<Long> buildingVsByDemand;

            // 3.3 get default rules
            List<Rule> defaultRules = ruleRepository.getDefaultRules();

            buildingVsByDemand = filterBuildingVsByDemand(nxOnlineVses, groupMap.getOnlineMapping(), nxOnlineGroups, tpMap.getOnlineMapping(), nxOnlineTps);
            if (activateSlbOps.size() > 0 && activateSlbOps.get(slbId) != null) {
                buildingVsIds = new HashSet<>(nxOnlineVses.keySet());
            } else {
                buildingVsIds = buildingVsByDemand;
            }

            Map<Long, Map<Long, Map<Long, Integer>>> drDesSlbByGvses = new HashMap<>();
            Map<Long, Dr> drByGroupIds = new HashMap<>();

            updateBuildingVses(groupReferrerOfBuildingVs, policyReferrerOfBuildingVs, drDesSlbByGvses, drByGroupIds, buildingVsIds,
                    nxOnlineGroups, nxOnlineVses, nxOnlineTps, nxOnlineDrs,
                    slbId, buildingGroupIds, buildingPolicyIds, buildingDrIds,
                    tpMap, groupMap, drMap);

            // Check For Vs Merge Or Split
            aggSyncVsTaskCheck(groupReferrerOfBuildingVs, policyReferrerOfBuildingVs, drByGroupIds);

            //3.* in case of no need to update the config files.
            //only have operation for inactivated groups.
            if (activateSlbOps.size() == 0 && buildingVsIds.size() == 0 && deactivateVsOps.size() == 0 && softDeactivateVsOps.size() == 0) {
                performTasks(groupMap.getOfflineMapping());
                setTaskResult(slbId, true, null);
                return;
            }

            //4.0 remove deactivate vs ids from need build vses
            Set<Long> cleanVsIds = new HashSet<>();
            cleanVsIds.addAll(deactivateVsOps.keySet());
            cleanVsIds.addAll(softDeactivateVsOps.keySet());
            buildingVsIds.removeAll(cleanVsIds);

            //4. build config
            buildVersion = buildConfig(nxOnlineSlb, nxOnlineVses, buildingVsIds, cleanVsIds, policyReferrerOfBuildingVs, groupReferrerOfBuildingVs, nxOnlineGroups, drDesSlbByGvses, drByGroupIds, defaultRules);
            //5. push config
            boolean softReload = (activateSlbOps.size() + activateGroupOps.size() + deactivateGroupOps.size() +
                    softDeactivateGroupOps.size() + activateVsOps.size() + deactivateVsOps.size() +
                    softDeactivateVsOps.size() + activatePolicyOps.size() + deactivatePolicyOps.size() + softDeactivatePolicyOps.size() +
                    softDeactivateDrOps.size() + deactivateDrOps.size() + activateDrOps.size()) > 0;
            boolean fullUpdate = activateSlbOps.size() > 0 && activateSlbOps.get(slbId) != null;

            if (writeEnable.get()) {
                confVersionService.updateSlbCurrentVersion(slbId, buildVersion);
                addCommit(slbId, fullUpdate, softReload, buildVersion, buildingVsIds, cleanVsIds, buildingGroupIds);
                needRollbackConf = true;
                NginxResponse response = nginxService.updateConf(nxOnlineSlb.getSlbServers());
                if (!response.getSucceed()) {
                    throw new Exception("Update config Fail.Fail Response:" + ObjectJsonWriter.write(response));
                }
            }
            performTasks(groupMap.getOfflineMapping());
            setTaskResult(slbId, true, null);
        } catch (Exception e) {
            StringWriter out = new StringWriter(512);
            PrintWriter printWriter = new PrintWriter(out);
            e.printStackTrace(printWriter);
            String failCause = e.getMessage() + out.getBuffer().toString();
            setTaskResult(slbId, false, failCause.length() > 1024 ? failCause.substring(0, 1024) : failCause);
            rollBack(onlineSlb, buildVersion, needRollbackConf);
            throw e;
        }
    }

    private boolean getDrRelations(Set<Long> buildingVsIdsTmp, Set<Long> buildingVsIds, Map<Long, Dr> onlineDrs, Map<Long, Dr> nxOnlineDrs, Map<Long, Group> nxOnlineGroups, Map<Long, VirtualServer> nxOnlineVses,
                                   Map<Long, Map<Long, Map<Long, Integer>>> drDesSlbByGvses, Map<Long, Dr> drByGroupIds) throws Exception {
        drDesSlbByGvses.clear();
        drByGroupIds.clear();
        for (Map.Entry<Long, Dr> e : nxOnlineDrs.entrySet()) {
            for (DrTraffic traffic : e.getValue().getDrTraffics()) {
                Long gid = traffic.getGroup().getId();
                drByGroupIds.put(gid, e.getValue());
            }
        }

        Map<Long, Group> drRelatedGroups = new HashMap<>();
        Set<Long> vsIds = new HashSet<>();
        Map<Long, VirtualServer> vsLookup = new HashMap<>();

        Set<IdVersion> keys = groupCriteriaQuery.queryByIdsAndMode(drByGroupIds.keySet().toArray(new Long[drByGroupIds.size()]), SelectionMode.ONLINE_EXCLUSIVE);
        for (Group g : groupRepository.list(keys.toArray(new IdVersion[keys.size()]))) {
            Long id = g.getId();
            if (nxOnlineGroups.containsKey(id)) {
                g = nxOnlineGroups.get(id);
            }
            drRelatedGroups.put(id, g);
            for (GroupVirtualServer gvs : g.getGroupVirtualServers()) {
                Long vsId = gvs.getVirtualServer().getId();
                if (nxOnlineVses.containsKey(vsId)) {
                    vsLookup.put(vsId, nxOnlineVses.get(vsId));
                } else {
                    vsIds.add(vsId);
                }
            }
        }

        keys = virtualServerCriteriaQuery.queryByIdsAndMode(vsIds.toArray(new Long[vsIds.size()]), SelectionMode.ONLINE_EXCLUSIVE);
        for (VirtualServer vs : virtualServerRepository.listAll(keys.toArray(new IdVersion[vsIds.size()]))) {
            vsLookup.put(vs.getId(), vs);
        }

        Set<IdVersion> idVersionSet = slbCriteriaQuery.queryAll(SelectionMode.ONLINE_EXCLUSIVE);
        List<Slb> slbs = slbRepository.list(idVersionSet.toArray(new IdVersion[idVersionSet.size()]));
        List<Long> slbIds = new ArrayList<>();
        for (Slb slb : slbs) {
            slbIds.add(slb.getId());
        }
        Map<Long, Property> slbIdcInfo = propertyService.getProperties("idc_code", "slb", slbIds.toArray(new Long[slbIds.size()]));
        Map<Long, Property> groupIdcInfo = propertyService.getProperties("idc_code", "group", drRelatedGroups.keySet().toArray(new Long[drRelatedGroups.size()]));

        boolean flag = true;

        for (Map.Entry<Long, Dr> entry : nxOnlineDrs.entrySet()) {
            Long drId = entry.getKey();
            if (deactivateDrOps.containsKey(drId)) {
                continue;
            }

            try {
                for (DrTraffic traffic : entry.getValue().getDrTraffics()) {
                    Long sourceGroupId = traffic.getGroup().getId();
                    for (Destination des : traffic.getDestinations()) {
                        Long sourceVsId = des.getVirtualServer().getId();
                        if ((softDeactivateDrOps.containsKey(drId) && softDeactivateDrOps.get(drId).getSlbVirtualServerId().equals(sourceVsId))
                                || (!buildingVsIds.contains(sourceVsId) && !buildingVsIdsTmp.contains(sourceVsId)))
                            continue;
                        Map<Long, Map<Long, Integer>> controlsByGroupId = drDesSlbByGvses.get(sourceVsId);
                        if (controlsByGroupId == null) {
                            controlsByGroupId = new HashMap<>();
                            drDesSlbByGvses.put(sourceVsId, controlsByGroupId);
                        }
                        Map<Long, Integer> slbControl = new HashMap<>(des.getControls().size());
                        for (TrafficControl control : des.getControls()) {
                            Long targetSlbId = validationFacade.validateDrDestination(drRelatedGroups.get(sourceGroupId), drRelatedGroups.get(control.getGroup().getId()), vsLookup.get(sourceVsId), vsLookup, slbIdcInfo, groupIdcInfo);
                            slbControl.put(targetSlbId, control.getWeight());
                        }
                        controlsByGroupId.put(sourceGroupId, slbControl);
                    }
                }
            } catch (Exception e) {
                flag = false;
                if (activateDrOps.containsKey(drId) && !activateDrOps.get(drId).isSkipValidate()) {
                    setTaskFail(activateDrOps.get(drId), "Fetch destination slbId failed for dr:" + drId + ";Error:" + e.getMessage());
                    activateDrOps.remove(drId);
                    if (onlineDrs.containsKey(drId)) {
                        nxOnlineDrs.put(drId, onlineDrs.get(drId));
                    } else {
                        nxOnlineDrs.remove(drId);
                    }
                } else if (deactivateDrOps.containsKey(drId) && !deactivateDrOps.get(drId).isSkipValidate()) {
                    setTaskFail(deactivateDrOps.get(drId), "Fetch destination slbId failed for dr:" + drId + ";Error:" + e.getMessage());
                    deactivateDrOps.remove(drId);
                }
            }
        }
        return flag;
    }


    private void updateBuildingVses(Map<Long, List<Group>> groupReferrerOfBuildingVs,
                                    Map<Long, List<TrafficPolicy>> policyReferrerOfBuildingVs,
                                    Map<Long, Map<Long, Map<Long, Integer>>> drDesSlbByGvses, Map<Long, Dr> drByGroupIds,
                                    Set<Long> buildingVsIds, Map<Long, Group> nxOnlineGroups,
                                    Map<Long, VirtualServer> nxOnlineVses,
                                    Map<Long, TrafficPolicy> nxOnlineTps,
                                    Map<Long, Dr> nxOnlineDrs,
                                    Long slbId, Set<Long> buildingGroupIds, Set<Long> buildingPolicyIds, Set<Long> buildingDrIds,
                                    ModelStatusMapping<TrafficPolicy> tpMap, ModelStatusMapping<Group> groupMap, ModelStatusMapping<Dr> drMap) throws Exception {
        Set<Long> buildingVsIdsTmp = null;
        boolean flag = true;
        for (int i = 0; i < 2; i++) {
            groupReferrerOfBuildingVs.clear();
            policyReferrerOfBuildingVs.clear();
            buildingVsIdsTmp = new HashSet<>(buildingVsIds);
            flag = true;
            for (Map.Entry<Long, Group> e : nxOnlineGroups.entrySet()) {
                boolean buildingRequired = traverseGroupContent(e.getKey(), e.getValue(), slbId,
                        nxOnlineVses,
                        buildingVsIdsTmp, groupReferrerOfBuildingVs);
                if (buildingRequired) {
                    buildingGroupIds.add(e.getKey());
                }
            }
            for (Map.Entry<Long, TrafficPolicy> e : nxOnlineTps.entrySet()) {
                boolean buildingRequired = traversePolicyContent(e.getKey(), e.getValue(), slbId,
                        nxOnlineVses,
                        buildingVsIdsTmp, policyReferrerOfBuildingVs);
                if (buildingRequired) {
                    buildingPolicyIds.add(e.getKey());
                }
            }
            for (Map.Entry<Long, Dr> e : nxOnlineDrs.entrySet()) {
                boolean buildingRequired = traverseDrContent(e.getKey(), e.getValue(), slbId,
                        buildingVsIdsTmp, buildingGroupIds,
                        nxOnlineVses, nxOnlineGroups);
                if (buildingRequired) {
                    buildingDrIds.add(e.getKey());
                }
            }
            for (Long vsId : buildingVsIdsTmp) {
                flag = flag & validateEntriesOnVs(vsId, groupMap.getOnlineMapping(), nxOnlineGroups, tpMap.getOnlineMapping(),
                        nxOnlineTps, groupReferrerOfBuildingVs, policyReferrerOfBuildingVs);
            }
            flag = flag & getDrRelations(buildingVsIdsTmp, buildingVsIds, drMap.getOnlineMapping(), nxOnlineDrs, nxOnlineGroups, nxOnlineVses, drDesSlbByGvses, drByGroupIds);
            if (flag) break;
        }
        if (!flag) {
            throw new ValidationException("Entries Validation On Vs Failed.Building VsIds: " + buildingVsIdsTmp.toString());
        }
        buildingVsIds.addAll(buildingVsIdsTmp);
    }

    private Long buildConfig(Slb nxOnlineSlb, Map<Long, VirtualServer> nxOnlineVses,
                             Set<Long> buildingVsIds, Set<Long> cleanVsIds,
                             Map<Long, List<TrafficPolicy>> policyReferrerOfBuildingVs,
                             Map<Long, List<Group>> groupReferrerOfBuildingVs, Map<Long, Group> nxOnlineGroups,
                             Map<Long, Map<Long, Map<Long, Integer>>> drDesSlbByGvses, Map<Long, Dr> drByGroupIds,
                             List<Rule> defaultRules) throws Exception {
        Set<String> allDownServers = getAllDownServer();
        Set<String> allUpGroupServers = getAllUpGroupServers(nxOnlineGroups);
        List<Long> tmpGids = new ArrayList<>();
        for (List<Group> groupList : groupReferrerOfBuildingVs.values()) {
            for (Group g : groupList) {
                tmpGids.add(g.getId());
            }
        }
        Map<Long, String> canaryIpMap = getCanaryIpMap(tmpGids);

        //4.4 build config
        return buildService.build(nxOnlineSlb, nxOnlineVses, buildingVsIds, cleanVsIds, policyReferrerOfBuildingVs, groupReferrerOfBuildingVs, drDesSlbByGvses, drByGroupIds, allDownServers, allUpGroupServers, canaryIpMap, defaultRules);
    }

    private Map<Long, String> getCanaryIpMap(List<Long> tmpGids) throws Exception {
        Map<Long, String> canaryIpMap = new HashMap<>();
        if (tmpGids.size() > 0) {
            Map<Long, Property> propertyMap = propertyService.getProperties("canaryIp", "group", tmpGids.toArray(new Long[tmpGids.size()]));
            for (Long id : propertyMap.keySet()) {
                canaryIpMap.put(id, propertyMap.get(id).getValue());
            }
        }
        return canaryIpMap;
    }

    private Map<Long, TrafficPolicy> getNextOnlinePolicies(ModelStatusMapping<TrafficPolicy> tpMap, Slb nxOnlineSlb) {
        Map<Long, TrafficPolicy> nxOnlineTps = new HashMap<>(tpMap.getOnlineMapping());
        for (Long pid : activatePolicyOps.keySet()) {
            TrafficPolicy offlineVersion = tpMap.getOfflineMapping().get(pid);
            nxOnlineTps.put(pid, offlineVersion);
        }
        if (nxOnlineTps.size() > 0) {
            nxOnlineSlb.addRule(new Rule().setPhaseId(RulePhase.HTTP_INIT_BY_LUA.getId()).setName("init_randomseed"));
        }
        return nxOnlineTps;
    }

    private Map<Long, Dr> getNextOnlineDrs(ModelStatusMapping<Dr> drMap) {
        Map<Long, Dr> nxOnlineDrs = new HashMap<>(drMap.getOnlineMapping());
        for (Long drId : activateDrOps.keySet()) {
            Dr offlineVersion = drMap.getOfflineMapping().get(drId);
            nxOnlineDrs.put(drId, offlineVersion);
        }
        return nxOnlineDrs;
    }

    private Map<Long, Group> getNextOnlineGroups(ModelStatusMapping<Group> groupMap) {
        Map<Long, Group> nxOnlineGroups = new HashMap<>(groupMap.getOnlineMapping());
        for (Long gid : activateGroupOps.keySet()) {
            Group offlineVersion = groupMap.getOfflineMapping().get(gid);
            nxOnlineGroups.put(gid, offlineVersion);
        }
        return nxOnlineGroups;
    }

    private Map<Long, VirtualServer> getNextOnlineVses(ModelStatusMapping<VirtualServer> vsMap, Long slbId) throws Exception {
        Map<Long, VirtualServer> nxOnlineVses = vsMap.getOnlineMapping();
        for (Long vsId : activateVsOps.keySet()) {
            nxOnlineVses.put(vsId, vsMap.getOfflineMapping().get(vsId));
        }
        if (!validateVsModel(slbId, nxOnlineVses, vsMap.getOnlineMapping(), 3)) {
            throw new ValidationException("[!!!]Validate Vs Model Failed.");
        }
        return nxOnlineVses;
    }

    private Slb getNextOnlineSlb(Long slbId, ModelStatusMapping<Slb> slbMap, Slb onlineSlb) throws Exception {
        Slb nxOnlineSlb = slbMap.getOfflineMapping().get(slbId);
        if (nxOnlineSlb == null) {
            throw new ValidationException("Not found activated slb version.SlbId:" + slbId);
        }
        if (activateSlbOps.size() > 0) {
            if (!validateSlbModel(nxOnlineSlb)) {
                nxOnlineSlb = onlineSlb;
            }
            return nxOnlineSlb;
        } else {
            return onlineSlb;
        }

    }

    private boolean validateVsModel(Long slbId, Map<Long, VirtualServer> nxOnlineVs, Map<Long, VirtualServer> onlineMap, int retry) throws Exception {
        if (retry <= 0) return false;
        ValidationContext context = new ValidationContext();
        validationFacade.validateVsesOnSlb(slbId, nxOnlineVs.values(), context);
        if (context.getErrorVses().size() > 0) {
            for (Long vsId : context.getErrorVses()) {
                if (activateVsOps.containsKey(vsId)) {
                    if (aggSyncVsActivateTasks.containsKey(activateVsOps.get(vsId).getId())) {
                        throw new ValidationException("AggSyncVsActivateTasks Failed.VsId:" + vsId + ";cause:" + context.getVsErrorReason(vsId));
                    }
                    setTaskFail(activateVsOps.get(vsId), "Invalidate version for online. VsId:" + vsId + ";cause:" + context.getVsErrorReason(vsId));
                    activateVsOps.remove(vsId);
                    if (onlineMap.containsKey(vsId)) {
                        nxOnlineVs.put(vsId, onlineMap.get(vsId));
                    } else {
                        nxOnlineVs.remove(vsId);
                    }
                    logger.error("Invalidate online version with activate task. set task failed. VsId:" + vsId + ";cause:" + context.getVsErrorReason(vsId));
                } else {
                    logger.error("Invalidate online version without activate task. VsId:" + vsId + ";cause:" + context.getVsErrorReason(vsId));
                }
            }
            return validateVsModel(slbId, nxOnlineVs, onlineMap, --retry);
        } else {
            return true;
        }
    }

    private boolean validateSlbModel(Slb nxOnlineSlb) throws Exception {
        Set<Long> slbIds = slbCriteriaQuery.queryAll();
        ModelStatusMapping<Slb> slbMap = entityFactory.getSlbsByIds(slbIds.toArray(new Long[slbIds.size()]));
        Map<Long, Slb> map = slbMap.getOnlineMapping();
        map.put(nxOnlineSlb.getId(), nxOnlineSlb);
        ValidationContext context = new ValidationContext();
        validationFacade.validateSlbNodes(map.values(), context);
        if (context.getErrors().size() > 0) {
            setTaskFail(activateSlbOps.get(nxOnlineSlb.getId()), "Invalidate version for online. SlbId:" + nxOnlineSlb.getId() + ";cause:" + context.getErrors().toString());
            activateSlbOps.remove(nxOnlineSlb.getId());
            logger.error("Invalidate version for online. SlbId:" + nxOnlineSlb.getId() + ";cause:" + context.getErrors().toString());
            return false;
        } else {
            return true;
        }
    }

    private boolean validateEntriesOnVs(Long vsId, Map<Long, Group> onlineGroups, Map<Long, Group> nxOnlineGroups,
                                        Map<Long, TrafficPolicy> onlineTpes, Map<Long, TrafficPolicy> nxOnlineTpes,
                                        Map<Long, List<Group>> groupReferrerOfBuildingVs,
                                        Map<Long, List<TrafficPolicy>> policyReferrerOfBuildingVs) throws Exception {
        logger.info("validate entries on vs: " + vsId);
        boolean flag = true;
        ValidationContext context = new ValidationContext();
        validationFacade.validateEntriesOnVs(vsId, groupReferrerOfBuildingVs.get(vsId), policyReferrerOfBuildingVs.get(vsId), context);

        if (context.getErrorGroups().size() > 0) {
            logger.info("Initial error groups: " + context.getErrorGroups());
            validationFacade.validateSkipErrorsOfWhiteList("group", context);
            logger.info("After ignore white list: " + context.getErrorGroups());
            for (Long gid : context.getErrorGroups()) {
                context.ignoreGroupErrors(gid, ErrorType.ROOT_PATH_OVERLAP);
                logger.info("After ignore root path overlap: " + context.getErrorGroups());
                if (activateGroupOps.containsKey(gid) && !activateGroupOps.get(gid).isSkipValidate()) {
                    ignoreExistedPathOverlapErrors(vsId, context, nxOnlineGroups, onlineGroups, gid);
                    logger.info("After ignore existed path overlap: " + context.getErrorGroups());
                    boolean success = checkForRelatedGroup(context, vsId, nxOnlineGroups, nxOnlineGroups.get(gid))
                            && !context.getErrorGroups().contains(gid);
                    logger.info("Success: " + success);
                    if (!success) {
                        setTaskFail(activateGroupOps.get(gid), "Group/Entries Validation On Vs Failed. VsId:" + vsId + ";Error:" + context.getGroupErrorReason(gid));
                        activateGroupOps.remove(gid);
                        flag = false;
                        if (onlineGroups.containsKey(gid)) {
                            nxOnlineGroups.put(gid, onlineGroups.get(gid));
                        } else {
                            nxOnlineGroups.remove(gid);
                        }
                    }
                } else if (deactivateGroupOps.containsKey(gid) && !deactivateGroupOps.get(gid).isSkipValidate()) {
                    setTaskFail(deactivateGroupOps.get(gid), "Group/Entries Validation On Vs Failed. VsId:" + vsId + ";Error:" + context.getGroupErrorReason(gid));
                    deactivateGroupOps.remove(gid);
                    flag = false;
                }
            }
        }
        if (context.getErrorPolicies().size() > 0) {
            validationFacade.validateSkipErrorsOfWhiteList("policy", context);
            for (Long pid : context.getErrorPolicies()) {
                if (activatePolicyOps.containsKey(pid) && !activatePolicyOps.get(pid).isSkipValidate()) {
                    setTaskFail(activatePolicyOps.get(pid), "Policy/Entries Validation On Vs Failed. VsId:" + vsId + ";Error:" + context.getPolicyErrorReason(pid));
                    activatePolicyOps.remove(pid);
                    flag = false;
                    if (onlineTpes.containsKey(pid)) {
                        nxOnlineTpes.put(pid, onlineTpes.get(pid));
                    } else {
                        nxOnlineTpes.remove(pid);
                    }
                } else if (deactivatePolicyOps.containsKey(pid) && !deactivatePolicyOps.get(pid).isSkipValidate()) {
                    setTaskFail(deactivatePolicyOps.get(pid), "Policy/Entries Validation On Vs Failed. VsId:" + vsId + ";Error:" + context.getPolicyErrorReason(pid));
                    deactivatePolicyOps.remove(pid);
                    flag = false;
                }
            }
        }
        return flag;
    }

    // Skip path overlap errors if the corresponding GVS remains the same with the online version.
    private void ignoreExistedPathOverlapErrors(Long vsId, ValidationContext context, Map<Long, Group> nextOnlineGroups, Map<Long, Group> onlineGroups, Long gid) {
        // Not Found Path Overlap, nothing to ignore.
        if (!context.getGroupErrorType(gid).contains(ErrorType.PATH_OVERLAP)) {
            return;
        }
        if (configHandler.getEnable("ignore.path.overlap", false)) {
            context.ignoreGroupErrors(gid, ErrorType.PATH_OVERLAP);
            return;
        }
        // current online group is null. we shouldn't ignore.
        if (!onlineGroups.containsKey(gid)) {
            return;
        }

        Group onlineVersion = onlineGroups.get(gid);
        Group nextOnlineVersion = nextOnlineGroups.get(gid);
        GroupVirtualServer onlineGvs = null;
        GroupVirtualServer nxonlineGvs = null;

        for (GroupVirtualServer gvs : onlineVersion.getGroupVirtualServers()) {
            if (gvs.getVirtualServer().getId().equals(vsId)) {
                onlineGvs = gvs;
                break;
            }
        }
        for (GroupVirtualServer gvs : nextOnlineVersion.getGroupVirtualServers()) {
            if (gvs.getVirtualServer().getId().equals(vsId)) {
                nxonlineGvs = gvs;
                break;
            }
        }
        // online Gvs is null, But next online Gvs is not null. Can't ignore.
        if (onlineGvs == null && nxonlineGvs != null) {
            return;
        }
        // Never happened.
        if (nxonlineGvs == null) {
            return;
        }
        // Ignore path overlap errors if path and priority remain the same.
        if (nxonlineGvs.getPriority().equals(onlineGvs.getPriority()) && StringUtils.equalsIgnoreCase(nxonlineGvs.getPath(), onlineGvs.getPath())) {
            context.ignoreGroupErrors(gid, ErrorType.PATH_OVERLAP);
        }
    }

    public boolean checkForRelatedGroup(ValidationContext context, Long vsId, Map<Long, Group> groups, Group group) throws Exception {
        logger.info("error groups before checkForRelatedGroup: " + context.getErrorGroups());
        List<Group> groupList = new ArrayList<>();
        for (Long gid : context.getErrorGroups()) {
            Group tmp = groups.get(gid);
            if (tmp == null) {
                logger.warn("Invalidate Group:" + gid);
            } else {
                groupList.add(tmp);
            }
        }
        if (!validationFacade.validateRelatedGroup(group, null, null, vsId, groupList)) {
            logger.info("validateFacade.validateRelatedGroup fail");
            return false;
        }
        logger.info("validateFacade.validateRelatedGroup succeed");
        validationFacade.validateSkipErrorsOfRelatedGroup(group, context);
        logger.info("error groups after validateSkipErrorsOfRelatedGroup: " + context.getErrorGroups());
        return true;
    }

    private ModelStatusMapping<Slb> mapSlbVersionAndRevise(Long slbId, Collection<OpsTask> activateSlbTask) throws Exception {
        ModelStatusMapping<Slb> slbMap = entityFactory.getSlbsByIds(new Long[]{slbId});
        Slb offlineSlb = slbMap.getOfflineMapping().get(slbId);
        for (OpsTask task : activateSlbTask) {
            if (task.getSlbId().equals(slbId)) {
                if (!offlineSlb.getVersion().equals(task.getVersion())) {
                    offlineSlb = slbRepository.getByKey(new IdVersion(task.getId(), task.getVersion()));
                    slbMap.getOfflineMapping().put(slbId, offlineSlb);
                }
            }
        }
        return slbMap;
    }

    private ModelStatusMapping<VirtualServer> mapVersionAndRevise(Long slbId, Collection<OpsTask> activateVsTasks) throws Exception {
        ModelStatusMapping<VirtualServer> vsMap = entityFactory.getVsesBySlbIds(slbId);
        Map<Long, VirtualServer> offlineVses = vsMap.getOfflineMapping();
        List<IdVersion> revisedVersion = new ArrayList<>();
        for (OpsTask task : activateVsTasks) {
            if (!offlineVses.get(task.getSlbVirtualServerId()).getVersion().equals(task.getVersion())) {
                revisedVersion.add(new IdVersion(task.getSlbVirtualServerId(), task.getVersion()));
            }
        }
        for (VirtualServer vs : virtualServerRepository.listAll(revisedVersion.toArray(new IdVersion[revisedVersion.size()]))) {
            offlineVses.put(vs.getId(), vs);
        }
        return vsMap;
    }

    private ModelStatusMapping<Group> mapVersionAndRevise(Set<Long> vsIds, Collection<OpsTask> activateGroupTask) throws Exception {
        ModelStatusMapping<Group> groupMap = entityFactory.getGroupsByVsIds(vsIds.toArray(new Long[vsIds.size()]));
        Map<Long, Group> offlineGroups = groupMap.getOfflineMapping();
        List<IdVersion> revisedVersion = new ArrayList<>();
        for (OpsTask task : activateGroupTask) {
            if (!offlineGroups.get(task.getGroupId()).getVersion().equals(task.getVersion())) {
                revisedVersion.add(new IdVersion(task.getGroupId(), task.getVersion()));
            }
        }
        for (Group group : groupRepository.list(revisedVersion.toArray(new IdVersion[revisedVersion.size()]))) {
            offlineGroups.put(group.getId(), group);
        }
        return groupMap;
    }

    private ModelStatusMapping<TrafficPolicy> mapPolicyVersionAndRevise(Set<Long> vsIds, Collection<OpsTask> activatePolicyTask) throws Exception {
        ModelStatusMapping<TrafficPolicy> tpMap = entityFactory.getPoliciesByVsIds(vsIds.toArray(new Long[vsIds.size()]));
        Map<Long, TrafficPolicy> offlinePolicies = tpMap.getOfflineMapping();
        List<IdVersion> revisedVersion = new ArrayList<>();
        for (OpsTask task : activatePolicyTask) {
            if (!offlinePolicies.get(task.getPolicyId()).getVersion().equals(task.getVersion())) {
                revisedVersion.add(new IdVersion(task.getPolicyId(), task.getVersion()));
            }
        }
        for (TrafficPolicy policy : trafficPolicyRepository.list(revisedVersion.toArray(new IdVersion[revisedVersion.size()]))) {
            offlinePolicies.put(policy.getId(), policy);
        }
        return tpMap;
    }

    private ModelStatusMapping<Dr> mapDrVersionAndRevise(Set<Long> vsIds, Collection<OpsTask> activateDrTask) throws Exception {
        ModelStatusMapping<Dr> drMap = entityFactory.getDrsByVsIds(vsIds.toArray(new Long[vsIds.size()]));
        Map<Long, Dr> offlineDrs = drMap.getOfflineMapping();
        List<IdVersion> revisedVersion = new ArrayList<>();
        for (OpsTask task : activateDrTask) {
            if (!offlineDrs.get(task.getDrId()).getVersion().equals(task.getVersion())) {
                revisedVersion.add(new IdVersion(task.getDrId(), task.getVersion()));
            }
        }
        for (Dr dr : drRepository.list(revisedVersion.toArray(new IdVersion[revisedVersion.size()]))) {
            offlineDrs.put(dr.getId(), dr);
        }
        return drMap;
    }

    private Set<Long> filterBuildingVsByDemand(Map<Long, VirtualServer> nxOnlineVses,
                                               Map<Long, Group> currOnlineGroups, Map<Long, Group> nxOnlineGroups, Map<Long, TrafficPolicy> currOnlinePolicies, Map<Long, TrafficPolicy> nxOnlineTpes) throws Exception {
        Set<Long> buildingVsIds = new HashSet<>();
        buildingVsIds.addAll(activateVsOps.keySet());
        if (softDeactivateGroupOps.size() > 0) {
            for (OpsTask task : softDeactivateGroupOps.values()) {
                if (task.getSlbVirtualServerId() != null && nxOnlineVses.containsKey(task.getSlbVirtualServerId())) {
                    buildingVsIds.add(task.getSlbVirtualServerId());
                } else {
                    setTaskFail(task, "Not found online vs for soft deactivate group ops. vs=" + task.getSlbVirtualServerId());
                }
            }
        }
        if (softDeactivatePolicyOps.size() > 0) {
            for (OpsTask task : softDeactivatePolicyOps.values()) {
                if (task.getSlbVirtualServerId() != null && nxOnlineVses.containsKey(task.getSlbVirtualServerId())) {
                    buildingVsIds.add(task.getSlbVirtualServerId());
                } else {
                    setTaskFail(task, "Not found online vs for soft deactivate policy ops. vs=" + task.getSlbVirtualServerId());
                }
            }
        }
        if (softDeactivateDrOps.size() > 0) {
            for (OpsTask task : softDeactivateDrOps.values()) {
                if (task.getSlbVirtualServerId() != null && nxOnlineVses.containsKey(task.getSlbVirtualServerId())) {
                    buildingVsIds.add(task.getSlbVirtualServerId());
                } else {
                    setTaskFail(task, "Not found online vs for soft deactivate dr ops. vs=" + task.getSlbVirtualServerId());
                }
            }
        }
        Set<Long> buildingGroupIds = new HashSet<>(activateGroupOps.keySet());
        // groups with 2~n vses which share the current target slb-id(cond1)
        // while 1~(n-1) of those vses are involved in any vs-ops(cond2)
        // must be rebuilt for the need of regenerating concat upstream filename
        checkCurrentNeedBuildGroupIds(currOnlineGroups, buildingGroupIds, nxOnlineVses);
        checkNextOnlineNeedBuildGroupIds(nxOnlineGroups, buildingGroupIds, nxOnlineVses);


        Set<Long> buildingPolicyIds = new HashSet<>(activatePolicyOps.keySet());
        for (TrafficPolicy p : currOnlinePolicies.values()) {
            if (p.getPolicyVirtualServers().size() <= 1) continue;
            boolean cond1 = false;
            boolean cond2 = false;
            for (PolicyVirtualServer pvs : p.getPolicyVirtualServers()) {
                if (deactivateVsOps.keySet().contains(pvs.getVirtualServer().getId()) ||
                        softDeactivateVsOps.keySet().contains(pvs.getVirtualServer().getId())) {
                    cond1 = true;
                } else if (nxOnlineVses.containsKey(pvs.getVirtualServer().getId())) {
                    cond2 = true;
                }
            }

            if (cond1 && cond2) {
                buildingPolicyIds.add(p.getId());
            }
        }
        for (TrafficPolicy p : nxOnlineTpes.values()) {
            if (p.getPolicyVirtualServers().size() <= 1) continue;
            boolean cond1 = false;
            boolean cond2 = false;
            for (PolicyVirtualServer pvs : p.getPolicyVirtualServers()) {
                if (activateVsOps.keySet().contains(pvs.getVirtualServer().getId())) {
                    cond1 = true;
                } else if (nxOnlineVses.containsKey(pvs.getVirtualServer().getId())) {
                    cond2 = true;
                }
            }
            if (cond1 && cond2) {
                buildingPolicyIds.add(p.getId());
            }
        }

        Set<Long> needBuildingVsIds = new HashSet<>();
        for (Long gid : buildingGroupIds) {
            Group currVersion = currOnlineGroups.get(gid);
            if (currVersion != null) {
                for (GroupVirtualServer gvs : currVersion.getGroupVirtualServers()) {
                    needBuildingVsIds.add(gvs.getVirtualServer().getId());
                }
                for (GroupVirtualServer gvs : nxOnlineGroups.get(gid).getGroupVirtualServers()) {
                    needBuildingVsIds.add(gvs.getVirtualServer().getId());
                }
            }
        }
        for (Long pid : buildingPolicyIds) {
            TrafficPolicy currVersion = currOnlinePolicies.get(pid);
            if (currVersion != null) {
                for (PolicyVirtualServer pvs : currVersion.getPolicyVirtualServers()) {
                    needBuildingVsIds.add(pvs.getVirtualServer().getId());
                }
                for (PolicyVirtualServer pvs : nxOnlineTpes.get(pid).getPolicyVirtualServers()) {
                    needBuildingVsIds.add(pvs.getVirtualServer().getId());
                }
            }
        }

        needBuildingVsIds.retainAll(nxOnlineVses.keySet());
        buildingVsIds.addAll(needBuildingVsIds);

        return buildingVsIds;
    }

    private void checkNextOnlineNeedBuildGroupIds(Map<Long, Group> nxOnlineGroups, Set<Long> buildingGroupIds, Map<Long, VirtualServer> nxOnlineVses) {
        for (Group g : nxOnlineGroups.values()) {
            if (g.getGroupVirtualServers().size() <= 1) continue;
            boolean cond1 = false;
            boolean cond2 = false;
            for (GroupVirtualServer gvs : g.getGroupVirtualServers()) {
                if (activateVsOps.keySet().contains(gvs.getVirtualServer().getId())) {
                    cond1 = true;
                } else if (nxOnlineVses.containsKey(gvs.getVirtualServer().getId())) {
                    cond2 = true;
                }
            }
            if (cond1 && cond2) {
                buildingGroupIds.add(g.getId());
            }
        }
    }

    private void checkCurrentNeedBuildGroupIds(Map<Long, Group> currOnlineGroups, Set<Long> buildingGroupIds, Map<Long, VirtualServer> nxOnlineVses) {
        for (Group g : currOnlineGroups.values()) {
            if (g.getGroupVirtualServers().size() <= 1) continue;
            boolean cond1 = false;
            boolean cond2 = false;
            for (GroupVirtualServer gvs : g.getGroupVirtualServers()) {
                if (deactivateVsOps.keySet().contains(gvs.getVirtualServer().getId()) ||
                        softDeactivateVsOps.keySet().contains(gvs.getVirtualServer().getId())) {
                    cond1 = true;
                } else if (nxOnlineVses.containsKey(gvs.getVirtualServer().getId())) {
                    cond2 = true;
                }
            }
            if (cond1 && cond2) {
                buildingGroupIds.add(g.getId());
            }
        }
    }

    private boolean traverseGroupContent(Long groupId, Group group, Long slbId,
                                         final Map<Long, VirtualServer> nxOnlineVses,
                                         Set<Long> buildingVsIds, Map<Long, List<Group>> groupReferrerOfBuildingVs) throws Exception {
        if (group == null) {
            String errMsg = "Unexpected online group with null value. groupId=" + groupId + ".";
            logger.error(errMsg);
            throw new NginxProcessingException(errMsg);
        }

        boolean buildingGroupRequired = false;
        if (activateGroupOps.containsKey(groupId) || deactivateGroupOps.containsKey(groupId)) {
            buildingGroupRequired = true;
        } else if (pullMemberOps.containsKey(groupId) || memberOps.containsKey(groupId) || healthyOps.containsKey(groupId)) {
            buildingGroupRequired = true;
        }
        if (serverOps.size() > 0 && !buildingGroupRequired) {
            for (GroupServer gs : group.getGroupServers()) {
                if (serverOps.containsKey(gs.getIp())) {
                    buildingGroupRequired = true;
                    break;
                }
            }
        }

        boolean currSlbRelevant = false;

        for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
            if (nxOnlineVses.containsKey(gvs.getVirtualServer().getId())) {
                currSlbRelevant = true;

                Long vsId = gvs.getVirtualServer().getId();

                if (buildingGroupRequired) {
                    if (!buildingVsIds.contains(vsId)) {
                        buildingVsIds.add(vsId);
                    }
                }

                if (deactivateGroupOps.containsKey(groupId)
                        || (softDeactivateGroupOps.containsKey(groupId) && softDeactivateGroupOps.get(groupId).getSlbVirtualServerId().equals(vsId))) {
                    continue;
                }

                List<Group> vsRelatedGroups = groupReferrerOfBuildingVs.get(vsId);
                if (vsRelatedGroups == null) {
                    // give an empty list as long as vs is activated
                    vsRelatedGroups = new ArrayList<>();
                    groupReferrerOfBuildingVs.put(vsId, vsRelatedGroups);
                }
                vsRelatedGroups.add(group);
            }
        }

        if (!currSlbRelevant) {
            //TODO elegantly solve issue of migrating vs and operating related groups simultaneously
            if (activateGroupOps.containsKey(groupId)) {
                setTaskFail(activateGroupOps.get(groupId), "Not found online virtual server for Group[" + groupId + "] in slb[" + slbId + "].");
            } else if (deactivateGroupOps.containsKey(groupId)) {
                setTaskFail(deactivateGroupOps.get(groupId), "Not found online virtual server for Group[" + groupId + "] in slb[" + slbId + "].");
            } else if (pullMemberOps.containsKey(groupId)) {
                for (OpsTask task : pullMemberOps.get(groupId)) {
                    setTaskFail(task, "Not found online virtual server for Group[" + groupId + "] in slb[" + slbId + "].");
                }
            } else if (memberOps.containsKey(groupId)) {
                for (OpsTask task : memberOps.get(groupId)) {
                    setTaskFail(task, "Not found online virtual server for Group[" + groupId + "] in slb[" + slbId + "].");
                }
            } else if (healthyOps.containsKey(groupId)) {
                for (OpsTask task : memberOps.get(groupId)) {
                    setTaskFail(task, "Not found online virtual server for Group[" + groupId + "] in slb[" + slbId + "].");
                }
            }
        }

        return buildingGroupRequired;
    }

    private boolean traversePolicyContent(Long policyId, TrafficPolicy policy, Long slbId,
                                          final Map<Long, VirtualServer> nxOnlineVses,
                                          Set<Long> buildingVsIds, Map<Long, List<TrafficPolicy>> policyReferrerOfBuildingVs) throws Exception {
        if (policy == null) {
            String errMsg = "Unexpected online policy with null value. policyId =" + policyId + ".";
            logger.error(errMsg);
            throw new NginxProcessingException(errMsg);
        }

        boolean buildingPolicyRequired = false;
        if (activatePolicyOps.containsKey(policyId) || deactivatePolicyOps.containsKey(policyId)) {
            buildingPolicyRequired = true;
        }

        boolean currSlbRelevant = false;

        for (PolicyVirtualServer pvs : policy.getPolicyVirtualServers()) {
            if (nxOnlineVses.containsKey(pvs.getVirtualServer().getId())) {
                currSlbRelevant = true;

                Long vsId = pvs.getVirtualServer().getId();

                if (buildingPolicyRequired) {
                    if (!buildingVsIds.contains(vsId)) {
                        buildingVsIds.add(vsId);
                    }
                }

                if (deactivatePolicyOps.containsKey(policyId)
                        || (softDeactivatePolicyOps.containsKey(policyId) && softDeactivatePolicyOps.get(policyId).getSlbVirtualServerId().equals(vsId))) {
                    continue;
                }

                List<TrafficPolicy> vsRelatedPolicies = policyReferrerOfBuildingVs.get(vsId);
                if (vsRelatedPolicies == null) {
                    // give an empty list as long as vs is activated
                    vsRelatedPolicies = new ArrayList<>();
                    policyReferrerOfBuildingVs.put(vsId, vsRelatedPolicies);
                }
                vsRelatedPolicies.add(policy);
            }
        }

        if (!currSlbRelevant) {
            //TODO elegantly solve issue of migrating vs and operating related groups simultaneously
            if (activatePolicyOps.containsKey(policyId)) {
                setTaskFail(activatePolicyOps.get(policyId), "Not found online virtual server for Policy[" + policyId + "] in slb[" + slbId + "].");
            } else if (deactivatePolicyOps.containsKey(policyId)) {
                setTaskFail(deactivatePolicyOps.get(policyId), "Not found online virtual server for Policy[" + policyId + "] in slb[" + slbId + "].");
            }
        }

        return buildingPolicyRequired;
    }

    private boolean traverseDrContent(Long drId, Dr dr, Long slbId, Set<Long> buildingVsIds, Set<Long> buildingGroupIds, Map<Long, VirtualServer> nxOnlineVses, Map<Long, Group> nxOnlineGroups) throws Exception {
        if (dr == null) {
            String errMsg = "Unexpected online dr with null value. drId =" + drId + ".";
            logger.error(errMsg);
            throw new NginxProcessingException(errMsg);
        }

        boolean buildingRequired = false;
        if (activateDrOps.containsKey(drId) || deactivateDrOps.containsKey(drId)) {
            buildingRequired = true;
        }

        boolean currSlbRelevant = false;

        for (DrTraffic traffic : dr.getDrTraffics()) {
            long gid = traffic.getGroup().getId();
            for (Destination des : traffic.getDestinations()) {
                Long vsId = des.getVirtualServer().getId();
                if (nxOnlineVses.containsKey(vsId)) {
                    currSlbRelevant = true;

                    if (buildingRequired) {
                        buildingVsIds.add(vsId);
                        if (nxOnlineGroups.containsKey(gid)) buildingGroupIds.add(gid);
                    }
                }
            }
        }

        if (!currSlbRelevant) {
            //TODO elegantly solve issue of migrating vs and operating related groups simultaneously
            if (activateDrOps.containsKey(drId)) {
                setTaskFail(activateDrOps.get(drId), "Not found online virtual server for Dr[" + drId + "] in slb[" + slbId + "].");
            } else if (deactivateDrOps.containsKey(drId)) {
                setTaskFail(deactivateDrOps.get(drId), "Not found online virtual server for Dr[" + drId + "] in slb[" + slbId + "].");
            }
        }

        return buildingRequired;
    }


    private void taskExecutorLog(Long slbId, long cost) {
        if (tasks == null || tasks.size() == 0) {
            return;
        }
        StringBuilder sb = new StringBuilder(256);
        sb.append("SlbId: ").append(slbId).append("\n");
        sb.append("TaskCount: ").append(tasks.size()).append("\n");
        sb.append("Cost: ").append(cost).append("\n");
        sb.append("Tasks:").append("[\n");
        for (OpsTask task : tasks) {
            sb.append("{");
            sb.append("taskId: ").append(task.getId());
            sb.append("status: ").append(task.getStatus());
            sb.append("failCause: ").append(task.getFailCause());
            sb.append("}\n");
        }
        sb.append("]");
        logger.info(sb.toString());
    }

    private void addCommit(Long slbId, boolean fullUpdate, boolean softReload, Long buildVersion, Set<Long> needBuildVses, Set<Long> cleanVsIds, Set<Long> needBuildGroups) throws Exception {
        Commit commit = new Commit();
        String type = CommitType.COMMIT_TYPE_DYUPS;
        if (fullUpdate) {
            type = CommitType.COMMIT_TYPE_FULL_UPDATE;
        } else if (softReload) {
            type = CommitType.COMMIT_TYPE_SOFT_RELOAD;
        }
        commit.setSlbId(slbId)
                .setVersion(buildVersion)
                .setType(type);
        if (!fullUpdate) {
            commit.getVsIds().addAll(needBuildVses);
            commit.getGroupIds().addAll(needBuildGroups);
        }
        commit.getCleanvsIds().addAll(cleanVsIds);
        for (OpsTask t : tasks) {
            commit.addTaskId(t.getId());
        }
        commitService.add(commit);
    }

    private void deactivateVsPreCheck(Set<Long> currOnlineVses, Map<Long, Group> nxOnlineGroups, Map<Long, TrafficPolicy> nxOnlinePolicies, Map<Long, Dr> nxOnlineDrs) throws Exception {
        Set<Long> keySet = new HashSet<>(deactivateVsOps.keySet());
        for (Long id : keySet) {
            OpsTask task = deactivateVsOps.get(id);
            if (!currOnlineVses.contains(task.getSlbVirtualServerId())) {
//                task.setStatus(TaskStatus.SUCCESS);
//                deactivateVsOps.remove(id);
                /**
                 * DO Nothing.
                 * Still Perform Task In Case Of Already Deactivated VS.
                 * Case: Vs has multi slb relations. One Slb task executor already performed the deactivate vs task while another just start executor.
                 * then the second executor should perform task anyway.
                 */
            }
        }

        List<Long> activatingGroupIds = new ArrayList<>();
        for (Long gid : activateGroupOps.keySet()) {
            Group group = nxOnlineGroups.get(gid);
            for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
                if (deactivateVsOps.containsKey(gvs.getVirtualServer().getId())) {
                    if (!activateGroupOps.containsKey(gid)) {
                        //todo: this will never happen
                        setTaskFail(deactivateVsOps.get(gvs.getVirtualServer().getId()), "[Deactivate Vs] Vs is has relative online group!GroupId:" + gid);
                        deactivateVsOps.remove(gvs.getVirtualServer().getId());
                    } else {
                        activatingGroupIds.add(gid);
                    }
                }
            }
        }
        for (Long groupId : activatingGroupIds) {
            setTaskFail(activateGroupOps.get(groupId), "[Vs deactivate Pre Check] Activating Group While Related Vs is deactivating!");
            activateGroupOps.remove(groupId);
        }

        List<Long> activatingPolicyIds = new ArrayList<>();
        for (Long pid : activatePolicyOps.keySet()) {
            TrafficPolicy policy = nxOnlinePolicies.get(pid);
            for (PolicyVirtualServer pvs : policy.getPolicyVirtualServers()) {
                if (deactivateVsOps.containsKey(pvs.getVirtualServer().getId())) {
                    activatingPolicyIds.add(pid);
                }
            }
        }
        for (Long pid : activatingPolicyIds) {
            setTaskFail(activatePolicyOps.get(pid), "[Vs deactivate Pre Check] Activating Policy While Related Vs is deactivating!");
            activatePolicyOps.remove(pid);
        }

        List<Long> activatingDrIds = new ArrayList<>();
        for (Long drId : activateDrOps.keySet()) {
            Dr dr = nxOnlineDrs.get(drId);
            for (DrTraffic traffic : dr.getDrTraffics()) {
                for (Destination des : traffic.getDestinations()) {
                    if (deactivateVsOps.containsKey(des.getVirtualServer().getId())) {
                        activatingDrIds.add(drId);
                    }
                }
            }
        }
        for (Long drId : activatingDrIds) {
            setTaskFail(activateDrOps.get(drId), "[Vs deactivate Pre Check] Activating Dr While Related Vs is deactivating!");
            activateDrOps.remove(drId);
        }
    }

    private void rollBack(Slb slb, Long buildVersion, boolean needRollbackConf) {
        try {
            if (buildVersion != null && buildVersion > 0) {
                commitService.removeCommit(slb.getId(), buildVersion);
                Long pre = confVersionService.getSlbPreviousVersion(slb.getId());
                if (pre > 0) {
                    confVersionService.updateSlbCurrentVersion(slb.getId(), pre);
                    buildService.rollBackConfig(slb.getId(), pre.intValue());
                }
            }
            if (needRollbackConf) {
                nginxService.rollbackAllConf(slb.getSlbServers());
            }
        } catch (Exception e) {
            logger.error("RollBack Fail!", e);
        }
    }

    private void performTasks(Map<Long, Group> offlineGroups) throws Exception {
        try {
            for (OpsTask task : activateSlbOps.values()) {
                if (!task.getStatus().equals(TaskStatus.DOING)) {
                    continue;
                }
                IdVersion newSlbStatus = new IdVersion(task.getSlbId(), task.getVersion());
                DistLock lock = dbLockFactory.newLock(task.getSlbId() + "_updateSlb");
                if (lock.tryLock()) {
                    try {
                        IdVersion[] idv = slbCriteriaQuery.queryByIdAndMode(task.getSlbId(), SelectionMode.ONLINE_EXCLUSIVE);
                        if (idv.length == 0 || task.getVersion() > idv[0].getVersion()) {
                            slbRepository.updateStatus(new IdVersion[]{newSlbStatus});
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            }
            List<IdVersion> newVsStatus = new ArrayList<>();
            for (OpsTask task : activateVsOps.values()) {
                if (!task.getStatus().equals(TaskStatus.DOING)) {
                    continue;
                }
                DistLock lock = dbLockFactory.newLock(task.getSlbVirtualServerId() + "_updateVs");
                if (lock.tryLock()) {
                    try {
                        IdVersion[] idv = virtualServerCriteriaQuery.queryByIdAndMode(task.getSlbVirtualServerId(), SelectionMode.ONLINE_EXCLUSIVE);
                        if (idv.length == 0 || task.getVersion() > idv[0].getVersion()) {
                            newVsStatus.add(new IdVersion(task.getSlbVirtualServerId(), task.getVersion()));
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            }
            for (OpsTask task : deactivateVsOps.values()) {
                if (!task.getStatus().equals(TaskStatus.DOING)) {
                    continue;
                }
                newVsStatus.add(new IdVersion(task.getSlbVirtualServerId(), 0));
            }
            virtualServerRepository.updateStatus(newVsStatus.toArray(new IdVersion[newVsStatus.size()]));
            List<IdVersion> newGroupStatus = new ArrayList<>();
            List<DistLock> groupLocks = new ArrayList<>();

            try {
                for (OpsTask task : activateGroupOps.values()) {
                    if (!task.getStatus().equals(TaskStatus.DOING)) {
                        continue;
                    }
                    DistLock lock = dbLockFactory.newLock(task.getGroupId() + "_updateGroup");
                    if (lock.tryLock()) {
                        groupLocks.add(lock);
                    } else {
                        for (DistLock tmpLog : groupLocks) {
                            tmpLog.unlock();
                        }
                        throw new Exception("Failed To Get Log:" + task.getGroupId());
                    }
                }
                for (OpsTask task : activateGroupOps.values()) {
                    IdVersion[] idv = groupCriteriaQuery.queryByIdAndMode(task.getGroupId(), SelectionMode.ONLINE_EXCLUSIVE);
                    if (idv.length == 0 || task.getVersion() > idv[0].getVersion()) {
                        newGroupStatus.add(new IdVersion(task.getGroupId(), task.getVersion()));
                    }
                }
                groupRepository.updateStatus(newGroupStatus.toArray(new IdVersion[newGroupStatus.size()]));
            } finally {
                for (DistLock tmpLog : groupLocks) {
                    tmpLog.unlock();
                }
            }

            newGroupStatus.clear();
            for (OpsTask task : deactivateGroupOps.values()) {
                if (!task.getStatus().equals(TaskStatus.DOING)) {
                    continue;
                }
                newGroupStatus.add(new IdVersion(task.getGroupId(), 0));
            }
            groupRepository.updateStatus(newGroupStatus.toArray(new IdVersion[newGroupStatus.size()]));
            List<IdVersion> newPolicyStatus = new ArrayList<>();
            for (OpsTask task : activatePolicyOps.values()) {
                if (!task.getStatus().equals(TaskStatus.DOING)) {
                    continue;
                }
                newPolicyStatus.add(new IdVersion(task.getPolicyId(), task.getVersion()));
            }
            for (OpsTask task : deactivatePolicyOps.values()) {
                if (!task.getStatus().equals(TaskStatus.DOING)) {
                    continue;
                }
                newPolicyStatus.add(new IdVersion(task.getPolicyId(), 0));
            }
            trafficPolicyRepository.updateActiveStatus(newPolicyStatus.toArray(new IdVersion[newPolicyStatus.size()]));

            List<IdVersion> newDrStatus = new ArrayList<>();
            Set<Long> updatedDrIds = new HashSet<>();
            for (OpsTask task : activateDrOps.values()) {
                if (!task.getStatus().equals(TaskStatus.DOING)) {
                    continue;
                }
                updatedDrIds.add(task.getDrId());
                newDrStatus.add(new IdVersion(task.getDrId(), task.getVersion()));
            }
            for (OpsTask task : deactivateDrOps.values()) {
                if (!task.getStatus().equals(TaskStatus.DOING)) {
                    continue;
                }
                updatedDrIds.add(task.getDrId());
                newDrStatus.add(new IdVersion(task.getDrId(), 0));
            }
            //only softDeactivateDr tasks will be generated if remove all drTraffics
            for (OpsTask task : softDeactivateDrOps.values()) {
                if (!task.getStatus().equals(TaskStatus.DOING)) {
                    continue;
                }
                if (!updatedDrIds.contains(task.getDrId())) {
                    newDrStatus.add(new IdVersion(task.getDrId(), task.getVersion()));
                }
            }
            drRepository.updateActiveStatus(newDrStatus.toArray(new IdVersion[newDrStatus.size()]));
            for (OpsTask task : serverOps.values()) {
                if (!task.getStatus().equals(TaskStatus.DOING)) {
                    continue;
                }
                if (task.getUp()) {
                    statusService.upServer(task.getIpList());
                } else {
                    statusService.downServer(task.getIpList());
                }
            }
            List<UpdateStatusItem> memberUpdates = new ArrayList<>();
            for (List<OpsTask> taskList : memberOps.values()) {
                for (OpsTask task : taskList) {
                    UpdateStatusItem item = createUpdateStatusItem(offlineGroups.get(task.getGroupId()), task, StatusOffset.MEMBER_OPS);
                    if (item == null) continue;
                    memberUpdates.add(item);
                }
            }
            statusService.updateStatus(memberUpdates);
            // update pull op status
            List<UpdateStatusItem> pullUpdates = new ArrayList<>();
            for (List<OpsTask> taskList : pullMemberOps.values()) {
                for (OpsTask task : taskList) {
                    UpdateStatusItem item = createUpdateStatusItem(offlineGroups.get(task.getGroupId()), task, StatusOffset.PULL_OPS);
                    if (item == null) continue;
                    pullUpdates.add(item);
                }
            }
            statusService.updateStatus(pullUpdates);
            // update hc op status
            List<UpdateStatusItem> healthyStatus = new ArrayList<>();
            for (List<OpsTask> taskList : healthyOps.values()) {
                for (OpsTask task : taskList) {
                    UpdateStatusItem item = createUpdateStatusItem(offlineGroups.get(task.getGroupId()), task, StatusOffset.HEALTHY);
                    if (item == null) continue;
                    healthyStatus.add(item);
                }
            }
            statusService.updateStatus(healthyStatus);
        } catch (Exception e) {
            throw new Exception("Perform Tasks Fail! TargetSlbId:" + tasks.get(0).getTargetSlbId(), e);
        }

    }

    private UpdateStatusItem createUpdateStatusItem(Group group, OpsTask task, int statusOffset) throws Exception {
        if (!task.getStatus().equals(TaskStatus.DOING)) {
            return null;
        }
        if (group == null) {
            logger.warn("[TaskExecutorImpl] Failed to get group id: " + task.getGroupId());
            return null;
        }

        String[] ips = task.getIpList().split(";");
        List<String> ipList = Arrays.asList(ips);

        UpdateStatusItem item = new UpdateStatusItem();
        item.setGroupId(task.getGroupId()).setVsId(-1L).setSlbId(-1L)
                .setOffset(statusOffset).setUp(task.getUp());
        item.getIpses().addAll(ipList);
        return item;
    }

    private void setTaskResult(Long slbId, boolean isSuc, String failCause) throws Exception {
        List<OpsTask> updateTask = new ArrayList<>();
        for (OpsTask task : tasks) {
            if (task.getId() != null && !aggTasks.containsKey(task.getId())) {
                updateTask.add(task);
            }
            if (task.getStatus().equals(TaskStatus.DOING)) {
                if (isSuc) {
                    task.setStatus(TaskStatus.SUCCESS);
                } else {
                    task.setStatus(TaskStatus.FAIL);
                    task.setFailCause(failCause);
                    logger.warn("TaskFail", "Task:" + ObjectJsonWriter.write(task) + "FailCause:" + failCause);
                }
            }
        }
        for (Long tid : aggTasks.keySet()) {
            String status = TaskStatus.SUCCESS;
            for (OpsTask t : aggSubTasks.get(tid)) {
                if (t.getStatus().equalsIgnoreCase(TaskStatus.FAIL)) {
                    status = TaskStatus.FAIL;
                    break;
                }
            }
            OpsTask task = aggTasks.get(tid);
            task.setStatus(status);
            task.setTaskList(DefaultObjectJsonWriter.write(aggSubTasks.get(tid)));
            updateTask.add(task);
        }
        try {
            taskService.updateTasks(updateTask);
        } catch (Exception e) {
            logger.error("Task Update Failed! TargetSlbId:" + slbId, e);
            throw new Exception("Task Update Failed! TargetSlbId:" + slbId, e);
        }
    }

    private Set<String> getAllUpGroupServers(Map<Long, Group> groups) throws Exception {
        Map<String, List<Boolean>> memberStatus = statusService.fetchGroupServerStatus(groups.keySet().toArray(new Long[]{}));
        Set<Long> tmpid = memberOps.keySet();
        for (Long gid : tmpid) {
            if (!groups.containsKey(gid)) {
                /*group not activated. just skip it.*/
                continue;
            }
            List<OpsTask> taskList = memberOps.get(gid);
            for (OpsTask opsTask : taskList) {
                String ipList = opsTask.getIpList();
                String[] ips = ipList.split(";");
                for (String ip : ips) {
                    if (!memberStatus.containsKey(gid + "_" + ip)) {
                        logger.warn("[[NPE=GID]]" + gid + "_" + ip);
                        continue;
                    }
                    memberStatus.get(gid + "_" + ip).set(StatusOffset.MEMBER_OPS, opsTask.getUp());
                }
            }
        }
        tmpid = pullMemberOps.keySet();
        for (Long gid : tmpid) {
            if (!groups.containsKey(gid)) {
                /*group not activated. just skip it.*/
                continue;
            }
            List<OpsTask> taskList = pullMemberOps.get(gid);
            for (OpsTask opsTask : taskList) {
                String ipList = opsTask.getIpList();
                String[] ips = ipList.split(";");
                for (String ip : ips) {
                    if (!memberStatus.containsKey(gid + "_" + ip)) {
                        logger.warn("[[NPE=GID]]" + gid + "_" + ip);
                        continue;
                    }
                    memberStatus.get(gid + "_" + ip).set(StatusOffset.PULL_OPS, opsTask.getUp());
                }
            }
        }

        tmpid = healthyOps.keySet();
        for (Long gid : tmpid) {
            if (!groups.containsKey(gid)) {
                /*group not activated. just skip it.*/
                continue;
            }
            List<OpsTask> taskList = healthyOps.get(gid);
            for (OpsTask opsTask : taskList) {
                String ipList = opsTask.getIpList();
                String[] ips = ipList.split(";");
                for (String ip : ips) {
                    if (!memberStatus.containsKey(gid + "_" + ip)) {
                        logger.warn("[[NPE=GID]]" + gid + "_" + ip);
                        continue;
                    }
                    memberStatus.get(gid + "_" + ip).set(StatusOffset.HEALTHY, opsTask.getUp());
                }
            }
        }

        Set<String> result = new HashSet<>();
        for (String key : memberStatus.keySet()) {
            List<Boolean> status = memberStatus.get(key);
            if (status.get(StatusOffset.PULL_OPS) && status.get(StatusOffset.MEMBER_OPS) && status.get(StatusOffset.HEALTHY)) {
                result.add(key);
            }
        }
        return result;
    }

    private Set<String> getAllDownServer() throws Exception {
        Set<String> allDownServers = statusService.findAllDownServers();
        Set<String> serverip = serverOps.keySet();
        for (String ip : serverip) {
            if (allDownServers.contains(ip) && serverOps.get(ip).getUp()) {
                allDownServers.remove(ip);
            } else if (!allDownServers.contains(ip) && !serverOps.get(ip).getUp()) {
                allDownServers.add(ip);
            }
        }
        return allDownServers;
    }

    private void sortTaskData(List<OpsTask> pendingTasks) {
        activateSlbOps.clear();
        activateVsOps.clear();
        deactivateVsOps.clear();
        softDeactivateVsOps.clear();
        activatePolicyOps.clear();
        deactivatePolicyOps.clear();
        softDeactivatePolicyOps.clear();
        activateGroupOps.clear();
        deactivateGroupOps.clear();
        softDeactivateGroupOps.clear();
        activateDrOps.clear();
        deactivateDrOps.clear();
        softDeactivateDrOps.clear();
        serverOps.clear();
        memberOps.clear();
        pullMemberOps.clear();
        healthyOps.clear();
        aggSubTasks.clear();
        aggTasks.clear();
        aggSyncVsActivateTasks.clear();
        List<OpsTask> tmp = new ArrayList<>();
        for (OpsTask task : pendingTasks) {
            if (task.getOpsType().equalsIgnoreCase(TaskOpsType.AGGREGATION)) {
                List<OpsTask> subTask = DefaultObjectJsonParser.parseArray(task.getTaskList(), OpsTask.class);
                if (subTask != null) {
                    aggSubTasks.put(task.getId(), subTask);
                    aggTasks.put(task.getId(), task);
                    for (OpsTask s : subTask) {
                        s.setId(task.getId());
                        tmp.add(s);
                    }
                }
            }

        }
        pendingTasks.addAll(tmp);
        List<OpsTask> taskList;
        for (OpsTask task : pendingTasks) {
            task.setStatus(TaskStatus.DOING);
            switch (task.getOpsType()) {
                case TaskOpsType.ACTIVATE_GROUP:
                    if (!activateGroupOps.containsKey(task.getGroupId())
                            || activateGroupOps.get(task.getGroupId()).getVersion() < task.getVersion()) {
                        activateGroupOps.put(task.getGroupId(), task);
                    }
                    break;
                case TaskOpsType.ACTIVATE_SLB:
                    if (!activateSlbOps.containsKey(task.getSlbId())
                            || activateSlbOps.get(task.getSlbId()).getVersion() < task.getVersion()) {
                        activateSlbOps.put(task.getSlbId(), task);
                    }
                    break;
                case TaskOpsType.SERVER_OPS:
                    serverOps.put(task.getIpList(), task);
                    break;
                case TaskOpsType.MEMBER_OPS:
                    taskList = memberOps.get(task.getGroupId());
                    if (taskList == null) {
                        taskList = new ArrayList<>();
                        memberOps.put(task.getGroupId(), taskList);
                    }
                    taskList.add(task);
                    break;
                case TaskOpsType.PULL_MEMBER_OPS:
                    taskList = pullMemberOps.get(task.getGroupId());
                    if (taskList == null) {
                        taskList = new ArrayList<>();
                        pullMemberOps.put(task.getGroupId(), taskList);
                    }
                    taskList.add(task);
                    break;
                case TaskOpsType.HEALTHY_OPS:
                    taskList = healthyOps.get(task.getGroupId());
                    if (taskList == null) {
                        taskList = new ArrayList<>();
                        healthyOps.put(task.getGroupId(), taskList);
                    }
                    taskList.add(task);
                    break;
                case TaskOpsType.DEACTIVATE_GROUP:
                    deactivateGroupOps.put(task.getGroupId(), task);
                    break;
                case TaskOpsType.SOFT_DEACTIVATE_GROUP:
                    softDeactivateGroupOps.put(task.getGroupId(), task);
                    break;
                case TaskOpsType.DEACTIVATE_VS:
                    deactivateVsOps.put(task.getSlbVirtualServerId(), task);
                    break;
                case TaskOpsType.SOFT_DEACTIVATE_VS:
                    softDeactivateVsOps.put(task.getSlbVirtualServerId(), task);
                    break;
                case TaskOpsType.ACTIVATE_VS:
                    if (!activateVsOps.containsKey(task.getSlbVirtualServerId())
                            || activateVsOps.get(task.getSlbVirtualServerId()).getVersion() < task.getVersion()) {
                        activateVsOps.put(task.getSlbVirtualServerId(), task);
                    }
                    break;
                case TaskOpsType.ACTIVATE_POLICY:
                    if (!activatePolicyOps.containsKey(task.getPolicyId())
                            || activatePolicyOps.get(task.getPolicyId()).getVersion() < task.getVersion()) {
                        activatePolicyOps.put(task.getPolicyId(), task);
                    }
                    break;
                case TaskOpsType.DEACTIVATE_POLICY:
                    deactivatePolicyOps.put(task.getPolicyId(), task);
                    break;
                case TaskOpsType.SOFT_DEACTIVATE_POLICY:
                    softDeactivatePolicyOps.put(task.getPolicyId(), task);
                    break;
                case TaskOpsType.ACTIVATE_DR:
                    if (!activateDrOps.containsKey(task.getDrId())
                            || activateDrOps.get(task.getDrId()).getVersion() < task.getVersion()) {
                        activateDrOps.put(task.getDrId(), task);
                    }
                    break;
                case TaskOpsType.DEACTIVATE_DR:
                    deactivateDrOps.put(task.getDrId(), task);
                    break;
                case TaskOpsType.SOFT_DEACTIVATE_DR:
                    softDeactivateDrOps.put(task.getDrId(), task);
                    break;
                case TaskOpsType.ACTIVATE_SYNC_VSES:
                    List<OpsTask> subTask = DefaultObjectJsonParser.parseArray(task.getTaskList(), OpsTask.class);
                    if (subTask != null && subTask.size() > 0) {
                        for (OpsTask s : subTask) {
                            s.setId(task.getId());
                            s.setStatus(TaskStatus.DOING);
                            if (!activateVsOps.containsKey(s.getSlbVirtualServerId())
                                    || activateVsOps.get(s.getSlbVirtualServerId()).getVersion() < s.getVersion()) {
                                activateVsOps.put(s.getSlbVirtualServerId(), s);
                            }
                        }
                        aggSyncVsActivateTasks.put(task.getId(), subTask);
                    }
                    break;
            }
        }
    }

    private void aggSyncVsTaskCheck(Map<Long, List<Group>> groupReferrerOfBuildingVs, Map<Long, List<TrafficPolicy>> policyReferrerOfBuildingVs, Map<Long, Dr> drGroupMap) throws ValidationException {
        ValidationContext context = new ValidationContext();

        if (aggSyncVsActivateTasks.size() > 0) {
            for (List<OpsTask> tasks : aggSyncVsActivateTasks.values()) {
                Set<Long> vsIds = new HashSet<>();
                Set<Group> groups = new HashSet<>();
                Set<TrafficPolicy> policies = new HashSet<>();
                Set<Dr> drs = new HashSet<>();
                for (OpsTask task : tasks) {
                    vsIds.add(task.getSlbVirtualServerId());
                    if (groupReferrerOfBuildingVs.get(task.getSlbVirtualServerId()) != null) {
                        groups.addAll(groupReferrerOfBuildingVs.get(task.getSlbVirtualServerId()));
                    }
                    if (policyReferrerOfBuildingVs.get(task.getSlbVirtualServerId()) != null) {
                        policies.addAll(policyReferrerOfBuildingVs.get(task.getSlbVirtualServerId()));
                    }
                }
                for (Group g : groups) {
                    if (drGroupMap.get(g.getId()) != null) {
                        drs.add(drGroupMap.get(g.getId()));
                    }
                }
                validationFacade.validateSyngeneticVs(vsIds, groups, policies, drs, context);
                if (!context.shouldProceed()) {
                    throw new ValidationException("Sync Vs Task Validate Failed.Errors:" + context.getErrors());
                }
            }
        }
    }

    private void setTaskFail(OpsTask task, String failcause) throws Exception {
        task.setStatus(TaskStatus.FAIL);
        task.setFailCause(failcause);
        logger.warn("[Task Fail] OpsTask: Type[" + task.getOpsType() + " TaskId:" + task.getId() + "],FailCause:" + failcause);
        if (aggSubTasks.containsKey(task.getId())) {
            throw new Exception("Stop All Tasks.Because Of AGG Task Failed.TaskID:" + task.getId() + " Tasks:" + aggSubTasks.get(task.getId()).toString());
        }
    }

    public List<Long> getResources() {
        List<Long> resources = new ArrayList<>();
        Set<Long> tmp = new HashSet<>();
        for (OpsTask task : tasks) {
            if (task.getResources() != null) {
                tmp.add(Long.parseLong(task.getResources()));
            }
            tmp.add(task.getTargetSlbId());
            logger.info("[[taskId=" + task.getId() + "]]" + "Tasks are executing, TaskID [" + task.getId() + "]");
        }
        resources.addAll(tmp);
        Collections.sort(resources);
        return resources;
    }
}
