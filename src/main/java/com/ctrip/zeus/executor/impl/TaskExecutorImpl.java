package com.ctrip.zeus.executor.impl;

import com.ctrip.zeus.commit.entity.Commit;
import com.ctrip.zeus.exceptions.NginxProcessingException;
import com.ctrip.zeus.executor.TaskExecutor;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.nginx.entity.NginxResponse;
import com.ctrip.zeus.service.build.BuildInfoService;
import com.ctrip.zeus.service.build.BuildService;
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

    Logger logger = LoggerFactory.getLogger(this.getClass());
    private static DynamicBooleanProperty writeEnable = DynamicPropertyFactory.getInstance().getBooleanProperty("write.enable", true);//"http://slberrorpages.ctripcorp.com/slberrorpages/500.htm");


    private HashMap<String, OpsTask> serverOps = new HashMap<>();
    private HashMap<Long, OpsTask> activateGroupOps = new HashMap<>();
    private HashMap<Long, OpsTask> softDeactivateGroupOps = new HashMap<>();
    private HashMap<Long, OpsTask> activateVsOps = new HashMap<>();
    private HashMap<Long, OpsTask> deactivateGroupOps = new HashMap<>();
    private HashMap<Long, OpsTask> deactivateVsOps = new HashMap<>();
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

    public void executeJob(Long slbId) throws Exception {
        sortTaskData(slbId);

        Map<Long, Group> onlineGroups;
        Map<Long, Group> offlineGroups;
        Map<Long, VirtualServer> offlineVses;
        Map<Long, VirtualServer> onlineVses;
        Slb onlineSlb = null;
        Slb offlineSlb = null;
        Long buildVersion = null;
        boolean needRollbackConf = false;

        try {
            //1. get needed data from database
            //1.1 slb data
            ModelStatusMapping<Slb> slbMap = entityFactory.getSlbsByIds(new Long[]{slbId});
            onlineSlb = slbMap.getOnlineMapping().get(slbId);
            offlineSlb = slbMap.getOfflineMapping().get(slbId);

            //1.2 virtual server data
            ModelStatusMapping<VirtualServer> vsMap = entityFactory.getVsesBySlbIds(slbId);
            offlineVses = vsMap.getOfflineMapping();
            onlineVses = vsMap.getOnlineMapping();

            //1.3 group data
            Set<Long> vsIds = new HashSet<>(onlineVses.keySet());
            vsIds.addAll(offlineVses.keySet());
            ModelStatusMapping<Group> groupsMap = entityFactory.getGroupsByVsIds(vsIds.toArray(new Long[]{}));
            onlineGroups = groupsMap.getOnlineMapping();
            offlineGroups = groupsMap.getOfflineMapping();

            //1.4 offline data check
            List<IdVersion> toFetch = new ArrayList<>();
            for (OpsTask task : activateGroupOps.values()) {
                if (!offlineGroups.get(task.getGroupId()).getVersion().equals(task.getVersion())) {
                    toFetch.add(new IdVersion(task.getId(), task.getVersion()));
                }
            }
            List<Group> groups = groupRepository.list(toFetch.toArray(new IdVersion[]{}));
            for (Group group : groups) {
                offlineGroups.put(group.getId(), group);
            }

            toFetch = new ArrayList<>();
            for (OpsTask task : activateVsOps.values()) {
                if (!offlineVses.get(task.getSlbVirtualServerId()).getVersion().equals(task.getVersion())) {
                    toFetch.add(new IdVersion(task.getId(), task.getVersion()));
                }
            }
            List<VirtualServer> vses = virtualServerRepository.listAll(toFetch.toArray(new IdVersion[]{}));
            for (VirtualServer vs : vses) {
                offlineVses.put(vs.getId(), vs);
            }

            if (activateSlbOps.size() > 0) {
                OpsTask task = activateSlbOps.get(slbId);
                if (!offlineSlb.getVersion().equals(task.getVersion())) {
                    offlineSlb = slbRepository.getByKey(new IdVersion(task.getId(), task.getVersion()));
                }
            }

            //2. merge data.
            //2.1 merge on/offline groups
            Set<Long> softVses = new HashSet<>();
            for (Long gid : activateGroupOps.keySet()) {
                if (onlineGroups.get(gid) != null) {
                    List<Long> onVs = new ArrayList<>();
                    List<Long> offVs = new ArrayList<>();
                    for (GroupVirtualServer gvs : onlineGroups.get(gid).getGroupVirtualServers()) {
                        onVs.add(gvs.getVirtualServer().getId());
                    }
                    for (GroupVirtualServer gvs : offlineGroups.get(gid).getGroupVirtualServers()) {
                        offVs.add(gvs.getVirtualServer().getId());
                    }
                    onVs.removeAll(offVs);
                    softVses.addAll(onVs);
                }
                onlineGroups.put(gid, offlineGroups.get(gid));
            }
            //2.2 merge on/offline vses
            for (Long sid : activateVsOps.keySet()) {
                onlineVses.put(sid, offlineVses.get(sid));
            }
            //2.3 merge on/offline slb
            if (activateSlbOps.size() > 0) {
                onlineSlb = offlineSlb;
            }

            //3. find out vses which need build.
            //3.1 deactivate vs pre check
            if (deactivateVsOps.size() > 0) {
                deactivateVsPreCheck(onlineVses.keySet(), onlineGroups);
            }
            //3.2 find out vses which need build.
            //a. Vs->Groups Pairs. Groups is sorted by priority
            //b. needBuild Group->Vs pairs
            //c. needBuild VsIdes
            Map<Long, List<Group>> vsGroups = new HashMap<>();
            Set<Long> needBuildVses = new HashSet<>();
            Map<Long, Set<Long>> needBuildGroupVs = new HashMap<>();
            final Map<String, Integer> vsGroupPriority = new HashMap<>();
            needBuildVses.addAll(activateVsOps.keySet());
            if (activateSlbOps.size() > 0 && activateSlbOps.get(slbId) != null) {
                needBuildVses.addAll(onlineVses.keySet());
            }
            if (softDeactivateGroupOps.size() > 0) {
                for (OpsTask task : softDeactivateGroupOps.values()) {
                    if (task.getSlbVirtualServerId() != null && onlineVses.containsKey(task.getSlbVirtualServerId())) {
                        needBuildVses.add(task.getSlbVirtualServerId());
                    } else {
                        setTaskFail(task, "Not found online vs for soft deactivate group ops. vs=" + task.getSlbVirtualServerId());
                    }
                }
            }
            for (Long id : softVses) {
                if (onlineVses.containsKey(id)) {
                    needBuildVses.add(id);
                }
            }

            boolean need = false;
            boolean hasRelatedVs = false;
            for (Long gid : onlineGroups.keySet()) {
                need = false;
                if (activateGroupOps.containsKey(gid) || pullMemberOps.containsKey(gid) || memberOps.containsKey(gid)
                        || healthyOps.containsKey(gid) || deactivateGroupOps.containsKey(gid)) {
                    need = true;
                }
                Group group = onlineGroups.get(gid);
                if (group == null) {
                    logger.error("Not Found Online Group Data, GroupId: " + gid);
                    throw new NginxProcessingException("Not Found Online Group Data, GroupId: " + gid);
                }
                if (serverOps.size() > 0 && !need) {
                    for (GroupServer gs : group.getGroupServers()) {
                        if (serverOps.containsKey(gs.getIp())) {
                            need = true;
                            break;
                        }
                    }
                }
                hasRelatedVs = false;
                for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
                    if (onlineVses.containsKey(gvs.getVirtualServer().getId())) {
                        if (need) {
                            needBuildVses.add(gvs.getVirtualServer().getId());
                            Set<Long> tmpVses = needBuildGroupVs.get(gid);
                            if (tmpVses == null) {
                                tmpVses = new HashSet<Long>();
                                needBuildGroupVs.put(gid, tmpVses);
                            }
                            tmpVses.add(gvs.getVirtualServer().getId());
                        }
                        List<Group> groupList = vsGroups.get(gvs.getVirtualServer().getId());
                        if (groupList == null) {
                            groupList = new ArrayList<>();
                            vsGroups.put(gvs.getVirtualServer().getId(), groupList);
                        }
                        hasRelatedVs = true;
                        if (deactivateGroupOps.containsKey(gid)) {
                            continue;
                        }
                        if (softDeactivateGroupOps.containsKey(group.getId())
                                && gvs.getVirtualServer().getId().equals(softDeactivateGroupOps.get(group.getId()).getSlbVirtualServerId())) {
                            continue;
                        }
                        groupList.add(group);
                        vsGroupPriority.put("VS" + gvs.getVirtualServer().getId() + "_" + gid, gvs.getPriority());
                    }
                }
                if (!hasRelatedVs) {
                    if (activateGroupOps.containsKey(gid)) {
                        setTaskFail(activateGroupOps.get(gid), "Not found online virtual server for Group[" + gid + "] in slb[" + slbId + "].");
                    } else if (deactivateGroupOps.containsKey(gid)) {
                        setTaskFail(deactivateGroupOps.get(gid), "Not found online virtual server for Group[" + gid + "] in slb[" + slbId + "].");
                    } else if (pullMemberOps.containsKey(gid)) {
                        for (OpsTask task : pullMemberOps.get(gid)) {
                            setTaskFail(task, "Not found online virtual server for Group[" + gid + "] in slb[" + slbId + "].");
                        }
                    } else if (memberOps.containsKey(gid)) {
                        for (OpsTask task : memberOps.get(gid)) {
                            setTaskFail(task, "Not found online virtual server for Group[" + gid + "] in slb[" + slbId + "].");
                        }
                    } else if (healthyOps.containsKey(gid)) {
                        for (OpsTask task : memberOps.get(gid)) {
                            setTaskFail(task, "Not found online virtual server for Group[" + gid + "] in slb[" + slbId + "].");
                        }
                    }
                }
            }
            //3.* in case of no need to update the config files.
            //only have operation for inactivated groups.
            if (needBuildVses.size() == 0 && activateSlbOps.size() == 0 && deactivateVsOps.size() == 0) {
                performTasks(slbId, needBuildGroupVs, offlineGroups);
                setTaskResult(slbId, true, null);
                return;
            }
            //3.3 sort Groups by priority
            for (Long vsId : vsGroups.keySet()) {
                final Long vs = vsId;
                List<Group> list = vsGroups.get(vsId);
                Collections.sort(list, new Comparator<Group>() {
                    public int compare(Group group0, Group group1) {
                        if (vsGroupPriority.get("VS" + vs + "_" + group1.getId()) == vsGroupPriority.get("VS" + vs + "_" + group0.getId())) {
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
            Set<String> allUpGroupServers = getAllUpGroupServers(needBuildVses, onlineGroups);
            //4.3 build config
            buildVersion = buildService.build(onlineSlb, onlineVses, needBuildVses, deactivateVsOps.keySet(),
                    vsGroups, allDownServers, allUpGroupServers);

            //5. push config
            //5.1 need reload?
            boolean needReload = false;
            if (activateSlbOps.size() > 0 || activateGroupOps.size() > 0 || deactivateGroupOps.size() > 0
                    || activateVsOps.size() > 0 || deactivateVsOps.size() > 0 || softDeactivateGroupOps.size() > 0) {
                needReload = true;
            }
            //5.2 push config to all slb servers. reload if needed.
            //5.2.1 remove deactivate vs ids from need build vses
            needBuildVses.removeAll(deactivateVsOps.keySet());
            if (writeEnable.get()) {
                //5.2.2 update slb current version
                confVersionService.updateSlbCurrentVersion(slbId, buildVersion);
                //5.2.3 add commit
                addCommit(slbId, needReload, buildVersion, needBuildVses, needBuildGroupVs.keySet());
                //5.2.4 fire update job
                needRollbackConf = true;
                NginxResponse response = nginxService.updateConf(onlineSlb.getSlbServers());
                if (!response.getSucceed()) {
                    throw new Exception("Update config Fail.Fail Response:" + String.format(NginxResponse.JSON, response));
                }
            }
            performTasks(slbId, needBuildGroupVs, offlineGroups);
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

    private void deactivateVsPreCheck(Set<Long> onlineVses, Map<Long, Group> onlineGroups) throws Exception {
        Set<Long> keySet = new HashSet<>(deactivateVsOps.keySet());
        for (Long id : keySet) {
            OpsTask task = deactivateVsOps.get(id);
            if (!onlineVses.contains(task.getSlbVirtualServerId())) {
                setTaskFail(task, "[Deactivate Vs] Vs is unactivated!");
                deactivateVsOps.remove(id);
            }
        }
        List<Long> groups = new ArrayList<>();
        for (Long gid : activateGroupOps.keySet()) {
            Group group = onlineGroups.get(gid);
            for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
                if (deactivateVsOps.containsKey(gvs.getVirtualServer().getId())) {
                    groups.add(gid);
                }
            }
        }
        for (Long groupId : groups) {
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

    private Set<String> getAllUpGroupServers(Set<Long> vsIds, Map<Long, Group> groups) throws Exception {
        Map<String, List<Boolean>> memberStatus = statusService.fetchGroupServerStatus(vsIds.toArray(new Long[]{}),
                groups.keySet().toArray(new Long[]{}));
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
