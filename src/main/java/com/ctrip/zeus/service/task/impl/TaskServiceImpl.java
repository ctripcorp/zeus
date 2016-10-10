package com.ctrip.zeus.service.task.impl;

import com.ctrip.zeus.dal.core.TaskDao;
import com.ctrip.zeus.dal.core.TaskDo;
import com.ctrip.zeus.dal.core.TaskEntity;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.service.task.TaskService;
import com.ctrip.zeus.service.task.constant.TaskStatus;
import com.ctrip.zeus.support.C;
import com.ctrip.zeus.task.entity.OpsTask;
import com.ctrip.zeus.task.entity.OpsTaskList;
import com.ctrip.zeus.task.entity.TaskResult;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by fanqq on 2015/7/28.
 */
@Component("taskService")
public class TaskServiceImpl implements TaskService {

    @Resource
    private TaskDao taskDao;

    private final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    @Override
    public Long add(OpsTask task) throws Exception {
        TaskDo taskDo = C.toTaskDo(task);
        taskDo.setStatus(TaskStatus.PENDING);
        taskDo.setCreateTime(new Date());
        taskDao.insert(taskDo);
        Long taskId = taskDo.getId();
        StringBuilder sb = new StringBuilder(128);
        sb.append("[[");
        sb.append("opsType=").append(task.getOpsType());
        sb.append(",taskId=").append(taskId);
        sb.append(",targetSlbId=").append(task.getTargetSlbId());
        if (task.getGroupId() != null) {
            sb.append(",groupId=").append(task.getGroupId());
        }
        if (task.getVersion() != null) {
            sb.append(",version=").append(task.getVersion());
        }
        if (task.getSlbVirtualServerId() != null) {
            sb.append(",vsId=").append(task.getSlbVirtualServerId());
        }
        if (task.getSlbId() != null) {
            sb.append(",slbId=").append(task.getSlbId());
        }
        sb.append("]]");
        sb.append("Task Added.Task info:［").append(task.toString()).append("]");
        logger.info(sb.toString());
        return taskDo.getId();
    }

    @Override
    public List<Long> add(List<OpsTask> tasks) throws Exception {
        List<Long> result = new ArrayList<>();
        for (OpsTask task : tasks) {
            result.add(add(task));
        }
        return result;
    }

    @Override
    public List<TaskResult> getResult(List<Long> taskIds) throws Exception {
        List<TaskResult> results = new ArrayList<>();
        List<Long> ids = new ArrayList<>(taskIds);
        List<TaskDo> tmp = taskDao.findByIds(ids.toArray(new Long[ids.size()]), TaskEntity.READSET_FULL);
        if (tmp == null || tmp.size() != ids.size()) {
            return null;
        }
        for (TaskDo task : tmp) {
            if (task.getStatus().equals(TaskStatus.SUCCESS)) {
                results.add(new TaskResult().setDateTime(new Date()).setSuccess(true).setOpsTask(C.toOpsTask(task)));
            }
            if (task.getStatus().equals(TaskStatus.FAIL)) {
                results.add(new TaskResult().setDateTime(new Date()).setSuccess(false).setFailCause(task.getFailCause()).setOpsTask(C.toOpsTask(task)));
            }
        }
        if (results.size() == tmp.size()) {
            return results;
        } else {
            return null;
        }
    }

    @Override
    public TaskResult getResult(Long taskId) throws Exception {
        TaskResult result = new TaskResult();

        TaskDo tmp = taskDao.findByPK(taskId, TaskEntity.READSET_FULL);
        if (tmp == null) return null;
        if (tmp.getStatus().equals(TaskStatus.SUCCESS)) {
            result.setDateTime(new Date()).setSuccess(true).setOpsTask(C.toOpsTask(tmp));
        } else if (tmp.getStatus().equals(TaskStatus.FAIL)) {
            result.setDateTime(new Date()).setSuccess(false).setFailCause(tmp.getFailCause()).setOpsTask(C.toOpsTask(tmp));
        } else {
            return null;
        }
        return result;
    }

    @Override
    public List<OpsTask> getPendingTasks(Long slbId) throws Exception {
        List<TaskDo> tmp = taskDao.findByTargetSlbIdAndStatus(TaskStatus.PENDING, slbId, TaskEntity.READSET_FULL);
        List<OpsTask> result = new ArrayList<>();
        for (TaskDo taskDo : tmp) {
            result.add(C.toOpsTask(taskDo));
        }
        return result;
    }

    @Override
    public void updateTasks(List<OpsTask> tasks) throws Exception {
        for (OpsTask opsTask : tasks) {
            taskDao.updateByPK(C.toTaskDo(opsTask), TaskEntity.UPDATESET_FULL);
            StringBuilder msg = new StringBuilder(128);
            msg.append("[[taskId=").append(opsTask.getId());
            msg.append(",taskStatus=").append(opsTask.getStatus());
            msg.append(",opsType=").append(opsTask.getOpsType());
            msg.append(",targetSlbId=").append(opsTask.getTargetSlbId());
            if (opsTask.getGroupId() != null) {
                msg.append(",groupId=").append(opsTask.getGroupId());
            }
            if (opsTask.getVersion() != null) {
                msg.append(",version=").append(opsTask.getVersion());
            }
            if (opsTask.getSlbVirtualServerId() != null) {
                msg.append(",vsId=").append(opsTask.getSlbVirtualServerId());
            }
            if (opsTask.getSlbId() != null) {
                msg.append(",slbId=").append(opsTask.getSlbId());
            }
            msg.append("]]Task Execute Finished.TaskId:[").append(opsTask.getId()).append("];Status: ")
                    .append(opsTask.getStatus()).append(";failureCause:").append(opsTask.getFailCause());
            logger.info(msg.toString());
        }
    }

    @Override
    public OpsTaskList find(Date fromDate, String opsType, Long targetSlbId) throws Exception {
        if (targetSlbId == null) {
            targetSlbId = -1L;
        }
        List<TaskDo> taskDos = taskDao.find(fromDate, opsType, targetSlbId, TaskEntity.READSET_FULL);
        OpsTaskList result = new OpsTaskList();
        if (taskDos == null) {
            return result;
        }
        for (TaskDo taskDo : taskDos) {
            result.addOpsTask(C.toOpsTask(taskDo));
        }
        return result;
    }

    @Override
    public boolean taskCancel(Long taskId) throws Exception {
        int i = taskDao.compareAndUpdate(new TaskDo().setFailCause("Task Pending timeout.Task is canceled").setId(taskId).setStatus(TaskStatus.FAIL).setExpectStatus(TaskStatus.PENDING), TaskEntity.UPDATESET_FULL);
        return i > 0;
    }
}
