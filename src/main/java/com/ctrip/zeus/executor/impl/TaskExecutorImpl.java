package com.ctrip.zeus.executor.impl;

import com.ctrip.zeus.commit.entity.Commit;
import com.ctrip.zeus.exceptions.NginxProcessingException;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.executor.TaskExecutor;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.nginx.entity.NginxResponse;
import com.ctrip.zeus.service.build.BuildService;
import com.ctrip.zeus.service.commit.CommitService;
import com.ctrip.zeus.service.commit.util.CommitType;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.common.RulePhase;
import com.ctrip.zeus.service.model.common.ValidationContext;
import com.ctrip.zeus.service.nginx.NginxService;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.status.StatusOffset;
import com.ctrip.zeus.service.status.StatusService;
import com.ctrip.zeus.service.task.TaskService;
import com.ctrip.zeus.service.task.constant.TaskOpsType;
import com.ctrip.zeus.service.task.constant.TaskStatus;
import com.ctrip.zeus.service.version.ConfVersionService;
import com.ctrip.zeus.status.entity.UpdateStatusItem;
import com.ctrip.zeus.task.entity.OpsTask;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * Created by fanqq on 2015/7/31.
 */
@Component("taskExecutor")
public class TaskExecutorImpl implements TaskExecutor {

    @Resource
    private DbLockFactory dbLockFactory;
    @Resource
    private GroupRepository groupRepository;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private VirtualServerRepository virtualServerRepository;
    @Resource
    private EntityFactory entityFactory;
    @Resource
    private TaskService taskService;
    @Resource
    private BuildService buildService;
    @Resource
    private StatusService statusService;
    @Resource
    private NginxService nginxService;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private ConfVersionService confVersionService;
    @Resource
    private CommitService commitService;
    @Resource
    private TrafficPolicyRepository trafficPolicyRepository;
    @Resource
    private ValidationFacade validationFacade;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private static DynamicBooleanProperty writeEnable = DynamicPropertyFactory.getInstance().getBooleanProperty("write.enable", true);//"http://slberrorpages.ctripcorp.com/slberrorpages/500.htm");
    private static DynamicBooleanProperty healthyOpsActivate = DynamicPropertyFactory.getInstance().getBooleanProperty("healthy.operation.active", false);

    private HashMap<String, OpsTask> serverOps = new HashMap<>();
    private HashMap<Long, OpsTask> activateGroupOps = new HashMap<>();
    private HashMap<Long, OpsTask> deactivateGroupOps = new HashMap<>();
    private HashMap<Long, OpsTask> softDeactivateGroupOps = new HashMap<>();
    private HashMap<Long, OpsTask> activatePolicyOps = new HashMap<>();
    private HashMap<Long, OpsTask> deactivatePolicyOps = new HashMap<>();
    private HashMap<Long, OpsTask> softDeactivatePolicyOps = new HashMap<>();
    private HashMap<Long, OpsTask> activateVsOps = new HashMap<>();
    private HashMap<Long, OpsTask> deactivateVsOps = new HashMap<>();
    private HashMap<Long, OpsTask> softDeactivateVsOps = new HashMap<>();
    private HashMap<Long, OpsTask> activateSlbOps = new HashMap<>();
    private HashMap<Long, List<OpsTask>> memberOps = new HashMap<>();
    private HashMap<Long, List<OpsTask>> pullMemberOps = new HashMap<>();
    private HashMap<Long, List<OpsTask>> healthyOps = new HashMap<>();

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

            //1.1 slb
            slbMap = mapSlbVersionAndRevise(slbId, activateSlbOps.values());
            onlineSlb = slbMap.getOnlineMapping().get(slbId);
            //1.2 vs
            vsMap = mapVersionAndRevise(slbId, activateVsOps.values());
            //1.3 group
            Set<Long> relatedVsIds = new HashSet<>();
            relatedVsIds.addAll(vsMap.getOnlineMapping().keySet());
            relatedVsIds.addAll(vsMap.getOfflineMapping().keySet());
            groupMap = mapVersionAndRevise(relatedVsIds, activateGroupOps.values());
            //1.4 policy
            tpMap = mapPolicyVersionAndRevise(relatedVsIds, activatePolicyOps.values());

            //2. merge data and get next online entities
            Map<Long, Group> nxOnlineGroups;
            Map<Long, VirtualServer> nxOnlineVses;
            Map<Long, TrafficPolicy> nxOnlineTps;
            Slb nxOnlineSlb;

