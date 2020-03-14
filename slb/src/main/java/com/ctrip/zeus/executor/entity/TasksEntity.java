package com.ctrip.zeus.executor.entity;

import com.ctrip.zeus.model.task.OpsTask;
import com.ctrip.zeus.service.task.constant.TaskOpsType;
import com.ctrip.zeus.service.task.constant.TaskStatus;
import com.ctrip.zeus.support.DefaultObjectJsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TasksEntity {
    private List<OpsTask> orgTasks;
    private HashMap<String, OpsTask> serverOps = new HashMap<>();
    private HashMap<Long, OpsTask> activateGroupOps = new HashMap<>();
    private HashMap<Long, OpsTask> activateCanaryGroupOps = new HashMap<>();
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

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public TasksEntity(List<OpsTask> tasks) {
        sortTask(tasks);
        this.orgTasks = tasks;
    }

    public void clear() {
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
    }

    public void setTaskFail(OpsTask task, String failcause) throws Exception {
        task.setStatus(TaskStatus.FAIL);
        task.setFailCause(failcause);
        logger.warn("[Task Fail] OpsTask: Type[" + task.getOpsType() + " TaskId:" + task.getId() + "],FailCause:" + failcause);
        if (this.getAggSubTasks().containsKey(task.getId())) {
            throw new Exception("Stop All Tasks.Because Of AGG Task Failed.TaskID:" + task.getId() + " Tasks:" + this.getAggSubTasks().get(task.getId()).toString());
        }
    }

    public List<OpsTask> getOrgTasks() {
        return orgTasks;
    }

    public TasksEntity setOrgTasks(List<OpsTask> orgTasks) {
        this.orgTasks = orgTasks;
        return this;
    }

    private void sortTask(List<OpsTask> tasks) {
        clear();
        List<OpsTask> tmp = new ArrayList<>();
        for (OpsTask task : tasks) {
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
        tasks.addAll(tmp);
        List<OpsTask> taskList;
        for (OpsTask task : tasks) {
            task.setStatus(TaskStatus.DOING);
            switch (task.getOpsType()) {
                case TaskOpsType.ACTIVATE_GROUP:
                    if (!activateGroupOps.containsKey(task.getGroupId())
                            || activateGroupOps.get(task.getGroupId()).getVersion() < task.getVersion()) {
                        activateGroupOps.put(task.getGroupId(), task);
                    }
                    break;

                case TaskOpsType.ACTIVATE_GROUP_CANARY:
                    if (!activateCanaryGroupOps.containsKey(task.getGroupId())
                            || activateCanaryGroupOps.get(task.getGroupId()).getVersion() < task.getVersion()) {
                        activateCanaryGroupOps.put(task.getGroupId(), task);
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
                    taskList = memberOps.computeIfAbsent(task.getGroupId(), k -> new ArrayList<>());
                    taskList.add(task);
                    break;
                case TaskOpsType.PULL_MEMBER_OPS:
                    taskList = pullMemberOps.computeIfAbsent(task.getGroupId(), k -> new ArrayList<>());
                    taskList.add(task);
                    break;
                case TaskOpsType.HEALTHY_OPS:
                    taskList = healthyOps.computeIfAbsent(task.getGroupId(), k -> new ArrayList<>());
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

    public HashMap<String, OpsTask> getServerOps() {
        return serverOps;
    }

    public TasksEntity setServerOps(HashMap<String, OpsTask> serverOps) {
        this.serverOps = serverOps;
        return this;
    }

    public HashMap<Long, OpsTask> getActivateGroupOps() {
        return activateGroupOps;
    }

    public HashMap<Long, OpsTask> getActivateCanaryGroupOps() {
        return activateCanaryGroupOps;
    }

    public TasksEntity setActivateCanaryGroupOps(HashMap<Long, OpsTask> activateCanaryGroupOps) {
        this.activateCanaryGroupOps = activateCanaryGroupOps;
        return this;
    }

    public TasksEntity setActivateGroupOps(HashMap<Long, OpsTask> activateGroupOps) {
        this.activateGroupOps = activateGroupOps;
        return this;
    }

    public HashMap<Long, OpsTask> getDeactivateGroupOps() {
        return deactivateGroupOps;
    }

    public TasksEntity setDeactivateGroupOps(HashMap<Long, OpsTask> deactivateGroupOps) {
        this.deactivateGroupOps = deactivateGroupOps;
        return this;
    }

    public HashMap<Long, OpsTask> getSoftDeactivateGroupOps() {
        return softDeactivateGroupOps;
    }

    public TasksEntity setSoftDeactivateGroupOps(HashMap<Long, OpsTask> softDeactivateGroupOps) {
        this.softDeactivateGroupOps = softDeactivateGroupOps;
        return this;
    }

    public HashMap<Long, OpsTask> getActivatePolicyOps() {
        return activatePolicyOps;
    }

    public TasksEntity setActivatePolicyOps(HashMap<Long, OpsTask> activatePolicyOps) {
        this.activatePolicyOps = activatePolicyOps;
        return this;
    }

    public HashMap<Long, OpsTask> getDeactivatePolicyOps() {
        return deactivatePolicyOps;
    }

    public TasksEntity setDeactivatePolicyOps(HashMap<Long, OpsTask> deactivatePolicyOps) {
        this.deactivatePolicyOps = deactivatePolicyOps;
        return this;
    }

    public HashMap<Long, OpsTask> getSoftDeactivatePolicyOps() {
        return softDeactivatePolicyOps;
    }

    public TasksEntity setSoftDeactivatePolicyOps(HashMap<Long, OpsTask> softDeactivatePolicyOps) {
        this.softDeactivatePolicyOps = softDeactivatePolicyOps;
        return this;
    }

    public HashMap<Long, OpsTask> getActivateDrOps() {
        return activateDrOps;
    }

    public TasksEntity setActivateDrOps(HashMap<Long, OpsTask> activateDrOps) {
        this.activateDrOps = activateDrOps;
        return this;
    }

    public HashMap<Long, OpsTask> getDeactivateDrOps() {
        return deactivateDrOps;
    }

    public TasksEntity setDeactivateDrOps(HashMap<Long, OpsTask> deactivateDrOps) {
        this.deactivateDrOps = deactivateDrOps;
        return this;
    }

    public HashMap<Long, OpsTask> getSoftDeactivateDrOps() {
        return softDeactivateDrOps;
    }

    public TasksEntity setSoftDeactivateDrOps(HashMap<Long, OpsTask> softDeactivateDrOps) {
        this.softDeactivateDrOps = softDeactivateDrOps;
        return this;
    }

    public HashMap<Long, OpsTask> getActivateVsOps() {
        return activateVsOps;
    }

    public TasksEntity setActivateVsOps(HashMap<Long, OpsTask> activateVsOps) {
        this.activateVsOps = activateVsOps;
        return this;
    }

    public HashMap<Long, OpsTask> getDeactivateVsOps() {
        return deactivateVsOps;
    }

    public TasksEntity setDeactivateVsOps(HashMap<Long, OpsTask> deactivateVsOps) {
        this.deactivateVsOps = deactivateVsOps;
        return this;
    }

    public HashMap<Long, OpsTask> getSoftDeactivateVsOps() {
        return softDeactivateVsOps;
    }

    public TasksEntity setSoftDeactivateVsOps(HashMap<Long, OpsTask> softDeactivateVsOps) {
        this.softDeactivateVsOps = softDeactivateVsOps;
        return this;
    }

    public HashMap<Long, OpsTask> getActivateSlbOps() {
        return activateSlbOps;
    }

    public TasksEntity setActivateSlbOps(HashMap<Long, OpsTask> activateSlbOps) {
        this.activateSlbOps = activateSlbOps;
        return this;
    }

    public HashMap<Long, List<OpsTask>> getMemberOps() {
        return memberOps;
    }

    public TasksEntity setMemberOps(HashMap<Long, List<OpsTask>> memberOps) {
        this.memberOps = memberOps;
        return this;
    }

    public HashMap<Long, List<OpsTask>> getPullMemberOps() {
        return pullMemberOps;
    }

    public TasksEntity setPullMemberOps(HashMap<Long, List<OpsTask>> pullMemberOps) {
        this.pullMemberOps = pullMemberOps;
        return this;
    }

    public HashMap<Long, List<OpsTask>> getHealthyOps() {
        return healthyOps;
    }

    public TasksEntity setHealthyOps(HashMap<Long, List<OpsTask>> healthyOps) {
        this.healthyOps = healthyOps;
        return this;
    }

    public HashMap<Long, List<OpsTask>> getAggSubTasks() {
        return aggSubTasks;
    }

    public TasksEntity setAggSubTasks(HashMap<Long, List<OpsTask>> aggSubTasks) {
        this.aggSubTasks = aggSubTasks;
        return this;
    }

    public HashMap<Long, List<OpsTask>> getAggSyncVsActivateTasks() {
        return aggSyncVsActivateTasks;
    }

    public TasksEntity setAggSyncVsActivateTasks(HashMap<Long, List<OpsTask>> aggSyncVsActivateTasks) {
        this.aggSyncVsActivateTasks = aggSyncVsActivateTasks;
        return this;
    }

    public HashMap<Long, OpsTask> getAggTasks() {
        return aggTasks;
    }

    public TasksEntity setAggTasks(HashMap<Long, OpsTask> aggTasks) {
        this.aggTasks = aggTasks;
        return this;
    }
}
