package com.ctrip.zeus.executor.impl;

import com.ctrip.zeus.exceptions.NginxProcessingException;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.executor.ModelSnapshotBuilder;
import com.ctrip.zeus.executor.entity.TasksEntity;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.model.commit.Commit;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.model.nginx.NginxResponse;
import com.ctrip.zeus.model.status.UpdateStatusItem;
import com.ctrip.zeus.model.task.OpsTask;
import com.ctrip.zeus.restful.message.view.ExtendedView;
import com.ctrip.zeus.restful.message.view.ViewDecorator;
import com.ctrip.zeus.service.build.BuildInfoService;
import com.ctrip.zeus.service.build.BuildService;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.build.ModelSnapshotService;
import com.ctrip.zeus.service.commit.CommitService;
import com.ctrip.zeus.service.commit.util.CommitType;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.common.ErrorType;
import com.ctrip.zeus.service.model.common.RulePhase;
import com.ctrip.zeus.service.model.common.ValidationContext;
import com.ctrip.zeus.service.model.snapshot.ModelEntities;
import com.ctrip.zeus.service.model.snapshot.ModelSnapshotEntity;
import com.ctrip.zeus.service.nginx.NginxService;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.service.status.StatusOffset;
import com.ctrip.zeus.service.status.StatusService;
import com.ctrip.zeus.service.task.TaskService;
import com.ctrip.zeus.service.task.constant.TaskStatus;
import com.ctrip.zeus.service.version.ConfVersionService;
import com.ctrip.zeus.support.DefaultObjectJsonWriter;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.util.ObjectJsonWriter;
import com.ctrip.zeus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

;

@Service("modelSnapshotBuilder")
public class ModelSnapshotBuilderImpl implements ModelSnapshotBuilder {
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
    @Resource
    private ModelSnapshotService modelSnapshotService;
    @Resource
    private ViewDecorator viewDecorator;
    @Resource
    private BuildInfoService buildInfoService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void build(Long slbId) {
        List<OpsTask> tasks = null;
        DistLock buildLock = null;
        List<DistLock> resLocks = new ArrayList<>();
        boolean flag = false;
        long start = System.currentTimeMillis();
        try {
            buildLock = dbLockFactory.newLock("TaskWorker_" + slbId);
            if (flag = buildLock.tryLock()) {
                tasks = fetchTask(slbId);
                if (null == tasks || tasks.isEmpty()) {
                    return;
                }
                List<Long> resources = getResourcesSlbIds(tasks);
                for (Long res : resources) {
                    DistLock resLock = dbLockFactory.newLock("TaskRes_" + res);
                    if (resLock.tryLock()) {
                        resLocks.add(resLock);
                    } else {
                        throw new Exception("Get Resources Failed! ResourceId : " + res);
                    }
                }
                execute(slbId, tasks);
            } else {
                logger.warn("[[Fail=LockFailed]] SlbId:" + slbId);
            }
        } catch (Exception e) {
            logger.warn("[[Fail=BuildFailed]]Build Models Failed! SlbId: " + slbId, e);
        } finally {
            taskExecutorLog(tasks, slbId, System.currentTimeMillis() - start);
            for (DistLock lock : resLocks) {
                lock.unlock();
            }
            if (flag) {
                buildLock.unlock();
            }
        }
    }