            //2.1 slb
            nxOnlineSlb = slbMap.getOfflineMapping().get(slbId);
            if (activateSlbOps.size() > 0) {
                if (!validateSlbModel(nxOnlineSlb)) {
                    nxOnlineSlb = onlineSlb;
                }
            }
            if (nxOnlineSlb == null) {
                throw new ValidationException("Not found activated slb version.SlbId:" + slbId);
            }

            //2.2 vs
            nxOnlineVses = vsMap.getOnlineMapping();
            for (Long vsId : activateVsOps.keySet()) {
                nxOnlineVses.put(vsId, vsMap.getOfflineMapping().get(vsId));
            }
            if (!validateVsModel(slbId, nxOnlineVses, vsMap.getOnlineMapping(), 3)) {
                throw new ValidationException("[!!!]Validate Vs Model Failed.");
            }
            //2.3 group
            nxOnlineGroups = new HashMap<>(groupMap.getOnlineMapping());
            for (Long gid : activateGroupOps.keySet()) {
                Group offlineVersion = groupMap.getOfflineMapping().get(gid);
                nxOnlineGroups.put(gid, offlineVersion);
            }
            //2.4 policy
            nxOnlineTps = new HashMap<>(tpMap.getOnlineMapping());
            for (Long pid : activatePolicyOps.keySet()) {
                TrafficPolicy offlineVersion = tpMap.getOfflineMapping().get(pid);
                nxOnlineTps.put(pid, offlineVersion);
            }
            if (nxOnlineTps.size() > 0) {
                nxOnlineSlb.addRule(new Rule().setPhaseId(RulePhase.HTTP_INIT_BY_LUA.getId()).setName("init_randomseed"));
            }

            //3. find out vses which need build.
            //3.1 deactivate vs pre check
            if (deactivateVsOps.size() > 0) {
                deactivateVsPreCheck(vsMap.getOnlineMapping().keySet(), nxOnlineGroups, nxOnlineTps);
            }
            //3.2 find out vses which need build.
            Set<Long> buildingVsIds;
            Set<Long> buildingGroupIds = new HashSet<>();
            Set<Long> buildingPolicyIds = new HashSet<>();
            Map<Long, List<Group>> groupReferrerOfBuildingVs = null;
            Map<Long, List<TrafficPolicy>> policyReferrerOfBuildingVs = null;
            Set<Long> buildingVsByDemand;

            // build all
            buildingVsByDemand = filterBuildingVsByDemand(nxOnlineVses, groupMap.getOnlineMapping(), nxOnlineGroups, tpMap.getOnlineMapping(), nxOnlineTps);
            if (activateSlbOps.size() > 0 && activateSlbOps.get(slbId) != null) {
                buildingVsIds = new HashSet<>(nxOnlineVses.keySet());
            } else {
                buildingVsIds = buildingVsByDemand;
            }
            Set<Long> buildingVsIdsTmp = null;
            boolean flag = true;
            for (int i = 0; i < 2; i++) {
                groupReferrerOfBuildingVs = new HashMap<>();
                policyReferrerOfBuildingVs = new HashMap<>();
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

                for (Long vsId : buildingVsIdsTmp) {
                    flag = flag & validateEntriesOnVs(vsId, groupMap.getOnlineMapping(), nxOnlineGroups, tpMap.getOnlineMapping(),
                            nxOnlineTps, groupReferrerOfBuildingVs, policyReferrerOfBuildingVs);
                }
                if (flag) break;
            }
            if (!flag) {
                throw new ValidationException("Entries Validation On Vs Failed.Building VsIds: " + buildingVsIdsTmp.toString());
            }
            buildingVsIds.addAll(buildingVsIdsTmp);

            //3.* in case of no need to update the config files.
            //only have operation for inactivated groups.
            if (activateSlbOps.size() == 0 && buildingVsIds.size() == 0 && deactivateVsOps.size() == 0 && softDeactivateVsOps.size() == 0) {
                performTasks(groupMap.getOfflineMapping());
                setTaskResult(slbId, true, null);
                return;
            }

            //5.2 push config to all slb servers. reload if needed.
            //5.2.1 remove deactivate vs ids from need build vses
            Set<Long> cleanVsIds = new HashSet<>();
            cleanVsIds.addAll(deactivateVsOps.keySet());
            cleanVsIds.addAll(softDeactivateVsOps.keySet());

