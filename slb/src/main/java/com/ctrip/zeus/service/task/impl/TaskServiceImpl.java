package com.ctrip.zeus.service.task.impl;

import com.ctrip.zeus.dao.entity.TaskTask;
import com.ctrip.zeus.dao.entity.TaskTaskExample;
import com.ctrip.zeus.dao.mapper.TaskTaskMapper;
import com.ctrip.zeus.model.task.OpsTask;
import com.ctrip.zeus.model.task.OpsTaskList;
import com.ctrip.zeus.model.task.TaskResult;
import com.ctrip.zeus.service.task.TaskService;
import com.ctrip.zeus.service.task.constant.TaskOpsType;
import com.ctrip.zeus.service.task.constant.TaskStatus;
import com.ctrip.zeus.support.C;
import com.ctrip.zeus.support.DefaultObjectJsonParser;
import com.ctrip.zeus.support.DefaultObjectJsonWriter;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by fanqq on 2015/7/28.
 */
@Component("taskService")
public class TaskServiceImpl implements TaskService {

    @Resource
    private TaskTaskMapper taskTaskMapper;

    private final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    @VisibleForTesting
    public void setTaskTaskMapper(TaskTaskMapper mapper) {
        this.taskTaskMapper = mapper;
    }

    @Override
    public Long add(OpsTask task) throws Exception {
        Long taskId = null;

        TaskTask taskTask = C.toTaskTask(task);
        taskTask.setStatus(TaskStatus.PENDING);
        if (taskTaskMapper.insert(taskTask) == 0) {
            logger.warn("Task insertion failed when calling taskTaskMapper.insert(TaskTask).");
            return null;
        }
        taskId = taskTask.getId();

        // Log task insertion.
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
        sb.append("Task Added.Task info:[").append(task.toString()).append("]");
        logger.info(sb.toString());

        return taskId;
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
        if (taskIds == null || taskIds.size() <= 0) {
            return null;
        }
        List<TaskResult> results = new ArrayList<>(taskIds.size());

        TaskTaskExample example = new TaskTaskExample();
        example.or().andIdIn(taskIds);
        List<TaskTask> tasks = taskTaskMapper.selectByExampleWithBLOBs(example);
        if (tasks == null || tasks.size() != taskIds.size()) {
            return null;
        }
        for (TaskTask task : tasks) {
            if (!isTaskFinished(task)) {
                return null;
            }
            List<TaskResult> results1 = parseOpsTaskResult(C.toOpsTask(task));
            if (results1 == null || results1.size() != 1) {
                return null;
            }
            results.add(results1.get(0));
        }
        return results;
    }

    private List<TaskResult> parseOpsTaskResult(OpsTask opsTask) {
        List<TaskResult> results = new ArrayList<>();
        List<OpsTask> subTasks = new ArrayList<>();
        if (!opsTask.getOpsType().equalsIgnoreCase(TaskOpsType.AGGREGATION)) {
            subTasks.add(opsTask);
        } else {
            if (opsTask.getTaskList() != null) {
                List<OpsTask> tmp = DefaultObjectJsonParser.parseArray(opsTask.getTaskList(), OpsTask.class);
                if (tmp == null)
                    return null;
                subTasks.addAll(tmp);
            }
        }
        for (OpsTask item : subTasks) {
            item.setId(opsTask.getId());
            TaskResult result = new TaskResult().setOpsTask(item).setDateTime(new Date());
            boolean successful = item.getStatus().equalsIgnoreCase(TaskStatus.SUCCESS);
            if (successful) {
                result.setSuccess(true);
            } else {
                result.setSuccess(false);
                result.setFailCause(item.getFailCause());
            }
            results.add(result);
        }
        return results;
    }

    @Override
    public List<Long> addAggTasks(List<OpsTask> task) throws Exception {
        Map<Long, List<OpsTask>> map = new HashMap<>();
        List<Long> result = new ArrayList<>();
        for (OpsTask t : task) {
            List<OpsTask> l = map.get(t.getTargetSlbId());
            if (l == null) {
                l = new ArrayList<>();
                map.put(t.getTargetSlbId(), l);
            }
            t.setStatus(TaskStatus.PENDING);
            l.add(t);
        }
        for (Long id : map.keySet()) {
            OpsTask tmpTask = new OpsTask();
            tmpTask.setTargetSlbId(id);
            tmpTask.setOpsType(TaskOpsType.AGGREGATION);
            tmpTask.setTaskList(DefaultObjectJsonWriter.write(map.get(id)));
            tmpTask.setCreateTime(new Date());
            result.add(add(tmpTask));
        }
        return result;
    }