    private void execute(Long slbId, List<OpsTask> tasks) throws Exception {
        TasksEntity tasksEntity = new TasksEntity(tasks);
        ModelSnapshotEntity entity = new ModelSnapshotEntity();
        logger.info("[Model Snapshot Test]Execute Start");
        Slb onlineSlb = null;
        Long buildVersion = null;
        boolean needRollbackConf = false;
        try {
            /**
             * 1. full access data from database and revise offline version by tasks
             * Get All Models Data From DB.
             */
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
            //Revise Model Start
            slbMap = mapSlbVersionAndRevise(slbId, tasksEntity.getActivateSlbOps().values());
            onlineSlb = slbMap.getOnlineMapping().get(slbId);
            vsMap = mapVsVersionAndRevise(slbId, tasksEntity.getActivateVsOps().values());
            Set<Long> relatedVsIds = new HashSet<>();
            relatedVsIds.addAll(vsMap.getOnlineMapping().keySet());
            relatedVsIds.addAll(vsMap.getOfflineMapping().keySet());
            groupMap = mapGroupVersionAndRevise(relatedVsIds, tasksEntity.getActivateGroupOps().values());
            tpMap = mapPolicyVersionAndRevise(relatedVsIds, tasksEntity.getActivatePolicyOps().values());
            drMap = mapDrVersionAndRevise(relatedVsIds, tasksEntity.getActivateDrOps().values());
            //Revise Model End

            //Get Next Online Version. And Validate Vs&Slb Models.
            Map<Long, Group> nxOnlineGroups = getNextOnlineGroups(groupMap, tasksEntity);
            Map<Long, Group> canaryGroups = getCanaryGroups(groupMap, tasksEntity);
            entity.setCanaryGroups(toExtendedGroupMap(canaryGroups));
            Map<Long, VirtualServer> nxOnlineVses = getNextOnlineVses(vsMap, slbId, tasksEntity);
            Slb nxOnlineSlb = getNextOnlineSlb(slbId, slbMap, onlineSlb, tasksEntity);
            Map<Long, TrafficPolicy> nxOnlineTps = getNextOnlinePolicies(tpMap, nxOnlineSlb, tasksEntity);
            Map<Long, Dr> nxOnlineDrs = getNextOnlineDrs(drMap, tasksEntity);

            // default rules
            List<Rule> defaultRules = ruleRepository.getDefaultRules();

            //3. find out vses which need build.
            //3.1 deactivate vs pre check
            if (tasksEntity.getDeactivateVsOps().size() > 0) {
                deactivateVsPreCheck(tasksEntity, nxOnlineGroups, nxOnlineTps, nxOnlineDrs);
            }
            //3.2 find out vses which need build.

            if (tasksEntity.getActivateSlbOps().size() > 0 && tasksEntity.getActivateSlbOps().get(slbId) != null) {
                buildingVsIds = new HashSet<>(nxOnlineVses.keySet());
            } else {
                buildingVsIds = filterBuildingVsByDemand(tasksEntity, nxOnlineVses, groupMap.getOnlineMapping(), nxOnlineGroups, tpMap.getOnlineMapping(), nxOnlineTps);
            }

            Map<Long, Map<Long, Map<Long, Integer>>> drDesSlbByGvses = new HashMap<>();
            Map<Long, Dr> drByGroupIds = new HashMap<>();

            updateBuildingVses(tasksEntity, groupReferrerOfBuildingVs, policyReferrerOfBuildingVs, drDesSlbByGvses, drByGroupIds, buildingVsIds,
                    nxOnlineGroups, nxOnlineVses, nxOnlineTps, nxOnlineDrs,
                    slbId, buildingGroupIds, buildingPolicyIds, buildingDrIds,
                    tpMap, groupMap, drMap, canaryGroups);

            // Check For Vs Merge Or Split
            aggSyncVsTaskCheck(tasksEntity, groupReferrerOfBuildingVs, policyReferrerOfBuildingVs, drByGroupIds);

            //3.* in case of no need to update the config files.
            //only have operation for inactivated groups.
            if (tasksEntity.getActivateSlbOps().size() == 0 && buildingVsIds.size() == 0 && tasksEntity.getDeactivateVsOps().size() == 0 && tasksEntity.getSoftDeactivateVsOps().size() == 0) {
                performTasks(tasksEntity, groupMap.getOfflineMapping(), slbId);
                setTaskResult(tasksEntity, slbId, true, null);
                return;
            }

            //4.0 remove deactivate vs ids from need build vses
            Set<Long> cleanVsIds = new HashSet<>();
            cleanVsIds.addAll(tasksEntity.getDeactivateVsOps().keySet());
            cleanVsIds.addAll(tasksEntity.getSoftDeactivateVsOps().keySet());
            buildingVsIds.removeAll(cleanVsIds);


            //4. build config
            Set<String> allDownServers = getAllDownServer(tasksEntity);
            Set<String> allUpGroupServers = getAllUpGroupServers(tasksEntity, nxOnlineGroups);
            List<Long> tmpGids = new ArrayList<>();
            for (List<Group> groupList : groupReferrerOfBuildingVs.values()) {
                for (Group g : groupList) {
                    tmpGids.add(g.getId());
                }
            }
            Map<Long, String> canaryIpMap = getCanaryIpMap(tmpGids);
            logger.info("[Model Snapshot Test]Finished Prepare Data:");
            //4.4 build config
            if (configHandler.getEnable("model.snapshot.build.nginx.conf", slbId, null, null, true)) {
                buildVersion = buildService.build(nxOnlineSlb, nxOnlineVses, buildingVsIds, cleanVsIds, policyReferrerOfBuildingVs, groupReferrerOfBuildingVs, drDesSlbByGvses, drByGroupIds, allDownServers, allUpGroupServers, canaryIpMap, defaultRules);
            } else {
                buildVersion = (long) buildInfoService.getTicket(nxOnlineSlb.getId());
            }

            logger.info("[Model Snapshot Test]Finished Build Conf:");
            //5. push config
            boolean softReload = (tasksEntity.getActivateSlbOps().size() + tasksEntity.getActivateGroupOps().size() + tasksEntity.getDeactivateGroupOps().size() +
                    tasksEntity.getSoftDeactivateGroupOps().size() + tasksEntity.getActivateVsOps().size() + tasksEntity.getDeactivateVsOps().size() +
                    tasksEntity.getSoftDeactivateVsOps().size() + tasksEntity.getActivatePolicyOps().size() + tasksEntity.getDeactivatePolicyOps().size() + tasksEntity.getSoftDeactivatePolicyOps().size() +
                    tasksEntity.getSoftDeactivateDrOps().size() + tasksEntity.getDeactivateDrOps().size() + tasksEntity.getActivateDrOps().size() +
                    tasksEntity.getActivateCanaryGroupOps().size()) > 0;
            boolean fullUpdate = tasksEntity.getActivateSlbOps().size() > 0 && tasksEntity.getActivateSlbOps().get(slbId) != null;

            Commit commit = addCommit(tasksEntity, slbId, fullUpdate, softReload, buildVersion, buildingVsIds, cleanVsIds, buildingGroupIds);

            //Build Model Snapshot
            entity.setCommits(commit);
            entity.setFullUpdate(fullUpdate);

            ModelEntities modelEntities = new ModelEntities();
            entity.setModels(modelEntities);
            entity.setTargetSlbId(slbId);
            logger.info("[Model Snapshot Test]Start Add Entity");
            Map<Long, Slb> allSlbs = entityFactory.getSlbsByIds(slbCriteriaQuery.queryAll().toArray(new Long[0])).getOnlineMapping();
            allSlbs.put(slbId, nxOnlineSlb);
            modelEntities.setSlbs(toExtendedSlb(allSlbs));
            modelEntities.setDefaultRules(defaultRules);
            modelEntities.setIncrementalVses(buildingVsIds);
            modelEntities.setRemoveVsIds(cleanVsIds);
            modelEntities.setGroupIdDrMap(toExtendedDr(drByGroupIds));
            modelEntities.setVsIdSourceGroupIdTargetSlbIdWeightMap(drDesSlbByGvses);
            Set<Long> nxOnlineVsIds = new HashSet<>(nxOnlineVses.size());
            nxOnlineVses.forEach((id, vs) -> nxOnlineVsIds.add(id));
            modelEntities.setAllNxOnlineVsIds(nxOnlineVsIds);
            if (configHandler.getEnable("snapshot.incremental", slbId, null, null, false)) {
                // vses
                Map<Long, VirtualServer> buildingVses = new HashMap<>();
                for (Long vsId: buildingVsIds) {
                    if (!nxOnlineVses.containsKey(vsId)) {
                        logger.warn("nxOnlineVses doesn't contain building vs. VsId: " + vsId);
                        continue;
                    }
                    buildingVses.put(vsId, nxOnlineVses.get(vsId));
                }
                modelEntities.setVses(toExtendedVs(buildingVses));

                // groupReferees
                Map<Long, List<Group>> groupReferees = new HashMap<>(buildingVsIds.size());
                for (Long vsId: buildingVsIds) {
                    if (!groupReferrerOfBuildingVs.containsKey(vsId)) {
                        logger.warn("groupReferrerOfBuildingVs doesn't contain building vs. VsId: " + vsId);
                        continue;
                    }
                    groupReferees.put(vsId, groupReferrerOfBuildingVs.get(vsId));
                }
                modelEntities.setGroupReferrerOfVses(toExtendedGroup(groupReferees));
                // policyReferees
                Map<Long, List<TrafficPolicy>> policyReferees = new HashMap<>(buildingVsIds.size());
                for (Long vsId: buildingVsIds) {
                    if (!policyReferrerOfBuildingVs.containsKey(vsId)) {
                        logger.warn("policyReferrerOfBuildingVs doesn't contain building vs. VsId: " + vsId);
                        continue;
                    }
                    policyReferees.put(vsId, policyReferrerOfBuildingVs.get(vsId));
                }
                modelEntities.setPolicyReferrerOfVses(toExtendedPolicy(policyReferees));
            } else {
                modelEntities.setVses(toExtendedVs(nxOnlineVses));
                modelEntities.setGroupReferrerOfVses(toExtendedGroup(groupReferrerOfBuildingVs));
                modelEntities.setPolicyReferrerOfVses(toExtendedPolicy(policyReferrerOfBuildingVs));
            }
            entity.setAllDownServers(allDownServers);
            entity.setAllUpGroupServers(allUpGroupServers);
            entity.setVersion(buildVersion);
            logger.info("[Model Snapshot Test] Add Entity");
            modelSnapshotService.add(entity);
            confVersionService.updateSlbCurrentVersion(slbId, buildVersion);

            logger.info("[Model Snapshot Test]Finished Add Entity");

            needRollbackConf = true;
            NginxResponse response = nginxService.updateConf(nxOnlineSlb.getSlbServers());
            if (!response.getSucceed()) {
                throw new Exception("Update config Fail.Fail Response:" + ObjectJsonWriter.write(response));
            }
            logger.info("[Model Snapshot Test]Finished Update Conf");
            performTasks(tasksEntity, groupMap.getOfflineMapping(), slbId);
            logger.info("[Model Snapshot Test]Finished Perform Tasks");
            setTaskResult(tasksEntity, slbId, true, null);
        } catch (Exception e) {
            StringWriter out = new StringWriter(512);
            PrintWriter printWriter = new PrintWriter(out);
            e.printStackTrace(printWriter);
            String failCause = e.getMessage() + out.getBuffer().toString();
            setTaskResult(tasksEntity, slbId, false, failCause.length() > 1024 ? failCause.substring(0, 1024) : failCause);
            rollBack(onlineSlb, buildVersion, needRollbackConf);
            throw e;
        }

    }

    private Map<Long, ExtendedView.ExtendedDr> toExtendedDr(Map<Long, Dr> drByGroupIds) {
        Map<Long, ExtendedView.ExtendedDr> result = new HashMap<>();
        List<ExtendedView.ExtendedDr> tmpList = new ArrayList<>();
        for (Long gId : drByGroupIds.keySet()) {
            ExtendedView.ExtendedDr tmp = new ExtendedView.ExtendedDr(drByGroupIds.get(gId));
            tmpList.add(tmp);
            result.put(gId, tmp);
        }
        viewDecorator.decorate(tmpList, "dr");
        return result;
    }

    private Map<Long, ExtendedView.ExtendedGroup> toExtendedGroupMap(Map<Long, Group> canaryGroups) {
        Map<Long, ExtendedView.ExtendedGroup> result = new HashMap<>();
        List<ExtendedView.ExtendedGroup> tmpList = new ArrayList<>();
        canaryGroups.values().forEach(e -> tmpList.add(new ExtendedView.ExtendedGroup(e)));
        viewDecorator.decorate(tmpList, "group");
        tmpList.forEach(e -> result.put(e.getId(), e));
        return result;
    }

    private Map<Long, List<ExtendedView.ExtendedGroup>> toExtendedGroup(Map<Long, List<Group>> groupReferrerOfBuildingVs) {
        Map<Long, List<ExtendedView.ExtendedGroup>> result = new HashMap<>();
        Map<Long, ExtendedView.ExtendedGroup> extendedGroupMap = new HashMap<>();
        for (Long id : groupReferrerOfBuildingVs.keySet()) {
            List<Group> list = groupReferrerOfBuildingVs.get(id);
            for (Group group : list) {
                extendedGroupMap.putIfAbsent(group.getId(), new ExtendedView.ExtendedGroup(group));
            }
        }
        List<ExtendedView.ExtendedGroup> extendedList = new ArrayList<>(extendedGroupMap.values());
        viewDecorator.decorate(extendedList, "group");
        extendedList.forEach(e -> extendedGroupMap.put(e.getId(), e));
        for (Long id : groupReferrerOfBuildingVs.keySet()) {
            List<Group> list = groupReferrerOfBuildingVs.get(id);
            List<ExtendedView.ExtendedGroup> tmpList = new ArrayList<>();
            for (Group group : list) {
                tmpList.add(extendedGroupMap.get(group.getId()));
            }
            result.put(id, tmpList);
        }
        return result;
    }

