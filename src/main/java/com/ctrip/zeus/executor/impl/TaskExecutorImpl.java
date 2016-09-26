package com.ctrip.zeus.executor.impl;

import com.ctrip.zeus.commit.entity.Commit;
import com.ctrip.zeus.exceptions.NginxProcessingException;
import com.ctrip.zeus.executor.TaskExecutor;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.nginx.entity.NginxResponse;
import com.ctrip.zeus.service.build.BuildService;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.commit.CommitService;
import com.ctrip.zeus.service.commit.util.CommitType;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.nginx.NginxService;
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
    StatusService statusService;
    @Resource
    NginxService nginxService;
    @Resource
    private ConfVersionService confVersionService;
    @Resource
    private CommitService commitService;
    @Resource
    private ConfigHandler configHandler;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private static DynamicBooleanProperty writeEnable = DynamicPropertyFactory.getInstance().getBooleanProperty("write.enable", true);//"http://slberrorpages.ctripcorp.com/slberrorpages/500.htm");
    private static DynamicBooleanProperty healthyOpsActivate = DynamicPropertyFactory.getInstance().getBooleanProperty("healthy.operation.active", false);

    private HashMap<String, OpsTask> serverOps = new HashMap<>();
    private HashMap<Long, OpsTask> activateGroupOps = new HashMap<>();
    private HashMap<Long, OpsTask> deactivateGroupOps = new HashMap<>();
    private HashMap<Long, OpsTask> softDeactivateGroupOps = new HashMap<>();
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
                    executeJob(slbId);
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

    private void executeJob(Long slbId) throws Exception {
        sortTaskData(slbId);

        Slb onlineSlb = null;
        Long buildVersion = null;
        boolean needRollbackConf = false;

        try {
            //1. full access data from database and revise offline version by tasks
            ModelStatusMapping<Slb> slbMap;
            ModelStatusMapping<VirtualServer> vsMap;
            ModelStatusMapping<Group> groupMap;

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


            //2. merge data and get next online entities
            Map<Long, Group> nxOnlineGroups;
            Map<Long, VirtualServer> nxOnlineVses;
            Slb nxOnlineSlb;

            //2.1 group
            nxOnlineGroups = groupMap.getOnlineMapping();
            //save vs migrating under the same slb
            //TODO to give a better name
            Set<Long> nxRemovedGroupVses = new HashSet<>();
            for (Long gid : activateGroupOps.keySet()) {
                Group offlineVersion = groupMap.getOfflineMapping().get(gid);
                nxOnlineGroups.put(gid, offlineVersion);

                if (nxOnlineGroups.get(gid) != null) {
                    for (GroupVirtualServer gvs : nxOnlineGroups.get(gid).getGroupVirtualServers()) {
                        nxRemovedGroupVses.add(gvs.getVirtualServer().getId());
                    }
                    for (GroupVirtualServer gvs : offlineVersion.getGroupVirtualServers()) {
                        nxRemovedGroupVses.remove(gvs.getVirtualServer().getId());
                    }
                }
            }
            //2.2 vs
            nxOnlineVses = vsMap.getOnlineMapping();
            for (Long vsId : activateVsOps.keySet()) {
                nxOnlineVses.put(vsId, vsMap.getOfflineMapping().get(vsId));
            }
            //2.3 slb
            nxOnlineSlb = onlineSlb;
            if (activateSlbOps.size() > 0) {
                nxOnlineSlb = slbMap.getOfflineMapping().get(slbId);
            }

            //3. find out vses which need build.
            //3.1 deactivate vs pre check
            if (deactivateVsOps.size() > 0) {
                deactivateVsPreCheck(vsMap.getOnlineMapping().keySet(), nxOnlineGroups);
            }
            //3.2 find out vses which need build.
            //a. Vs->Groups Pairs. Groups is sorted by priority
            //b. needBuild Group->Vs pairs
            //c. needBuild VsIdes
            Map<Long, List<Group>> vsGroups = new HashMap<>();
            Set<Long> needBuildVses = new HashSet<>();
            Map<Long, Set<Long>> needBuildGroupVs = new HashMap<>();
            final Map<String, Integer> vsGroupPriority = new HashMap<>();

            //TODO if activate slb, skip all other actions
            if (activateSlbOps.size() > 0 && activateSlbOps.get(slbId) != null) {
                needBuildVses.addAll(nxOnlineVses.keySet());
            } else {
                needBuildVses.addAll(activateVsOps.keySet());
            }

            if (softDeactivateGroupOps.size() > 0) {
                for (OpsTask task : softDeactivateGroupOps.values()) {
                    if (task.getSlbVirtualServerId() != null && nxOnlineVses.containsKey(task.getSlbVirtualServerId())) {
                        needBuildVses.add(task.getSlbVirtualServerId());
                    } else {
                        setTaskFail(task, "Not found online vs for soft deactivate group ops. vs=" + task.getSlbVirtualServerId());
                    }
                }
            }
            for (Long id : nxRemovedGroupVses) {
                if (nxOnlineVses.containsKey(id)) {
                    needBuildVses.add(id);
                }
            }

            for (Map.Entry<Long, Group> e : nxOnlineGroups.entrySet()) {
                traverseGroupContent(e.getKey(), e.getValue(), slbId,
                        nxOnlineVses,
                        needBuildGroupVs, needBuildVses, vsGroupPriority, vsGroups);
            }

            //3.* in case of no need to update the config files.
            //only have operation for inactivated groups.
            if (needBuildVses.size() == 0 && activateSlbOps.size() == 0 && deactivateVsOps.size() == 0) {
                performTasks(slbId, needBuildGroupVs, groupMap.getOfflineMapping());
                setTaskResult(slbId, true, null);
                return;
            }

            //3.3 sort Groups by priority
            for (Long vsId : vsGroups.keySet()) {
                final Long vs = vsId;
                List<Group> list = vsGroups.get(vsId);
                Collections.sort(list, new Comparator<Group>() {
                    public int compare(Group group0, Group group1) {
                        if (vsGroupPriority.get("VS" + vs + "_" + group1.getId()).equals(vsGroupPriority.get("VS" + vs + "_" + group0.getId()))) {
                            return (int) (group1.getId() - group0.getId());
                        }
                        return vsGroupPriority.get("VS" + vs + "_" + group1.getId()) - vsGroupPriority.get("VS" + vs + "_" + group0.getId());
                    }
                });
            }

            //4. build config
            //4.1 get allDownServers
            Set<String> allDownServers = getAllDownServer();
            //4.2 allUpGroupServers
            Set<String> allUpGroupServers = getAllUpGroupServers(needBuildVses, nxOnlineGroups, slbId);
            //4.3 build config
            buildVersion = buildService.build(nxOnlineSlb, nxOnlineVses, needBuildVses, deactivateVsOps.keySet(),
                    vsGroups, allDownServers, allUpGroupServers);

            //5. push config
            //5.1 need reload?
            boolean needReload = false;
            if (activateSlbOps.size() > 0 || activateGroupOps.size() > 0 || deactivateGroupOps.size() > 0
                    || softDeactivateGroupOps.size() > 0
                    || activateVsOps.size() > 0 || deactivateVsOps.size() > 0 || softDeactivateVsOps.size() > 0) {
                needReload = true;
            }

            //5.2 push config to all slb servers. reload if needed.
            //5.2.1 remove deactivate vs ids from need build vses
            needBuildVses.removeAll(deactivateVsOps.keySet());
            needBuildVses.removeAll(softDeactivateVsOps.keySet());
            if (writeEnable.get()) {
                //5.2.2 update slb current version
                confVersionService.updateSlbCurrentVersion(slbId, buildVersion);
                //5.2.3 add commit
                addCommit(slbId, needReload, buildVersion, needBuildVses, needBuildGroupVs.keySet());
                //5.2.4 fire update job
                needRollbackConf = true;
                NginxResponse response = nginxService.updateConf(nxOnlineSlb.getSlbServers());
                if (!response.getSucceed()) {
                    throw new Exception("Update config Fail.Fail Response:" + String.format(NginxResponse.JSON, response));
                }
            }
            performTasks(slbId, needBuildGroupVs, groupMap.getOfflineMapping());
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

    private void traverseGroupContent(Long groupId, Group group, Long slbId,
                                      Map<Long, VirtualServer> nxOnlineVses,
                                      Map<Long, Set<Long>> needBuildGroupVs,
                                      Set<Long> needBuildVses, Map<String, Integer> vsGroupPriority, Map<Long, List<Group>> vsGroups) throws NginxProcessingException {
        if (group == null) {
            String errMsg = "Unexpected online group with null value. groupId=" + groupId + ".";
            logger.error(errMsg);
            throw new NginxProcessingException(errMsg);
        }

        boolean groupRequireBuilding = false;
        if (activateGroupOps.containsKey(groupId) || deactivateGroupOps.containsKey(groupId)
                || pullMemberOps.containsKey(groupId) || memberOps.containsKey(groupId) || healthyOps.containsKey(groupId)) {
            groupRequireBuilding = true;
        }
        if (serverOps.size() > 0 && !groupRequireBuilding) {
            for (GroupServer gs : group.getGroupServers()) {
                if (serverOps.containsKey(gs.getIp())) {
                    groupRequireBuilding = true;
                    break;
                }
            }
        }

        boolean containsNxOnlineVses = false;
        Set<Long> buildingGroupRelatedVsIds = null;
        for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
            if (nxOnlineVses.containsKey(gvs.getVirtualServer().getId())) {
                containsNxOnlineVses = true;
                Long vsId = gvs.getVirtualServer().getId();

                if (groupRequireBuilding) {
                    needBuildVses.add(vsId);
                    if (buildingGroupRelatedVsIds == null) {
                        buildingGroupRelatedVsIds = new HashSet<>();
                    }
                    buildingGroupRelatedVsIds.add(vsId);
                }

                List<Group> vsRelatedGroups = vsGroups.get(vsId);
                if (vsRelatedGroups == null) {
                    vsRelatedGroups = new ArrayList<>();
                    vsGroups.put(vsId, vsRelatedGroups);
                }

                if (deactivateGroupOps.containsKey(groupId)
                        || (softDeactivateGroupOps.containsKey(groupId) && softDeactivateGroupOps.get(groupId).getSlbVirtualServerId().equals(vsId))) {
                    continue;
                }
                vsRelatedGroups.add(group);
                vsGroupPriority.put("VS" + gvs.getVirtualServer().getId() + "_" + groupId, gvs.getPriority());
            }
        }

        needBuildGroupVs.put(groupId, buildingGroupRelatedVsIds);

        //TODO Is it possible?
        if (!containsNxOnlineVses) {
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
    }

    private void taskExecutorLog(Long slbId, long cost) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("SlbId: " + slbId).append("\n");
        sb.append("TaskCount: " + tasks.size()).append("\n");
        sb.append("Cost: " + cost).append("\n");
        sb.append("Tasks:").append("[\n");
        for (OpsTask task : tasks) {
            sb.append("{");
            sb.append("taskId: ").append(task.getId());
            sb.append("status: ").append(task.getStatus());
            sb.append("failcause: ").append(task.getFailCause());
            sb.append("}\n");
        }
        sb.append("]");
        logger.info(sb.toString());
    }

    private void addCommit(Long slbId, boolean needReload, Long buildVersion, Set<Long> needBuildVses, Set<Long> needBuildGroups) throws Exception {
        Commit commit = new Commit();
        commit.setSlbId(slbId)
                .setType(needReload ? CommitType.COMMIT_TYPE_RELOAD : CommitType.COMMIT_TYPE_DYUPS)
                .setVersion(buildVersion);
        commit.getVsIds().addAll(needBuildVses);
        commit.getGroupIds().addAll(needBuildGroups);
        commit.getCleanvsIds().addAll(deactivateVsOps.keySet());
        for (OpsTask t : tasks) {
            commit.addTaskId(t.getId());
        }
        commitService.add(commit);
    }

    private void deactivateVsPreCheck(Set<Long> currOnlineVses, Map<Long, Group> nxOnlineGroups) throws Exception {
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
                    activatingGroupIds.add(gid);
                }
            }
        }
        for (Long groupId : activatingGroupIds) {
            setTaskFail(activateGroupOps.get(groupId), "[Vs deactivate Pre Check] Activating Group While Related Vs is deactivating!");
            activateGroupOps.remove(groupId);
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

    private void performTasks(Long slbId, Map<Long, Set<Long>> groupVs, Map<Long, Group> offlineGroups) throws Exception {
        try {
            for (OpsTask task : activateSlbOps.values()) {
                if (!task.getStatus().equals(TaskStatus.DOING)) {
                    continue;
                }
                IdVersion idVersion = new IdVersion(task.getSlbId(), task.getVersion());
                slbRepository.updateStatus(new IdVersion[]{idVersion});
            }
            List<IdVersion> vsIds = new ArrayList<>();
            for (OpsTask task : activateVsOps.values()) {
                if (!task.getStatus().equals(TaskStatus.DOING)) {
                    continue;
                }
                vsIds.add(new IdVersion(task.getSlbVirtualServerId(), task.getVersion()));
            }
            virtualServerRepository.updateStatus(vsIds.toArray(new IdVersion[]{}));

            List<IdVersion> deactivateVsIds = new ArrayList<>();
            for (OpsTask task : deactivateVsOps.values()) {
                if (!task.getStatus().equals(TaskStatus.DOING)) {
                    continue;
                }
                deactivateVsIds.add(new IdVersion(task.getSlbVirtualServerId(), 0));
            }
            virtualServerRepository.updateStatus(deactivateVsIds.toArray(new IdVersion[]{}));

            List<IdVersion> activateGroups = new ArrayList<>();
            for (OpsTask task : activateGroupOps.values()) {
                if (!task.getStatus().equals(TaskStatus.DOING)) {
                    continue;
                }
                activateGroups.add(new IdVersion(task.getGroupId(), task.getVersion()));
            }
            groupRepository.updateStatus(activateGroups.toArray(new IdVersion[]{}));

            List<IdVersion> deactivateGroups = new ArrayList<>();
            for (OpsTask task : deactivateGroupOps.values()) {
                if (!task.getStatus().equals(TaskStatus.DOING)) {
                    continue;
                }
                deactivateGroups.add(new IdVersion(task.getGroupId(), 0));
            }
            groupRepository.updateStatus(deactivateGroups.toArray(new IdVersion[]{}));

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
                    if (!task.getStatus().equals(TaskStatus.DOING)) {
                        continue;
                    }
                    String[] ips = task.getIpList().split(";");
                    List<String> ipList = Arrays.asList(ips);
                    Set<Long> ids = groupVs.get(task.getGroupId());
                    Set<Long> tmpVsIds = new HashSet<>();
                    if (ids != null) {
                        tmpVsIds.addAll(ids);
                    }
                    Group group = offlineGroups.get(task.getGroupId());
                    if (group == null || group.getGroupVirtualServers().size() == 0) {
                        setTaskFail(task, "Not Found Group Id:" + task.getGroupId());
                        continue;
                    }
                    for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
                        tmpVsIds.add(gvs.getVirtualServer().getId());
                    }
                    for (Long vsId : tmpVsIds) {
                        UpdateStatusItem item = new UpdateStatusItem();
                        item.setGroupId(task.getGroupId()).setVsId(vsId).setSlbId(slbId)
                                .setOffset(StatusOffset.MEMBER_OPS).setUp(task.getUp());
                        item.getIpses().addAll(ipList);
                        memberUpdates.add(item);
                    }
                }
            }
            statusService.updateStatus(memberUpdates);

            List<UpdateStatusItem> pullUpdates = new ArrayList<>();
            for (List<OpsTask> taskList : pullMemberOps.values()) {
                for (OpsTask task : taskList) {
                    if (!task.getStatus().equals(TaskStatus.DOING)) {
                        continue;
                    }
                    String[] ips = task.getIpList().split(";");
                    List<String> ipList = Arrays.asList(ips);
                    Set<Long> ids = groupVs.get(task.getGroupId());
                    Set<Long> tmpVsIds = new HashSet<>();
                    if (ids != null) {
                        tmpVsIds.addAll(ids);
                    }
                    Group group = offlineGroups.get(task.getGroupId());
                    if (group == null || group.getGroupVirtualServers().size() == 0) {
                        setTaskFail(task, "Not Found Group Id:" + task.getGroupId());
                        continue;
                    }
                    for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
                        tmpVsIds.add(gvs.getVirtualServer().getId());
                    }
                    for (Long vsId : tmpVsIds) {
                        UpdateStatusItem item = new UpdateStatusItem();
                        item.setGroupId(task.getGroupId()).setVsId(vsId).setSlbId(slbId)
                                .setOffset(StatusOffset.PULL_OPS).setUp(task.getUp());
                        item.getIpses().addAll(ipList);
                        pullUpdates.add(item);
                    }
                }
            }
            statusService.updateStatus(pullUpdates);

            List<UpdateStatusItem> healthyStatus = new ArrayList<>();
            for (List<OpsTask> taskList : healthyOps.values()) {
                for (OpsTask task : taskList) {
                    if (!task.getStatus().equals(TaskStatus.DOING)) {
                        continue;
                    }
                    String[] ips = task.getIpList().split(";");
                    List<String> ipList = Arrays.asList(ips);
                    Set<Long> ids = groupVs.get(task.getGroupId());
                    Set<Long> tmpVsIds = new HashSet<>();
                    if (ids != null) {
                        tmpVsIds.addAll(ids);
                    }
                    Group group = offlineGroups.get(task.getGroupId());
                    if (group == null || group.getGroupVirtualServers().size() == 0) {
                        setTaskFail(task, "Not Found Group Id:" + task.getGroupId());
                        continue;
                    }
                    for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
                        tmpVsIds.add(gvs.getVirtualServer().getId());
                    }
                    for (Long vsId : tmpVsIds) {
                        UpdateStatusItem item = new UpdateStatusItem();
                        item.setGroupId(task.getGroupId()).setVsId(vsId).setSlbId(slbId)
                                .setOffset(StatusOffset.HEALTHY).setUp(task.getUp());
                        item.getIpses().addAll(ipList);
                        healthyStatus.add(item);
                    }
                }
            }
            statusService.updateStatus(healthyStatus);
        } catch (Exception e) {
            throw new Exception("Perform Tasks Fail! TargetSlbId:" + tasks.get(0).getTargetSlbId(), e);
        }

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

    private Set<String> getAllUpGroupServers(Set<Long> vsIds, Map<Long, Group> groups, Long slbId) throws Exception {
        Map<String, List<Boolean>> memberStatus = statusService.fetchGroupServerStatus(groups.keySet().toArray(new Long[]{}));
        Set<Long> tmpid = memberOps.keySet();
        for (Long gid : tmpid) {
            Group groupTmp = groups.get(gid);
            if (groupTmp == null) {
                /*group not activated. just skip it.*/
                continue;
            }
            List<OpsTask> taskList = memberOps.get(gid);
            for (OpsTask opsTask : taskList) {
                String ipList = opsTask.getIpList();
                String[] ips = ipList.split(";");
                for (GroupVirtualServer gvs : groupTmp.getGroupVirtualServers()) {
                    if (!vsIds.contains(gvs.getVirtualServer().getId())) {
                        continue;
                    }
                    for (String ip : ips) {
                        memberStatus.get(gid + "_" + ip).set(StatusOffset.MEMBER_OPS, opsTask.getUp());
                    }
                }
            }
        }
        tmpid = pullMemberOps.keySet();
        for (Long gid : tmpid) {
            Group groupTmp = groups.get(gid);
            if (groupTmp == null) {
                /*group not activated. just skip it.*/
                continue;
            }
            List<OpsTask> taskList = pullMemberOps.get(gid);
            for (OpsTask opsTask : taskList) {
                String ipList = opsTask.getIpList();
                String[] ips = ipList.split(";");
                for (GroupVirtualServer gvs : groupTmp.getGroupVirtualServers()) {
                    if (!vsIds.contains(gvs.getVirtualServer().getId())) {
                        continue;
                    }
                    for (String ip : ips) {
                        memberStatus.get(gid + "_" + ip).set(StatusOffset.PULL_OPS, opsTask.getUp());
                    }
                }
            }
        }

        tmpid = healthyOps.keySet();
        for (Long gid : tmpid) {
            Group groupTmp = groups.get(gid);
            if (groupTmp == null) {
                /*group not activated. just skip it.*/
                continue;
            }
            List<OpsTask> taskList = healthyOps.get(gid);
            for (OpsTask opsTask : taskList) {
                String ipList = opsTask.getIpList();
                String[] ips = ipList.split(";");
                for (GroupVirtualServer gvs : groupTmp.getGroupVirtualServers()) {
                    if (!vsIds.contains(gvs.getVirtualServer().getId())) {
                        continue;
                    }
                    for (String ip : ips) {
                        memberStatus.get(gid + "_" + ip).set(StatusOffset.HEALTHY, opsTask.getUp());
                    }
                }
            }
        }

        boolean healthyActivateFlag = healthyOpsActivate.get() && configHandler.getEnable("healthy.operation.active", slbId, null, null, false);
        Set<String> result = new HashSet<>();
        for (String key : memberStatus.keySet()) {
            List<Boolean> status = memberStatus.get(key);
            if (healthyActivateFlag) {
                if (status.get(StatusOffset.PULL_OPS) && status.get(StatusOffset.MEMBER_OPS) && status.get(StatusOffset.HEALTHY)) {
                    result.add(key);
                }
            } else {
                if (status.get(StatusOffset.PULL_OPS) && status.get(StatusOffset.MEMBER_OPS)) {
                    result.add(key);
                }
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

    private void sortTaskData(Long slbId) {
        activateGroupOps.clear();
        activateSlbOps.clear();
        serverOps.clear();
        memberOps.clear();
        deactivateGroupOps.clear();
        pullMemberOps.clear();
        healthyOps.clear();
        activateVsOps.clear();
        deactivateVsOps.clear();
        softDeactivateVsOps.clear();
        softDeactivateGroupOps.clear();
        for (OpsTask task : tasks) {
            task.setStatus(TaskStatus.DOING);
            //Activate group
            if (task.getOpsType().equals(TaskOpsType.ACTIVATE_GROUP)) {
                activateGroupOps.put(task.getGroupId(), task);
            }
            //activate slb
            if (task.getOpsType().equals(TaskOpsType.ACTIVATE_SLB)) {
                activateSlbOps.put(task.getSlbId(), task);
            }
            // server ops
            if (task.getOpsType().equals(TaskOpsType.SERVER_OPS)) {
                serverOps.put(task.getIpList(), task);
            }
            //member ops
            if (task.getOpsType().equals(TaskOpsType.MEMBER_OPS)) {
                List<OpsTask> taskList = memberOps.get(task.getGroupId());
                if (taskList == null) {
                    taskList = new ArrayList<>();
                    memberOps.put(task.getGroupId(), taskList);
                }
                taskList.add(task);
            }
            //tars member ops
            if (task.getOpsType().equals(TaskOpsType.PULL_MEMBER_OPS)) {
                List<OpsTask> taskList = pullMemberOps.get(task.getGroupId());
                if (taskList == null) {
                    taskList = new ArrayList<>();
                    pullMemberOps.put(task.getGroupId(), taskList);
                }
                taskList.add(task);
            }
            //healthy ops
            if (task.getOpsType().equals(TaskOpsType.HEALTHY_OPS)) {
                List<OpsTask> taskList = healthyOps.get(task.getGroupId());
                if (taskList == null) {
                    taskList = new ArrayList<>();
                    healthyOps.put(task.getGroupId(), taskList);
                }
                taskList.add(task);
            }
            //deactivate
            if (task.getOpsType().equals(TaskOpsType.DEACTIVATE_GROUP)) {
                deactivateGroupOps.put(task.getGroupId(), task);
            }
            //soft deactivate
            if (task.getOpsType().equals(TaskOpsType.SOFT_DEACTIVATE_GROUP)) {
                softDeactivateGroupOps.put(task.getGroupId(), task);
            }
            //deactivate vs
            if (task.getOpsType().equals(TaskOpsType.DEACTIVATE_VS)) {
                deactivateVsOps.put(task.getSlbVirtualServerId(), task);
            }
            if (task.getOpsType().equals(TaskOpsType.SOFT_DEACTIVATE_VS)) {
                softDeactivateVsOps.put(task.getSlbVirtualServerId(), task);
            }
            //activate vs
            if (task.getOpsType().equals(TaskOpsType.ACTIVATE_VS)) {
                activateVsOps.put(task.getSlbVirtualServerId(), task);
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
