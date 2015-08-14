package com.ctrip.zeus.service.task.impl;

import com.ctrip.zeus.dal.core.TaskDao;
import com.ctrip.zeus.dal.core.TaskDo;
import com.ctrip.zeus.dal.core.TaskEntity;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.service.task.TaskService;
import com.ctrip.zeus.service.task.constant.TaskOpsType;
import com.ctrip.zeus.service.task.constant.TaskStatus;
import com.ctrip.zeus.support.C;
import com.ctrip.zeus.task.entity.OpsTask;
import com.ctrip.zeus.task.entity.TaskResult;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
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

    private static final DynamicIntProperty taskCheckStatusInterval = DynamicPropertyFactory.getInstance().getIntProperty("task.check.status.interval", 200);
    private static DynamicIntProperty lockTimeout = DynamicPropertyFactory.getInstance().getIntProperty("lock.timeout", 5000);

    @Resource
    private TaskDao taskDao;
    @Resource
    private DbLockFactory dbLockFactory;

    @Override
    public Long add(OpsTask task) throws Exception {
        TaskDo taskDo = C.toTaskDo(task);
        taskDo.setStatus(TaskStatus.PENDING);
        String lockName = null;
        if ( taskDo.getOpsType().equals(TaskOpsType.ACTIVATE_SLB)){
            lockName = "AddTask_" + taskDo.getOpsType() + taskDo.getSlbId();
        }else if (taskDo.getOpsType().equals(TaskOpsType.SERVER_OPS)){
            lockName = "AddTask_" + taskDo.getOpsType();
        }else {
            lockName = "AddTask_" + taskDo.getOpsType() + taskDo.getGroupId();
        }
        DistLock buildLock = dbLockFactory.newLock( lockName );
        try {
            buildLock.lock(lockTimeout.get());
            taskDao.insert(taskDo);
        }finally {
            buildLock.unlock();
        }
        return taskDo.getId();
    }

    @Override
    public List<Long> add(List<OpsTask> tasks) throws Exception {
        List<Long> result = new ArrayList<>();
        for (OpsTask task : tasks){
            result.add(add(task));
        }
        return result;
    }

    @Override
    public List<TaskResult> getResult(List<Long> taskIds,Long timeOut) throws Exception {
        Long deadLine = System.currentTimeMillis() + timeOut;
        List<TaskResult> results = new ArrayList<>();
        List<Long> ids = new ArrayList<>(taskIds);
        while (true){
            Thread.sleep(taskCheckStatusInterval.get());
            for (int i = 0 ; i < ids.size() ; i ++){
                TaskDo tmp = taskDao.findByPK(ids.get(i), TaskEntity.READSET_FULL);
                if (tmp.getStatus().equals(TaskStatus.SUCCESS)){
                    results.add(new TaskResult().setDateTime(new Date()).setSuccess(true).setOpsTask(C.toOpsTask(tmp)));
                    ids.remove(i--);
                }
                if (tmp.getStatus().equals(TaskStatus.FAIL)){
                    results.add(new TaskResult().setDateTime(new Date()).setSuccess(false).setFailCause(tmp.getFailCause()).setOpsTask(C.toOpsTask(tmp)));
                    ids.remove(i--);
                }
            }
            if ( ids.size() == 0){
                break;
            }
            if (System.currentTimeMillis() > deadLine){
                throw new Exception("Get Operation Result TimeOut, Operation is still in task list .");
            }
        }
        return results;
    }

    @Override
    public TaskResult getResult(Long taskId, Long timeOut) throws Exception {
        Long deadLine = System.currentTimeMillis() + timeOut;
        TaskResult result = new TaskResult();
        while (true){
            Thread.sleep(taskCheckStatusInterval.get());

            TaskDo tmp = taskDao.findByPK(taskId, TaskEntity.READSET_FULL);
            if (tmp.getStatus().equals(TaskStatus.SUCCESS)){
                result.setDateTime(new Date()).setSuccess(true).setOpsTask(C.toOpsTask(tmp));
                break;
            }
            if (tmp.getStatus().equals(TaskStatus.FAIL)){
                result.setDateTime(new Date()).setSuccess(false).setFailCause(tmp.getFailCause()).setOpsTask(C.toOpsTask(tmp));
                break;
            }
            if (System.currentTimeMillis() > deadLine){
                throw new Exception("Get Operation Result TimeOut, Operation is still in task list .");
            }
        }
        return result;
    }

    @Override
    public List<OpsTask> getPendingTasks(Long slbId) throws Exception {
        List<TaskDo> tmp = taskDao.findByTargetSlbIdAndStatus(TaskStatus.PENDING,slbId,TaskEntity.READSET_FULL);
        List<OpsTask> result = new ArrayList<>();
        for (TaskDo taskDo :tmp){
            result.add(C.toOpsTask(taskDo));
        }
        return result;
    }

    @Override
    public void updateTasks(List<OpsTask> tasks) throws Exception {
        for (OpsTask opsTask : tasks){
            taskDao.updateByPK(C.toTaskDo(opsTask),TaskEntity.UPDATESET_FULL);
        }
    }
}