    private Map<Long, List<ExtendedView.ExtendedTrafficPolicy>> toExtendedPolicy(Map<Long, List<TrafficPolicy>> policyReferrerOfBuildingVs) {
        Map<Long, List<ExtendedView.ExtendedTrafficPolicy>> result = new HashMap<>();
        Map<Long, ExtendedView.ExtendedTrafficPolicy> extendedPolicyMap = new HashMap<>();
        for (Long id : policyReferrerOfBuildingVs.keySet()) {
            List<TrafficPolicy> list = policyReferrerOfBuildingVs.get(id);
            for (TrafficPolicy policy : list) {
                extendedPolicyMap.putIfAbsent(policy.getId(), new ExtendedView.ExtendedTrafficPolicy(policy));
            }
        }
        List<ExtendedView.ExtendedTrafficPolicy> extendedList = new ArrayList<>(extendedPolicyMap.values());
        viewDecorator.decorate(extendedList, "policy");
        extendedList.forEach(e -> extendedPolicyMap.put(e.getId(), e));
        for (Long id : policyReferrerOfBuildingVs.keySet()) {
            List<TrafficPolicy> list = policyReferrerOfBuildingVs.get(id);
            List<ExtendedView.ExtendedTrafficPolicy> tmpList = new ArrayList<>();
            for (TrafficPolicy policy : list) {
                tmpList.add(extendedPolicyMap.get(policy.getId()));
            }
            result.put(id, tmpList);
        }
        return result;
    }

    private Map<Long, ExtendedView.ExtendedSlb> toExtendedSlb(Map<Long, Slb> allSlbs) {
        Map<Long, ExtendedView.ExtendedSlb> result = new HashMap<>();
        List<ExtendedView.ExtendedSlb> tmpList = new ArrayList<>();
        allSlbs.values().forEach(e -> tmpList.add(new ExtendedView.ExtendedSlb(e)));
        viewDecorator.decorate(tmpList, "slb");
        tmpList.forEach(e -> result.put(e.getId(), e));
        return result;
    }

    private Map<Long, ExtendedView.ExtendedVs> toExtendedVs(Map<Long, VirtualServer> nxOnlineVses) {
        Map<Long, ExtendedView.ExtendedVs> result = new HashMap<>();
        List<ExtendedView.ExtendedVs> tmpList = new ArrayList<>();
        nxOnlineVses.values().forEach(e -> tmpList.add(new ExtendedView.ExtendedVs(e)));
        viewDecorator.decorate(tmpList, "vs");
        tmpList.forEach(e -> result.put(e.getId(), e));
        return result;
    }

    private Map<Long, Group> getCanaryGroups(ModelStatusMapping<Group> groupMap, TasksEntity tasksEntity) throws Exception {
        Map<Long, Group> canaryGroup = new HashMap<>();
        for (OpsTask t : tasksEntity.getActivateCanaryGroupOps().values()) {
            if (groupMap.getOfflineMapping().get(t.getGroupId()).getVersion().equals(t.getVersion())) {
                canaryGroup.put(t.getGroupId(), groupMap.getOfflineMapping().get(t.getGroupId()));
            } else {
                canaryGroup.put(t.getGroupId(), groupRepository.getByKey(new IdVersion(t.getGroupId(), t.getVersion())));
            }
        }

        return canaryGroup;
    }