    @Override
    public List<TaskResult> getAggResult(List<Long> taskIds) throws Exception {
        if (taskIds == null || taskIds.size() == 0) {
            return null;
        }
        List<TaskResult> results = new LinkedList<>();

        TaskTaskExample example = new TaskTaskExample();
        example.or().andIdIn(taskIds);
        List<TaskTask> tasks = taskTaskMapper.selectByExampleWithBLOBs(example);
        if (tasks.size() != taskIds.size()) {
            return null;
        }
        for (TaskTask task : tasks) {
            if (!isTaskFinished(task)) {
                return null;
            }
            List<TaskResult> taskResultList = parseOpsTaskResult(C.toOpsTask(task));
            if (taskResultList != null) {
                results.addAll(taskResultList);
            }
        }
        return results;
    }

    private boolean isTaskFinished(TaskTask task) {
        if (task != null) {
            return TaskStatus.SUCCESS.equalsIgnoreCase(task.getStatus()) || TaskStatus.FAIL.equalsIgnoreCase(task.getStatus());
        }
        return true;
    }

    @Override
    public TaskResult getResult(Long taskId) throws Exception {
        List<TaskResult> results = getResult(Arrays.asList(taskId));
        if (results == null || results.size() != 1) {
            return null;
        }
        return results.get(0);
    }

    @Override
    public List<OpsTask> getPendingTasks(Long slbId) throws Exception {
        TaskTaskExample example = new TaskTaskExample();
        example.or().andTargetSlbIdEqualTo(slbId).andStatusEqualTo(TaskStatus.PENDING);
        List<TaskTask> tasks = taskTaskMapper.selectByExampleWithBLOBs(example);

        if (tasks == null) {
            return null;
        }
        List<OpsTask> results = new ArrayList<>(tasks.size());
        for (TaskTask task : tasks) {
            results.add(C.toOpsTask(task));
        }
        return results;
    }

    @Override
    public void updateTasks(List<OpsTask> tasks) throws Exception {
        for (OpsTask task : tasks) {
            taskTaskMapper.updateByPrimaryKeyWithBLOBs(C.toTaskTask(task));
        }

        for (OpsTask opsTask : tasks) {
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
            msg.append("]]Mybatis Task update Finished.TaskId");
            logger.info(msg.toString());
        }
    }

    @Override
    public OpsTaskList find(Date fromDate, String opsType, Long targetSlbId) throws Exception {
        if (targetSlbId == null) {
            targetSlbId = -1L;
        }
        TaskTaskExample example = new TaskTaskExample();
        example.or().andOpsTypeEqualTo(opsType).andTargetSlbIdEqualTo(targetSlbId).andCreateTimeGreaterThanOrEqualTo(fromDate);
        List<TaskTask> tasks = taskTaskMapper.selectByExampleWithBLOBs(example);
        OpsTaskList result = new OpsTaskList();

        for (TaskTask taskTask : tasks) {
            result.addOpsTask(C.toOpsTask(taskTask));
        }
        return result;
    }

    @Override
    public TaskTask taskCancel(Long taskId) throws Exception {
        if (taskId == null) {
            return null;
        }
        TaskTaskExample example = new TaskTaskExample();
        example.or().andIdEqualTo(taskId).andStatusEqualTo(TaskStatus.PENDING);
        TaskTask record = new TaskTask();
        record.setFailCause("Task Pending timeout. Task is canceled");
        record.setStatus(TaskStatus.FAIL);
        record.setId(taskId);
        // i is the count of matched entry in table, not the updated count of entry in table
        int i = taskTaskMapper.updateByExampleSelective(record, example);

        if (i >= 1) return record;
        return null;
    }

    @Override
    public int deleteBeforeDate(TaskTask proto) throws Exception {
        TaskTaskExample example = new TaskTaskExample();
        example.or().andCreateTimeLessThan(proto.getCreateTime());
        example.limit(1000);
        return taskTaskMapper.deleteByExample(example);
    }

}