            buildingVsIds.removeAll(cleanVsIds);

            //4. build config
            //4.1 get allDownServers
            Set<String> allDownServers = getAllDownServer();
            //4.2 allUpGroupServers
            Set<String> allUpGroupServers = getAllUpGroupServers(nxOnlineGroups);
            //4.3 build config
            buildVersion = buildService.build(nxOnlineSlb, nxOnlineVses, buildingVsIds, cleanVsIds, policyReferrerOfBuildingVs,
                    groupReferrerOfBuildingVs, allDownServers, allUpGroupServers);

            //5. push config
            //5.1 need reload?
            boolean needReload = activateSlbOps.size() > 0
                    || activateGroupOps.size() > 0 || deactivateGroupOps.size() > 0 || softDeactivateGroupOps.size() > 0
                    || activateVsOps.size() > 0 || deactivateVsOps.size() > 0 || softDeactivateVsOps.size() > 0
                    || activatePolicyOps.size() > 0 || deactivatePolicyOps.size() > 0 || softDeactivatePolicyOps.size() > 0;

            if (writeEnable.get()) {
                //5.2.2 update slb current version
                confVersionService.updateSlbCurrentVersion(slbId, buildVersion);
                //5.2.3 add commit
                addCommit(slbId, needReload, buildVersion, buildingVsIds, cleanVsIds, buildingGroupIds);
                //5.2.4 fire update job
                needRollbackConf = true;
                NginxResponse response = nginxService.updateConf(nxOnlineSlb.getSlbServers());
                if (!response.getSucceed()) {
                    throw new Exception("Update config Fail.Fail Response:" + String.format(NginxResponse.JSON, response));
                }
            }
            performTasks(groupMap.getOfflineMapping());
            setTaskResult(slbId, true, null);
        } catch (Exception e) {
            // failed
            StringWriter out = new StringWriter(512);
            PrintWriter printWriter = new PrintWriter(out);
            e.printStackTrace(printWriter);
            String failCause = e.getMessage() + out.getBuffer().toString();
            setTaskResult(slbId, false, failCause.length() > 1024 ? failCause.substring(0, 1024) : failCause);
            rollBack(onlineSlb, buildVersion, needRollbackConf);
            throw e;
        }

    }

    private boolean validateVsModel(Long slbId, Map<Long, VirtualServer> nxOnlineVs, Map<Long, VirtualServer> onlineMap, int retry) {
        if (retry <= 0) return false;
        ValidationContext context = new ValidationContext();
        validationFacade.validateVsesOnSlb(slbId, nxOnlineVs.values(), context);
        if (context.getErrorVses().size() > 0) {
            for (Long vsId : context.getErrorVses()) {
                if (activateVsOps.containsKey(vsId)) {
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
        boolean flag = true;
        ValidationContext context = new ValidationContext();
        validationFacade.validateEntriesOnVs(vsId, groupReferrerOfBuildingVs.get(vsId), policyReferrerOfBuildingVs.get(vsId), context);
        if (context.getErrorGroups().size() > 0) {
            for (Long gid : context.getErrorGroups()) {
                if (activateGroupOps.containsKey(gid) && !activateGroupOps.get(gid).isSkipValidate()) {
                    setTaskFail(activateGroupOps.get(gid), "Group/Entries Validation On Vs Failed. VsId:" + vsId + ";Error:" + context.getGroupErrorReason(gid));
                    activateGroupOps.remove(gid);
                    flag = false;
                    if (onlineGroups.containsKey(gid)) {
                        nxOnlineGroups.put(gid, onlineGroups.get(gid));
                    } else {
                        nxOnlineGroups.remove(gid);
                    }
                } else if (deactivateGroupOps.containsKey(gid) && !deactivateGroupOps.get(gid).isSkipValidate()) {
                    setTaskFail(deactivateGroupOps.get(gid), "Group/Entries Validation On Vs Failed. VsId:" + vsId + ";Error:" + context.getGroupErrorReason(gid));
                    deactivateGroupOps.remove(gid);
                    flag = false;
                }
            }
        }
        if (context.getErrorPolicies().size() > 0) {
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
                revisedVersion.add(new IdVersion(task.getId(), task.getVersion()));
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
                revisedVersion.add(new IdVersion(task.getId(), task.getVersion()));
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

    private Set<Long> filterBuildingVsByDemand(Map<Long, VirtualServer> nxOnlineVses,
                                               Map<Long, Group> currOnlineGroups, Map<Long, Group> nxOnlineGroups, Map<Long, TrafficPolicy> currOnlinePolicies, Map<Long, TrafficPolicy> nxOnlineTpes) {
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


        Set<Long> _buildingGroupIds = new HashSet<>(activateGroupOps.keySet());

        // groups with 2~n vses which share the current target slb-id(cond1)
        // while 1~(n-1) of those vses are involved in any vs-ops(cond2)
        // must be rebuilt for the need of regenerating concat upstream filename
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
                _buildingGroupIds.add(g.getId());
            }
        }
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
                _buildingGroupIds.add(g.getId());
            }
        }

        Set<Long> _buildingPolicyIds = new HashSet<>(activatePolicyOps.keySet());

        // policy with 2~n vses which share the current target slb-id(cond1)
        // while 1~(n-1) of those vses are involved in any vs-ops(cond2)
        // must be rebuilt for the need of regenerating concat upstream filename
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
                _buildingPolicyIds.add(p.getId());
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
                _buildingPolicyIds.add(p.getId());
            }
        }


        Set<Long> _buildingVsIds = new HashSet<>();
        for (Long gid : _buildingGroupIds) {
            Group currVersion = currOnlineGroups.get(gid);
            if (currVersion != null) {
                for (GroupVirtualServer gvs : currVersion.getGroupVirtualServers()) {
                    _buildingVsIds.add(gvs.getVirtualServer().getId());
                }
                for (GroupVirtualServer gvs : nxOnlineGroups.get(gid).getGroupVirtualServers()) {
                    _buildingVsIds.add(gvs.getVirtualServer().getId());
                }
            }
        }
        for (Long pid : _buildingPolicyIds) {
            TrafficPolicy currVersion = currOnlinePolicies.get(pid);
            if (currVersion != null) {
                for (PolicyVirtualServer pvs : currVersion.getPolicyVirtualServers()) {
                    _buildingVsIds.add(pvs.getVirtualServer().getId());
                }
                for (PolicyVirtualServer pvs : nxOnlineTpes.get(pid).getPolicyVirtualServers()) {
                    _buildingVsIds.add(pvs.getVirtualServer().getId());
                }
            }
        }

        _buildingVsIds.retainAll(nxOnlineVses.keySet());
        buildingVsIds.addAll(_buildingVsIds);

        return buildingVsIds;
    }

    private boolean traverseGroupContent(Long groupId, Group group, Long slbId,
                                         final Map<Long, VirtualServer> nxOnlineVses,
                                         Set<Long> buildingVsIds, Map<Long, List<Group>> groupReferrerOfBuildingVs) throws NginxProcessingException {
        if (group == null) {
            String errMsg = "Unexpected online group with null value. groupId=" + groupId + ".";
            logger.error(errMsg);
            throw new NginxProcessingException(errMsg);
        }

        boolean buildingGroupRequired = false;
        if (activateGroupOps.containsKey(groupId) || deactivateGroupOps.containsKey(groupId)
                || pullMemberOps.containsKey(groupId) || memberOps.containsKey(groupId) || healthyOps.containsKey(groupId)) {
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
                                          Set<Long> buildingVsIds, Map<Long, List<TrafficPolicy>> policyReferrerOfBuildingVs) throws NginxProcessingException {
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

    private void addCommit(Long slbId, boolean needReload, Long buildVersion, Set<Long> needBuildVses, Set<Long> cleanVsIds, Set<Long> needBuildGroups) throws Exception {
        Commit commit = new Commit();
        commit.setSlbId(slbId)
                .setType(needReload ? CommitType.COMMIT_TYPE_RELOAD : CommitType.COMMIT_TYPE_DYUPS)
                .setVersion(buildVersion);
        commit.getVsIds().addAll(needBuildVses);
        commit.getGroupIds().addAll(needBuildGroups);
        commit.getCleanvsIds().addAll(cleanVsIds);
        for (OpsTask t : tasks) {
            commit.addTaskId(t.getId());
        }
        commitService.add(commit);
    }

    private void deactivateVsPreCheck(Set<Long> currOnlineVses, Map<Long, Group> nxOnlineGroups, Map<Long, TrafficPolicy> nxOnlinePolicies) throws Exception {
        Set<Long> keySet = new HashSet<>(deactivateVsOps.keySet());
        for (Long id : keySet) {
            OpsTask task = deactivateVsOps.get(id);
            if (!currOnlineVses.contains(task.getSlbVirtualServerId())) {
                setTaskFail(task, "[Deactivate Vs] Vs is unactivated!");
                deactivateVsOps.remove(id);
            }
        }
        List<Long> activatingGroupIds = new ArrayList<>();
        for (Long gid : activateGroupOps.keySet()) {
            Group group = nxOnlineGroups.get(gid);
            for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
                if (deactivateVsOps.containsKey(gvs.getVirtualServer().getId())) {
                    if (!activateGroupOps.containsKey(gid)) {
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
                slbRepository.updateStatus(new IdVersion[]{newSlbStatus});
            }

            // update vs status
            List<IdVersion> newVsStatus = new ArrayList<>();
            for (OpsTask task : activateVsOps.values()) {
                if (!task.getStatus().equals(TaskStatus.DOING)) {
                    continue;
                }
                newVsStatus.add(new IdVersion(task.getSlbVirtualServerId(), task.getVersion()));
            }
            for (OpsTask task : deactivateVsOps.values()) {
                if (!task.getStatus().equals(TaskStatus.DOING)) {
                    continue;
                }
                newVsStatus.add(new IdVersion(task.getSlbVirtualServerId(), 0));
            }
            virtualServerRepository.updateStatus(newVsStatus.toArray(new IdVersion[newVsStatus.size()]));

            // update group status
            List<IdVersion> newGroupStatus = new ArrayList<>();
            for (OpsTask task : activateGroupOps.values()) {
                if (!task.getStatus().equals(TaskStatus.DOING)) {
                    continue;
                }
                newGroupStatus.add(new IdVersion(task.getGroupId(), task.getVersion()));
            }
            for (OpsTask task : deactivateGroupOps.values()) {
                if (!task.getStatus().equals(TaskStatus.DOING)) {
                    continue;
                }
                newGroupStatus.add(new IdVersion(task.getGroupId(), 0));
            }
            groupRepository.updateStatus(newGroupStatus.toArray(new IdVersion[newGroupStatus.size()]));

            // update policy status
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

            // update server status
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

            // update member op status
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

    private UpdateStatusItem createUpdateStatusItem(Group group, OpsTask task, int statusOffset) {
        if (!task.getStatus().equals(TaskStatus.DOING)) {
            return null;
        }
        if (group == null) {
            setTaskFail(task, "Not Found Group Id:" + task.getGroupId());
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
        for (OpsTask task : tasks) {
            if (task.getStatus().equals(TaskStatus.DOING)) {
                if (isSuc) {
                    task.setStatus(TaskStatus.SUCCESS);
                } else {
                    task.setStatus(TaskStatus.FAIL);
                    task.setFailCause(failCause);
                    logger.warn("TaskFail", "Task:" + String.format(OpsTask.JSON, task) + "FailCause:" + failCause);
                }
            }
        }
        try {
            taskService.updateTasks(tasks);
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
        serverOps.clear();
        memberOps.clear();
        pullMemberOps.clear();
        healthyOps.clear();


        List<OpsTask> taskList;
        for (OpsTask task : pendingTasks) {
            task.setStatus(TaskStatus.DOING);
            switch (task.getOpsType()) {
                case TaskOpsType.ACTIVATE_GROUP:
                    activateGroupOps.put(task.getGroupId(), task);
                    break;
                case TaskOpsType.ACTIVATE_SLB:
                    activateSlbOps.put(task.getSlbId(), task);
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
                    activateVsOps.put(task.getSlbVirtualServerId(), task);
                    break;
                case TaskOpsType.ACTIVATE_POLICY:
                    activatePolicyOps.put(task.getPolicyId(), task);
                    break;
                case TaskOpsType.DEACTIVATE_POLICY:
                    deactivatePolicyOps.put(task.getPolicyId(), task);
                    break;
                case TaskOpsType.SOFT_DEACTIVATE_POLICY:
                    softDeactivatePolicyOps.put(task.getPolicyId(), task);
                    break;
            }
        }
    }

    private void setTaskFail(OpsTask task, String failcause) {
        task.setStatus(TaskStatus.FAIL);
        task.setFailCause(failcause);
        logger.warn("[Task Fail] OpsTask: Type[" + task.getOpsType() + " TaskId:" + task.getId() + "],FailCause:" + failcause);
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