    private Set<String> getAllUpGroupServers(TasksEntity tasksEntity, Map<Long, Group> groups) throws Exception {
        Map<String, List<Boolean>> memberStatus = statusService.fetchGroupServerStatus(groups.keySet().toArray(new Long[]{}));
        Set<Long> tmpid = tasksEntity.getMemberOps().keySet();
        for (Long gid : tmpid) {
            if (!groups.containsKey(gid)) {
                /*group not activated. just skip it.*/
                continue;
            }
            List<OpsTask> taskList = tasksEntity.getMemberOps().get(gid);
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
        tmpid = tasksEntity.getPullMemberOps().keySet();
        for (Long gid : tmpid) {
            if (!groups.containsKey(gid)) {
                /*group not activated. just skip it.*/
                continue;
            }
            List<OpsTask> taskList = tasksEntity.getPullMemberOps().get(gid);
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

        tmpid = tasksEntity.getHealthyOps().keySet();
        for (Long gid : tmpid) {
            if (!groups.containsKey(gid)) {
                /*group not activated. just skip it.*/
                continue;
            }
            List<OpsTask> taskList = tasksEntity.getHealthyOps().get(gid);
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

    private Set<String> getAllDownServer(TasksEntity tasksEntity) throws Exception {
        Set<String> allDownServers = statusService.findAllDownServers();
        Set<String> serverip = tasksEntity.getServerOps().keySet();
        for (String ip : serverip) {
            if (allDownServers.contains(ip) && tasksEntity.getServerOps().get(ip).getUp()) {
                allDownServers.remove(ip);
            } else if (!allDownServers.contains(ip) && !tasksEntity.getServerOps().get(ip).getUp()) {
                allDownServers.add(ip);
            }
        }
        return allDownServers;
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

    private Commit addCommit(TasksEntity tasksEntity, Long slbId, boolean fullUpdate, boolean softReload, Long buildVersion, Set<Long> needBuildVses, Set<Long> cleanVsIds, Set<Long> needBuildGroups) throws Exception {
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
        for (OpsTask t : tasksEntity.getOrgTasks()) {
            commit.addTaskId(t.getId());
        }
        commitService.add(commit);
        return commit;
    }


    private void rollBack(Slb slb, Long buildVersion, boolean needRollbackConf) {
        try {
            if (buildVersion != null && buildVersion > 0) {
                commitService.removeCommit(slb.getId(), buildVersion);
                Long pre = confVersionService.getSlbPreviousVersion(slb.getId());
                if (pre > 0) {
                    confVersionService.updateSlbCurrentVersion(slb.getId(), pre);
                    buildService.rollBackConfig(slb.getId(), pre.intValue());
                    modelSnapshotService.rollBack(slb.getId(), pre);
                }
            }
            if (needRollbackConf) {
                nginxService.rollbackAllConf(slb.getSlbServers());
            }
        } catch (Exception e) {
            logger.error("RollBack Fail!", e);
        }
    }


    private void setTaskResult(TasksEntity tasksEntity, Long slbId, boolean isSuc, String failCause) throws Exception {
        List<OpsTask> updateTask = new ArrayList<>();
        for (OpsTask task : tasksEntity.getOrgTasks()) {
            if (task.getId() != null && !tasksEntity.getAggTasks().containsKey(task.getId())) {
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
        for (Long tid : tasksEntity.getAggTasks().keySet()) {
            String status = TaskStatus.SUCCESS;
            for (OpsTask t : tasksEntity.getAggSubTasks().get(tid)) {
                if (t.getStatus().equalsIgnoreCase(TaskStatus.FAIL)) {
                    status = TaskStatus.FAIL;
                    break;
                }
            }
            OpsTask task = tasksEntity.getAggTasks().get(tid);
            task.setStatus(status);
            task.setTaskList(DefaultObjectJsonWriter.write(tasksEntity.getAggSubTasks().get(tid)));
            updateTask.add(task);
        }
        try {
            taskService.updateTasks(updateTask);
        } catch (Exception e) {
            logger.error("Task Update Failed! TargetSlbId:" + slbId, e);
            throw new Exception("Task Update Failed! TargetSlbId:" + slbId, e);
        }
    }


    private void performTasks(TasksEntity tasksEntity, Map<Long, Group> offlineGroups, Long slbId) throws Exception {
        try {
            for (OpsTask task : tasksEntity.getActivateSlbOps().values()) {
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
            for (OpsTask task : tasksEntity.getActivateVsOps().values()) {
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
            for (OpsTask task : tasksEntity.getDeactivateVsOps().values()) {
                if (!task.getStatus().equals(TaskStatus.DOING)) {
                    continue;
                }
                newVsStatus.add(new IdVersion(task.getSlbVirtualServerId(), 0));
            }
            virtualServerRepository.updateStatus(newVsStatus.toArray(new IdVersion[newVsStatus.size()]));
            List<IdVersion> newGroupStatus = new ArrayList<>();
            List<DistLock> groupLocks = new ArrayList<>();
            try {
                for (OpsTask task : tasksEntity.getActivateGroupOps().values()) {
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
                for (OpsTask task : tasksEntity.getActivateGroupOps().values()) {
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
            for (OpsTask task : tasksEntity.getDeactivateGroupOps().values()) {
                if (!task.getStatus().equals(TaskStatus.DOING)) {
                    continue;
                }
                newGroupStatus.add(new IdVersion(task.getGroupId(), 0));
            }
            groupRepository.updateStatus(newGroupStatus.toArray(new IdVersion[newGroupStatus.size()]));

            List<IdVersion> canaryGroups = new ArrayList<>();
            for (OpsTask task : tasksEntity.getActivateCanaryGroupOps().values()) {
                if (!task.getStatus().equals(TaskStatus.DOING)) {
                    continue;
                }
                DistLock lock = dbLockFactory.newLock(task.getGroupId() + "_updateGroup");
                if (lock.tryLock()) {
                    try {
                        IdVersion[] idv = groupCriteriaQuery.queryByIdAndMode(task.getGroupId(), SelectionMode.ONLINE_EXCLUSIVE);
                        if (idv.length == 0 || task.getVersion() > idv[0].getVersion()) {
                            canaryGroups.add(new IdVersion(task.getGroupId(), task.getVersion()));
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            }
            groupRepository.updateCanaryStatus(canaryGroups.toArray(new IdVersion[canaryGroups.size()]));

            List<IdVersion> newPolicyStatus = new ArrayList<>();
            for (OpsTask task : tasksEntity.getActivatePolicyOps().values()) {
                if (!task.getStatus().equals(TaskStatus.DOING)) {
                    continue;
                }
                newPolicyStatus.add(new IdVersion(task.getPolicyId(), task.getVersion()));
            }
            for (OpsTask task : tasksEntity.getDeactivatePolicyOps().values()) {
                if (!task.getStatus().equals(TaskStatus.DOING)) {
                    continue;
                }
                newPolicyStatus.add(new IdVersion(task.getPolicyId(), 0));
            }
            trafficPolicyRepository.updateActiveStatus(newPolicyStatus.toArray(new IdVersion[newPolicyStatus.size()]));

            List<IdVersion> newDrStatus = new ArrayList<>();
            Set<Long> updatedDrIds = new HashSet<>();
            for (OpsTask task : tasksEntity.getActivateDrOps().values()) {
                if (!task.getStatus().equals(TaskStatus.DOING)) {
                    continue;
                }
                updatedDrIds.add(task.getDrId());
                newDrStatus.add(new IdVersion(task.getDrId(), task.getVersion()));
            }
            for (OpsTask task : tasksEntity.getDeactivateDrOps().values()) {
                if (!task.getStatus().equals(TaskStatus.DOING)) {
                    continue;
                }
                updatedDrIds.add(task.getDrId());
                newDrStatus.add(new IdVersion(task.getDrId(), 0));
            }
            //only softDeactivateDr tasks will be generated if remove all drTraffics
            for (OpsTask task : tasksEntity.getSoftDeactivateDrOps().values()) {
                if (!task.getStatus().equals(TaskStatus.DOING)) {
                    continue;
                }
                if (!updatedDrIds.contains(task.getDrId())) {
                    newDrStatus.add(new IdVersion(task.getDrId(), task.getVersion()));
                }
            }
            drRepository.updateActiveStatus(newDrStatus.toArray(new IdVersion[newDrStatus.size()]));
            for (OpsTask task : tasksEntity.getServerOps().values()) {
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
            for (List<OpsTask> taskList : tasksEntity.getMemberOps().values()) {
                for (OpsTask task : taskList) {
                    UpdateStatusItem item = createUpdateStatusItem(offlineGroups.get(task.getGroupId()), task, StatusOffset.MEMBER_OPS);
                    if (item == null) continue;
                    memberUpdates.add(item);
                }
            }
            statusService.updateStatus(memberUpdates);
            // update pull op status
            List<UpdateStatusItem> pullUpdates = new ArrayList<>();
            for (List<OpsTask> taskList : tasksEntity.getPullMemberOps().values()) {
                for (OpsTask task : taskList) {
                    UpdateStatusItem item = createUpdateStatusItem(offlineGroups.get(task.getGroupId()), task, StatusOffset.PULL_OPS);
                    if (item == null) continue;
                    pullUpdates.add(item);
                }
            }
            statusService.updateStatus(pullUpdates);
            // update hc op status
            List<UpdateStatusItem> healthyStatus = new ArrayList<>();
            for (List<OpsTask> taskList : tasksEntity.getHealthyOps().values()) {
                for (OpsTask task : taskList) {
                    UpdateStatusItem item = createUpdateStatusItem(offlineGroups.get(task.getGroupId()), task, StatusOffset.HEALTHY);
                    if (item == null) continue;
                    healthyStatus.add(item);
                }
            }
            statusService.updateStatus(healthyStatus);
        } catch (Exception e) {
            throw new Exception("Perform Tasks Fail! TargetSlbId:" + slbId, e);
        }

    }

    private UpdateStatusItem createUpdateStatusItem(Group group, OpsTask task, int statusOffset) throws Exception {
        if (!task.getStatus().equals(TaskStatus.DOING)) {
            return null;
        }
        if (group == null) {
            logger.warn("[ModelSnapshotBuilderImpl] Failed to get group id: " + task.getGroupId());
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

    /**
     * Check for Vs Split and Vs Merge Cases.
     *
     * @param tasksEntity
     * @param groupReferrerOfBuildingVs
     * @param policyReferrerOfBuildingVs
     * @param drGroupMap
     * @throws ValidationException
     */
    private void aggSyncVsTaskCheck(TasksEntity tasksEntity, Map<Long, List<Group>> groupReferrerOfBuildingVs, Map<Long, List<TrafficPolicy>> policyReferrerOfBuildingVs, Map<Long, Dr> drGroupMap) throws ValidationException {
        ValidationContext context = new ValidationContext();

        if (tasksEntity.getAggSyncVsActivateTasks().size() > 0) {
            for (List<OpsTask> tasks : tasksEntity.getAggSyncVsActivateTasks().values()) {
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


    /**
     * Find out incremental vses.
     * Group: Activate/Deactivate/MemberOp/ServerOp
     * Policy:Activate/Deactivate
     * Dr:Activate/Deactivate
     * Extra: Doing Validate On Vses. Dr&Policy&Group Validate
     *
     * @param tasksEntity
     * @param groupReferrerOfBuildingVs  Map<vsId,List<Group> Groups On Incremental Vses.
     * @param policyReferrerOfBuildingVs Map<vsId,List<Policy> Policies On Incremental Vses.
     * @param drDesSlbByGvses            Map<vsId,List<Dr> Drs On Incremental Vses.  Map<vsId,Map<sourceGroupId,Map<targetSlbId,weight>>>
     * @param drByGroupIds               Map<groupId,Dr>  Mapping for groupId => Dr
     * @param buildingVsIds              Result of Incremental Vses
     * @param nxOnlineGroups             input
     * @param nxOnlineVses               input
     * @param nxOnlineTps                input
     * @param nxOnlineDrs                input
     * @param slbId                      input
     * @param buildingGroupIds           Result of need build groups
     * @param buildingPolicyIds          Result of need build policies
     * @param buildingDrIds              Result of need build drs
     * @param tpMap                      input
     * @param groupMap                   input
     * @param drMap                      input
     * @param canaryGroups
     * @throws Exception
     */
    private void updateBuildingVses(TasksEntity tasksEntity, Map<Long, List<Group>> groupReferrerOfBuildingVs,
                                    Map<Long, List<TrafficPolicy>> policyReferrerOfBuildingVs,
                                    Map<Long, Map<Long, Map<Long, Integer>>> drDesSlbByGvses, Map<Long, Dr> drByGroupIds,
                                    Set<Long> buildingVsIds, Map<Long, Group> nxOnlineGroups,
                                    Map<Long, VirtualServer> nxOnlineVses,
                                    Map<Long, TrafficPolicy> nxOnlineTps,
                                    Map<Long, Dr> nxOnlineDrs,
                                    Long slbId, Set<Long> buildingGroupIds, Set<Long> buildingPolicyIds, Set<Long> buildingDrIds,
                                    ModelStatusMapping<TrafficPolicy> tpMap, ModelStatusMapping<Group> groupMap, ModelStatusMapping<Dr> drMap, Map<Long, Group> canaryGroups) throws Exception {
        Set<Long> buildingVsIdsTmp = null;
        boolean flag = true;
        for (int i = 0; i < 2; i++) {
            groupReferrerOfBuildingVs.clear();
            policyReferrerOfBuildingVs.clear();
            buildingVsIdsTmp = new HashSet<>(buildingVsIds);
            flag = true;
            for (Map.Entry<Long, Group> e : nxOnlineGroups.entrySet()) {
                boolean buildingRequired = traverseGroupContent(tasksEntity, e.getKey(), e.getValue(), slbId,
                        nxOnlineVses,
                        buildingVsIdsTmp, groupReferrerOfBuildingVs);
                if (buildingRequired) {
                    buildingGroupIds.add(e.getKey());
                }
            }
            for (Map.Entry<Long, TrafficPolicy> e : nxOnlineTps.entrySet()) {
                boolean buildingRequired = traversePolicyContent(tasksEntity,
                        e.getKey(), e.getValue(), slbId,
                        nxOnlineVses,
                        buildingVsIdsTmp, policyReferrerOfBuildingVs);
                if (buildingRequired) {
                    buildingPolicyIds.add(e.getKey());
                }
            }
            for (Map.Entry<Long, Dr> e : nxOnlineDrs.entrySet()) {
                boolean buildingRequired = traverseDrContent(tasksEntity, e.getKey(), e.getValue(), slbId,
                        buildingVsIdsTmp, buildingGroupIds,
                        nxOnlineVses, nxOnlineGroups);
                if (buildingRequired) {
                    buildingDrIds.add(e.getKey());
                }
            }
            for (Long vsId : buildingVsIdsTmp) {
                flag = flag & validateEntriesOnVs(tasksEntity, vsId, groupMap.getOnlineMapping(), nxOnlineGroups, tpMap.getOnlineMapping(),
                        nxOnlineTps, groupReferrerOfBuildingVs, policyReferrerOfBuildingVs);
            }
            flag = flag & getDrRelations(tasksEntity, buildingVsIdsTmp, buildingVsIds, drMap.getOnlineMapping(), nxOnlineDrs, nxOnlineGroups, nxOnlineVses, drDesSlbByGvses, drByGroupIds);
            if (flag) break;
        }
        if (!flag) {
            throw new ValidationException("Entries Validation On Vs Failed.Building VsIds: " + buildingVsIdsTmp.toString());
        }
        buildingVsIds.addAll(buildingVsIdsTmp);
    }

    private boolean validateEntriesOnVs(TasksEntity tasksEntity, Long vsId, Map<Long, Group> onlineGroups, Map<Long, Group> nxOnlineGroups,
                                        Map<Long, TrafficPolicy> onlineTpes, Map<Long, TrafficPolicy> nxOnlineTpes,
                                        Map<Long, List<Group>> groupReferrerOfBuildingVs,
                                        Map<Long, List<TrafficPolicy>> policyReferrerOfBuildingVs) throws Exception {
        boolean flag = true;
        ValidationContext context = new ValidationContext();
        validationFacade.validateEntriesOnVs(vsId, groupReferrerOfBuildingVs.get(vsId), policyReferrerOfBuildingVs.get(vsId), context);

        if (context.getErrorGroups().size() > 0) {
            validationFacade.validateSkipErrorsOfWhiteList("group", context);
            for (Long gid : context.getErrorGroups()) {
                context.ignoreGroupErrors(gid, ErrorType.ROOT_PATH_OVERLAP);
                if (tasksEntity.getActivateGroupOps().containsKey(gid) && !tasksEntity.getActivateGroupOps().get(gid).isSkipValidate()) {
                    ignoreExistedPathOverlapErrors(vsId, context, nxOnlineGroups, onlineGroups, gid);
                    boolean success = checkForRelatedGroup(context, vsId, nxOnlineGroups, nxOnlineGroups.get(gid))
                            && !context.getErrorGroups().contains(gid);
                    if (!success) {
                        tasksEntity.setTaskFail(tasksEntity.getActivateGroupOps().get(gid), "Group/Entries Validation On Vs Failed. VsId:" + vsId + ";Error:" + context.getGroupErrorReason(gid));
                        tasksEntity.getActivateGroupOps().remove(gid);
                        flag = false;
                        if (onlineGroups.containsKey(gid)) {
                            nxOnlineGroups.put(gid, onlineGroups.get(gid));
                        } else {
                            nxOnlineGroups.remove(gid);
                        }
                    }
                } else if (tasksEntity.getDeactivateGroupOps().containsKey(gid) && !tasksEntity.getDeactivateGroupOps().get(gid).isSkipValidate()) {
                    tasksEntity.setTaskFail(tasksEntity.getDeactivateGroupOps().get(gid), "Group/Entries Validation On Vs Failed. VsId:" + vsId + ";Error:" + context.getGroupErrorReason(gid));
                    tasksEntity.getDeactivateGroupOps().remove(gid);
                    flag = false;
                }
            }
        }
        if (context.getErrorPolicies().size() > 0) {
            validationFacade.validateSkipErrorsOfWhiteList("policy", context);
            for (Long pid : context.getErrorPolicies()) {
                if (tasksEntity.getActivatePolicyOps().containsKey(pid) && !tasksEntity.getActivatePolicyOps().get(pid).isSkipValidate()) {
                    tasksEntity.setTaskFail(tasksEntity.getActivatePolicyOps().get(pid), "Policy/Entries Validation On Vs Failed. VsId:" + vsId + ";Error:" + context.getPolicyErrorReason(pid));
                    tasksEntity.getActivatePolicyOps().remove(pid);
                    flag = false;
                    if (onlineTpes.containsKey(pid)) {
                        nxOnlineTpes.put(pid, onlineTpes.get(pid));
                    } else {
                        nxOnlineTpes.remove(pid);
                    }
                } else if (tasksEntity.getDeactivatePolicyOps().containsKey(pid) && !tasksEntity.getDeactivatePolicyOps().get(pid).isSkipValidate()) {
                    tasksEntity.setTaskFail(tasksEntity.getDeactivatePolicyOps().get(pid), "Policy/Entries Validation On Vs Failed. VsId:" + vsId + ";Error:" + context.getPolicyErrorReason(pid));
                    tasksEntity.getDeactivatePolicyOps().remove(pid);
                    flag = false;
                }
            }
        }
        return flag;
    }


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

    private boolean checkForRelatedGroup(ValidationContext context, Long vsId, Map<Long, Group> groups, Group group) throws Exception {
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
            return false;
        }
        validationFacade.validateSkipErrorsOfRelatedGroup(group, context);
        return true;
    }


    private boolean getDrRelations(TasksEntity tasksEntity, Set<Long> buildingVsIdsTmp, Set<Long> buildingVsIds, Map<Long, Dr> onlineDrs, Map<Long, Dr> nxOnlineDrs, Map<Long, Group> nxOnlineGroups, Map<Long, VirtualServer> nxOnlineVses,
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
            if (tasksEntity.getDeactivateDrOps().containsKey(drId)) {
                continue;
            }

            try {
                for (DrTraffic traffic : entry.getValue().getDrTraffics()) {
                    Long sourceGroupId = traffic.getGroup().getId();
                    for (Destination des : traffic.getDestinations()) {
                        Long sourceVsId = des.getVirtualServer().getId();
                        if ((tasksEntity.getSoftDeactivateDrOps().containsKey(drId) && tasksEntity.getSoftDeactivateDrOps().get(drId).getSlbVirtualServerId().equals(sourceVsId))
                                || (!buildingVsIds.contains(sourceVsId) && !buildingVsIdsTmp.contains(sourceVsId)))
                            continue;
                        Map<Long, Map<Long, Integer>> controlsByGroupId = drDesSlbByGvses.computeIfAbsent(sourceVsId, k -> new HashMap<>());
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
                if (tasksEntity.getActivateDrOps().containsKey(drId) && !tasksEntity.getActivateDrOps().get(drId).isSkipValidate()) {
                    tasksEntity.setTaskFail(tasksEntity.getActivateDrOps().get(drId), "Fetch destination slbId failed for dr:" + drId + ";Error:" + e.getMessage());
                    tasksEntity.getActivateDrOps().remove(drId);
                    if (onlineDrs.containsKey(drId)) {
                        nxOnlineDrs.put(drId, onlineDrs.get(drId));
                    } else {
                        nxOnlineDrs.remove(drId);
                    }
                } else if (tasksEntity.getDeactivateDrOps().containsKey(drId) && !tasksEntity.getDeactivateDrOps().get(drId).isSkipValidate()) {
                    tasksEntity.setTaskFail(tasksEntity.getDeactivateDrOps().get(drId), "Fetch destination slbId failed for dr:" + drId + ";Error:" + e.getMessage());
                    tasksEntity.getDeactivateDrOps().remove(drId);
                }
            }
        }
        return flag;
    }

    private boolean  traverseGroupContent(TasksEntity tasksEntity, Long groupId, Group group, Long slbId,
                                         final Map<Long, VirtualServer> nxOnlineVses,
                                         Set<Long> buildingVsIds, Map<Long, List<Group>> groupReferrerOfBuildingVs) throws Exception {
        if (group == null) {
            String errMsg = "Unexpected online group with null value. groupId=" + groupId + ".";
            logger.error(errMsg);
            throw new NginxProcessingException(errMsg);
        }

        boolean buildingGroupRequired = false;
        if (tasksEntity.getActivateGroupOps().containsKey(groupId) || tasksEntity.getDeactivateGroupOps().containsKey(groupId)) {
            buildingGroupRequired = true;
        } else if (tasksEntity.getPullMemberOps().containsKey(groupId) || tasksEntity.getMemberOps().containsKey(groupId) || tasksEntity.getHealthyOps().containsKey(groupId)) {
            buildingGroupRequired = true;
        } else if (tasksEntity.getActivateCanaryGroupOps().containsKey(groupId)) {
            buildingGroupRequired = true;
        }
        if (tasksEntity.getServerOps().size() > 0 && !buildingGroupRequired) {
            for (GroupServer gs : group.getGroupServers()) {
                if (tasksEntity.getServerOps().containsKey(gs.getIp())) {
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

                if (tasksEntity.getDeactivateGroupOps().containsKey(groupId)
                        || (tasksEntity.getSoftDeactivateGroupOps().containsKey(groupId) && tasksEntity.getSoftDeactivateGroupOps().get(groupId).getSlbVirtualServerId().equals(vsId))) {
                    continue;
                }

                List<Group> vsRelatedGroups = groupReferrerOfBuildingVs.computeIfAbsent(vsId, k -> new ArrayList<>());
                // give an empty list as long as vs is activated
                vsRelatedGroups.add(group);
            }
        }

        if (!currSlbRelevant) {
            //TODO elegantly solve issue of migrating vs and operating related groups simultaneously
            if (tasksEntity.getActivateGroupOps().containsKey(groupId)) {
                tasksEntity.setTaskFail(tasksEntity.getActivateGroupOps().get(groupId), "Not found online virtual server for Group[" + groupId + "] in slb[" + slbId + "].");
            } else if (tasksEntity.getDeactivateGroupOps().containsKey(groupId)) {
                tasksEntity.setTaskFail(tasksEntity.getDeactivateGroupOps().get(groupId), "Not found online virtual server for Group[" + groupId + "] in slb[" + slbId + "].");
            } else if (tasksEntity.getPullMemberOps().containsKey(groupId)) {
                for (OpsTask task : tasksEntity.getPullMemberOps().get(groupId)) {
                    tasksEntity.setTaskFail(task, "Not found online virtual server for Group[" + groupId + "] in slb[" + slbId + "].");
                }
            } else if (tasksEntity.getMemberOps().containsKey(groupId)) {
                for (OpsTask task : tasksEntity.getMemberOps().get(groupId)) {
                    tasksEntity.setTaskFail(task, "Not found online virtual server for Group[" + groupId + "] in slb[" + slbId + "].");
                }
            } else if (tasksEntity.getHealthyOps().containsKey(groupId)) {
                for (OpsTask task : tasksEntity.getHealthyOps().get(groupId)) {
                    tasksEntity.setTaskFail(task, "Not found online virtual server for Group[" + groupId + "] in slb[" + slbId + "].");
                }
            }
        }
        return buildingGroupRequired;
    }

    private boolean traversePolicyContent(TasksEntity tasksEntity, Long policyId, TrafficPolicy policy, Long slbId,
                                          final Map<Long, VirtualServer> nxOnlineVses,
                                          Set<Long> buildingVsIds, Map<Long, List<TrafficPolicy>> policyReferrerOfBuildingVs) throws Exception {
        if (policy == null) {
            String errMsg = "Unexpected online policy with null value. policyId =" + policyId + ".";
            logger.error(errMsg);
            throw new NginxProcessingException(errMsg);
        }

        boolean buildingPolicyRequired = false;
        if (tasksEntity.getActivatePolicyOps().containsKey(policyId) || tasksEntity.getDeactivatePolicyOps().containsKey(policyId)) {
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

                if (tasksEntity.getDeactivatePolicyOps().containsKey(policyId)
                        || (tasksEntity.getSoftDeactivatePolicyOps().containsKey(policyId) && tasksEntity.getSoftDeactivatePolicyOps().get(policyId).getSlbVirtualServerId().equals(vsId))) {
                    continue;
                }

                List<TrafficPolicy> vsRelatedPolicies = policyReferrerOfBuildingVs.computeIfAbsent(vsId, k -> new ArrayList<>());
                // give an empty list as long as vs is activated
                vsRelatedPolicies.add(policy);
            }
        }

        if (!currSlbRelevant) {
            //TODO elegantly solve issue of migrating vs and operating related groups simultaneously
            if (tasksEntity.getActivatePolicyOps().containsKey(policyId)) {
                tasksEntity.setTaskFail(tasksEntity.getActivatePolicyOps().get(policyId), "Not found online virtual server for Policy[" + policyId + "] in slb[" + slbId + "].");
            } else if (tasksEntity.getDeactivatePolicyOps().containsKey(policyId)) {
                tasksEntity.setTaskFail(tasksEntity.getDeactivatePolicyOps().get(policyId), "Not found online virtual server for Policy[" + policyId + "] in slb[" + slbId + "].");
            }
        }

        return buildingPolicyRequired;
    }

    private boolean traverseDrContent(TasksEntity tasksEntity, Long drId, Dr dr, Long slbId, Set<Long> buildingVsIds, Set<Long> buildingGroupIds, Map<Long, VirtualServer> nxOnlineVses, Map<Long, Group> nxOnlineGroups) throws Exception {
        if (dr == null) {
            String errMsg = "Unexpected online dr with null value. drId =" + drId + ".";
            logger.error(errMsg);
            throw new NginxProcessingException(errMsg);
        }

        boolean buildingRequired = false;
        if (tasksEntity.getActivateDrOps().containsKey(drId) || tasksEntity.getDeactivateDrOps().containsKey(drId)) {
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
            if (tasksEntity.getActivateDrOps().containsKey(drId)) {
                tasksEntity.setTaskFail(tasksEntity.getActivateDrOps().get(drId), "Not found online virtual server for Dr[" + drId + "] in slb[" + slbId + "].");
            } else if (tasksEntity.getDeactivateDrOps().containsKey(drId)) {
                tasksEntity.setTaskFail(tasksEntity.getDeactivateDrOps().get(drId), "Not found online virtual server for Dr[" + drId + "] in slb[" + slbId + "].");
            }
        }

        return buildingRequired;
    }


    /**
     * if vs is deactivating. Activate tasks of group/policy/dr belong to this vs should set failed.
     * Do not check whether vs is already deactivated.
     * Still Perform Task In Case Of Already Deactivated VS.
     * Case: Vs has multi slb relations. One Slb task executor already performed the deactivate vs task while another just start executor.
     * then the second executor should perform task anyway.
     *
     * @param tasksEntity
     * @param nxOnlineGroups
     * @param nxOnlinePolicies
     * @param nxOnlineDrs
     * @throws Exception
     */
    private void deactivateVsPreCheck(TasksEntity tasksEntity, Map<Long, Group> nxOnlineGroups,
                                      Map<Long, TrafficPolicy> nxOnlinePolicies, Map<Long, Dr> nxOnlineDrs) throws Exception {
        List<Long> activatingGroupIds = new ArrayList<>();
        for (Long gid : tasksEntity.getActivateGroupOps().keySet()) {
            Group group = nxOnlineGroups.get(gid);
            for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
                if (tasksEntity.getDeactivateVsOps().containsKey(gvs.getVirtualServer().getId())) {
                    if (!tasksEntity.getActivateGroupOps().containsKey(gid)) {
                        //todo: this will never happen
                        tasksEntity.setTaskFail(tasksEntity.getDeactivateVsOps().get(gvs.getVirtualServer().getId()), "[Deactivate Vs] Vs is has relative online group!GroupId:" + gid);
                        tasksEntity.getDeactivateVsOps().remove(gvs.getVirtualServer().getId());
                    } else {
                        activatingGroupIds.add(gid);
                    }
                }
            }
        }
        for (Long groupId : activatingGroupIds) {
            tasksEntity.setTaskFail(tasksEntity.getActivateGroupOps().get(groupId), "[Vs deactivate Pre Check] Activating Group While Related Vs is deactivating!");
            tasksEntity.getActivateGroupOps().remove(groupId);
        }

        List<Long> activatingPolicyIds = new ArrayList<>();
        for (Long pid : tasksEntity.getActivatePolicyOps().keySet()) {
            TrafficPolicy policy = nxOnlinePolicies.get(pid);
            for (PolicyVirtualServer pvs : policy.getPolicyVirtualServers()) {
                if (tasksEntity.getDeactivateVsOps().containsKey(pvs.getVirtualServer().getId())) {
                    activatingPolicyIds.add(pid);
                }
            }
        }
        for (Long pid : activatingPolicyIds) {
            tasksEntity.setTaskFail(tasksEntity.getActivatePolicyOps().get(pid), "[Vs deactivate Pre Check] Activating Policy While Related Vs is deactivating!");
            tasksEntity.getActivatePolicyOps().remove(pid);
        }

        List<Long> activatingDrIds = new ArrayList<>();
        for (Long drId : tasksEntity.getActivateDrOps().keySet()) {
            Dr dr = nxOnlineDrs.get(drId);
            for (DrTraffic traffic : dr.getDrTraffics()) {
                for (Destination des : traffic.getDestinations()) {
                    if (tasksEntity.getDeactivateVsOps().containsKey(des.getVirtualServer().getId())) {
                        activatingDrIds.add(drId);
                    }
                }
            }
        }
        for (Long drId : activatingDrIds) {
            tasksEntity.setTaskFail(tasksEntity.getActivateDrOps().get(drId), "[Vs deactivate Pre Check] Activating Dr While Related Vs is deactivating!");
            tasksEntity.getActivateDrOps().remove(drId);
        }
    }


    private Set<Long> filterBuildingVsByDemand(TasksEntity tasksEntity, Map<Long, VirtualServer> nxOnlineVses,
                                               Map<Long, Group> currOnlineGroups,
                                               Map<Long, Group> nxOnlineGroups,
                                               Map<Long, TrafficPolicy> currOnlinePolicies,
                                               Map<Long, TrafficPolicy> nxOnlineTpes) throws Exception {
        Set<Long> buildingVsIds = new HashSet<>(tasksEntity.getActivateVsOps().keySet());
        if (tasksEntity.getSoftDeactivateGroupOps().size() > 0) {
            for (OpsTask task : tasksEntity.getSoftDeactivateGroupOps().values()) {
                if (task.getSlbVirtualServerId() != null && nxOnlineVses.containsKey(task.getSlbVirtualServerId())) {
                    buildingVsIds.add(task.getSlbVirtualServerId());
                } else {
                    tasksEntity.setTaskFail(task, "Not found online vs for soft deactivate group ops. vs=" + task.getSlbVirtualServerId());
                }
            }
        }
        if (tasksEntity.getSoftDeactivatePolicyOps().size() > 0) {
            for (OpsTask task : tasksEntity.getSoftDeactivatePolicyOps().values()) {
                if (task.getSlbVirtualServerId() != null && nxOnlineVses.containsKey(task.getSlbVirtualServerId())) {
                    buildingVsIds.add(task.getSlbVirtualServerId());
                } else {
                    tasksEntity.setTaskFail(task, "Not found online vs for soft deactivate policy ops. vs=" + task.getSlbVirtualServerId());
                }
            }
        }
        if (tasksEntity.getSoftDeactivateDrOps().size() > 0) {
            for (OpsTask task : tasksEntity.getSoftDeactivateDrOps().values()) {
                if (task.getSlbVirtualServerId() != null && nxOnlineVses.containsKey(task.getSlbVirtualServerId())) {
                    buildingVsIds.add(task.getSlbVirtualServerId());
                } else {
                    tasksEntity.setTaskFail(task, "Not found online vs for soft deactivate dr ops. vs=" + task.getSlbVirtualServerId());
                }
            }
        }
        Set<Long> buildingGroupIds = new HashSet<>(tasksEntity.getActivateGroupOps().keySet());
        // groups with 2~n vses which share the current target slb-id(cond1)
        // while 1~(n-1) of those vses are involved in any vs-ops(cond2)
        // must be rebuilt for the need of regenerating concat upstream filename
        checkCurrentNeedBuildGroupIds(tasksEntity, currOnlineGroups, buildingGroupIds, nxOnlineVses);
        checkNextOnlineNeedBuildGroupIds(tasksEntity, nxOnlineGroups, buildingGroupIds, nxOnlineVses);


        Set<Long> buildingPolicyIds = new HashSet<>(tasksEntity.getActivatePolicyOps().keySet());
        for (TrafficPolicy p : currOnlinePolicies.values()) {
            if (p.getPolicyVirtualServers().size() <= 1) continue;
            boolean cond1 = false;
            boolean cond2 = false;
            for (PolicyVirtualServer pvs : p.getPolicyVirtualServers()) {
                if (tasksEntity.getDeactivateVsOps().keySet().contains(pvs.getVirtualServer().getId()) ||
                        tasksEntity.getSoftDeactivateVsOps().keySet().contains(pvs.getVirtualServer().getId())) {
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
                if (tasksEntity.getActivateVsOps().keySet().contains(pvs.getVirtualServer().getId())) {
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

    private void checkCurrentNeedBuildGroupIds(TasksEntity tasksEntity, Map<Long, Group> currOnlineGroups, Set<Long> buildingGroupIds, Map<Long, VirtualServer> nxOnlineVses) {
        for (Group g : currOnlineGroups.values()) {
            if (g.getGroupVirtualServers().size() <= 1) continue;
            boolean cond1 = false;
            boolean cond2 = false;
            for (GroupVirtualServer gvs : g.getGroupVirtualServers()) {
                if (tasksEntity.getDeactivateVsOps().keySet().contains(gvs.getVirtualServer().getId()) ||
                        tasksEntity.getSoftDeactivateVsOps().keySet().contains(gvs.getVirtualServer().getId())) {
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

    private void checkNextOnlineNeedBuildGroupIds(TasksEntity tasksEntity, Map<Long, Group> nxOnlineGroups, Set<Long> buildingGroupIds, Map<Long, VirtualServer> nxOnlineVses) {
        for (Group g : nxOnlineGroups.values()) {
            if (g.getGroupVirtualServers().size() <= 1) continue;
            boolean cond1 = false;
            boolean cond2 = false;
            for (GroupVirtualServer gvs : g.getGroupVirtualServers()) {
                if (tasksEntity.getActivateVsOps().keySet().contains(gvs.getVirtualServer().getId())) {
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


    //Revise Model Start

    /**
     * Get SLB Data. And Revise Offline Version If Not Equals Activate Version.
     * Result: SLB Map. OnlineVersion, ActivatingVersion=OfflineVersion
     *
     * @param slbId
     * @param activateSlbTask
     * @return
     * @throws Exception
     */
    private ModelStatusMapping<Slb> mapSlbVersionAndRevise(Long slbId, Collection<OpsTask> activateSlbTask) throws Exception {
        ModelStatusMapping<Slb> slbMap = entityFactory.getSlbsByIds(new Long[]{slbId});
        Slb offlineSlb = slbMap.getOfflineMapping().get(slbId);
        for (OpsTask task : activateSlbTask) {
            if (task.getSlbId().equals(slbId)) {
                if (!offlineSlb.getVersion().equals(task.getVersion())) {
                    offlineSlb = slbRepository.getByKey(new IdVersion(task.getSlbId(), task.getVersion()));
                    slbMap.getOfflineMapping().put(slbId, offlineSlb);
                }
            }
        }
        return slbMap;
    }

    /***
     * Get VS Data. And Revise Offline Version If Not Equals Activate Version.
     * Result: Vs Map. OnlineVersion, ActivatingVersion=OfflineVersion
     * @param slbId
     * @param activateVsTasks
     * @return
     * @throws Exception
     */
    private ModelStatusMapping<VirtualServer> mapVsVersionAndRevise(Long slbId, Collection<OpsTask> activateVsTasks) throws Exception {
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

    /***
     * Get Group Data. And Revise Offline Version If Not Equals Activate Version.
     * Result: Group Map. OnlineVersion, ActivatingVersion=OfflineVersion
     * @param vsIds
     * @param activateGroupTask
     * @return
     * @throws Exception
     */
    private ModelStatusMapping<Group> mapGroupVersionAndRevise(Set<Long> vsIds, Collection<OpsTask> activateGroupTask) throws Exception {
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

    /**
     * Get Policy Data. And Revise Offline Version If Not Equals Activate Version.
     * Result: Policy Map. OnlineVersion, ActivatingVersion=OfflineVersion
     *
     * @param vsIds
     * @param activatePolicyTask
     * @return
     * @throws Exception
     */
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

    /**
     * Get Dr Data. And Revise Offline Version If Not Equals Activate Version.
     * Result: Dr Map. OnlineVersion, ActivatingVersion=OfflineVersion
     *
     * @param vsIds
     * @param activateDrTask
     * @return
     * @throws Exception
     */
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

    //Revise Model END


    //GET Next Online Model Versions Start
    private Map<Long, Dr> getNextOnlineDrs(ModelStatusMapping<Dr> drMap, TasksEntity entity) {
        Map<Long, Dr> nxOnlineDrs = new HashMap<>(drMap.getOnlineMapping());
        for (Long drId : entity.getActivateDrOps().keySet()) {
            Dr offlineVersion = drMap.getOfflineMapping().get(drId);
            nxOnlineDrs.put(drId, offlineVersion);
        }
        return nxOnlineDrs;
    }

    private Map<Long, Group> getNextOnlineGroups(ModelStatusMapping<Group> groupMap, TasksEntity entity) {
        Map<Long, Group> nxOnlineGroups = new HashMap<>(groupMap.getOnlineMapping());
        for (Long gid : entity.getActivateGroupOps().keySet()) {
            Group offlineVersion = groupMap.getOfflineMapping().get(gid);
            nxOnlineGroups.put(gid, offlineVersion);
        }
        return nxOnlineGroups;
    }

    private Map<Long, VirtualServer> getNextOnlineVses(ModelStatusMapping<VirtualServer> vsMap, Long slbId, TasksEntity entity) throws Exception {
        Map<Long, VirtualServer> nxOnlineVses = vsMap.getOnlineMapping();
        for (Long vsId : entity.getActivateVsOps().keySet()) {
            nxOnlineVses.put(vsId, vsMap.getOfflineMapping().get(vsId));
        }
        if (!validateVsModel(slbId, nxOnlineVses, vsMap.getOnlineMapping(), entity, 3)) {
            throw new ValidationException("[!!!]Validate Vs Model Failed.");
        }
        return nxOnlineVses;
    }

    private Slb getNextOnlineSlb(Long slbId, ModelStatusMapping<Slb> slbMap, Slb onlineSlb, TasksEntity entity) throws Exception {
        Slb nxOnlineSlb = slbMap.getOfflineMapping().get(slbId);
        if (nxOnlineSlb == null) {
            throw new ValidationException("Not found activated slb version.SlbId:" + slbId);
        }
        if (entity.getActivateSlbOps().size() > 0) {
            if (!validateSlbModel(nxOnlineSlb, entity)) {
                nxOnlineSlb = onlineSlb;
            }
            return nxOnlineSlb;
        } else {
            return onlineSlb;
        }

    }

    /**
     * Validate Vs Model for next online version. Validate In Function getNextOnlineVses.
     *
     * @param slbId
     * @param nxOnlineVs
     * @param onlineMap
     * @param entity
     * @param retry
     * @return
     * @throws Exception
     */
    private boolean validateVsModel(Long slbId, Map<Long, VirtualServer> nxOnlineVs, Map<Long, VirtualServer> onlineMap, TasksEntity entity, int retry) throws Exception {
        if (retry <= 0) return false;
        ValidationContext context = new ValidationContext();
        validationFacade.validateVsesOnSlb(slbId, nxOnlineVs.values(), context);
        if (context.getErrorVses().size() > 0) {
            for (Long vsId : context.getErrorVses()) {
                if (entity.getActivateVsOps().containsKey(vsId)) {
                    if (entity.getAggSyncVsActivateTasks().containsKey(entity.getActivateVsOps().get(vsId).getId())) {
                        throw new ValidationException("AggSyncVsActivateTasks Failed.VsId:" + vsId + ";cause:" + context.getVsErrorReason(vsId));
                    }
                    entity.setTaskFail(entity.getActivateVsOps().get(vsId), "Invalidate version for online. VsId:" + vsId + ";cause:" + context.getVsErrorReason(vsId));
                    entity.getActivateVsOps().remove(vsId);
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
            return validateVsModel(slbId, nxOnlineVs, onlineMap, entity, --retry);
        } else {
            return true;
        }
    }

    /**
     * Validate Slb Model For Next Online Version. Validate In Function getNextOnlineSlb.
     *
     * @param nxOnlineSlb
     * @param entity
     * @return
     * @throws Exception
     */
    private boolean validateSlbModel(Slb nxOnlineSlb, TasksEntity entity) throws Exception {
        Set<Long> slbIds = slbCriteriaQuery.queryAll();
        ModelStatusMapping<Slb> slbMap = entityFactory.getSlbsByIds(slbIds.toArray(new Long[slbIds.size()]));
        Map<Long, Slb> map = slbMap.getOnlineMapping();
        map.put(nxOnlineSlb.getId(), nxOnlineSlb);
        ValidationContext context = new ValidationContext();
        validationFacade.validateSlbNodes(map.values(), context);
        if (context.getErrors().size() > 0) {
            entity.setTaskFail(entity.getActivateSlbOps().get(nxOnlineSlb.getId()), "Invalidate version for online. SlbId:" + nxOnlineSlb.getId() + ";cause:" + context.getErrors().toString());
            entity.getActivateSlbOps().remove(nxOnlineSlb.getId());
            logger.error("Invalidate version for online. SlbId:" + nxOnlineSlb.getId() + ";cause:" + context.getErrors().toString());
            return false;
        } else {
            return true;
        }
    }
    //GET Next Online Model Versions END


    private Map<Long, TrafficPolicy> getNextOnlinePolicies(ModelStatusMapping<TrafficPolicy> tpMap,
                                                           Slb nxOnlineSlb, TasksEntity entity) {
        Map<Long, TrafficPolicy> nxOnlineTps = new HashMap<>(tpMap.getOnlineMapping());
        for (Long pid : entity.getActivatePolicyOps().keySet()) {
            TrafficPolicy offlineVersion = tpMap.getOfflineMapping().get(pid);
            nxOnlineTps.put(pid, offlineVersion);
        }
        if (nxOnlineTps.size() > 0) {
            nxOnlineSlb.addRule(new Rule().setPhaseId(RulePhase.HTTP_INIT_BY_LUA.getId()).setName("init_randomseed"));
        }
        return nxOnlineTps;

    }

    private List<OpsTask> fetchTask(Long slbId) {
        try {
            return taskService.getPendingTasks(slbId);
        } catch (Exception e) {
            logger.warn("Task Executor get pending tasks failed! ", e);
            return null;
        }
    }

    private List<Long> getResourcesSlbIds(List<OpsTask> tasks) {
        Set<Long> tmp = new HashSet<>();
        for (OpsTask task : tasks) {
            if (task.getResources() != null) {
                tmp.add(Long.parseLong(task.getResources()));
            }
            tmp.add(task.getTargetSlbId());
            logger.info("[[taskId=" + task.getId() + "]]" + "Tasks are executing, TaskID [" + task.getId() + "]");
        }
        List<Long> resources = new ArrayList<>(tmp);
        Collections.sort(resources);
        return resources;
    }

    private void taskExecutorLog(List<OpsTask> tasks, Long slbId, long cost) {
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

}
