package com.ctrip.zeus.executor;

import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.service.task.TaskService;
import com.ctrip.zeus.service.task.constant.TaskOpsType;
import com.ctrip.zeus.task.entity.OpsTask;
import com.ctrip.zeus.task.entity.TaskResult;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fanqq on 2015/8/14.
 */
@Component("taskManager")
public class TaskManager {

    @Resource
    private TaskService taskService;
    @Resource
    private DbLockFactory dbLockFactory;

    private static DynamicIntProperty lockTimeout = DynamicPropertyFactory.getInstance().getIntProperty("lock.timeout", 5000);
    private static final DynamicIntProperty taskCheckStatusInterval = DynamicPropertyFactory.getInstance().getIntProperty("task.check.status.interval", 200);


    public Long addTask(OpsTask task)throws Exception{
        String lockName = null;
        if ( task.getOpsType().equals(TaskOpsType.ACTIVATE_SLB)){
            lockName = "AddTask_" + task.getOpsType() + task.getSlbId();
        }else if (task.getOpsType().equals(TaskOpsType.SERVER_OPS)){
            lockName = "AddTask_" + task.getOpsType();
        }else {
            lockName = "AddTask_" + task.getOpsType() + task.getGroupId();
        }
        DistLock buildLock = dbLockFactory.newLock( lockName );
        try {
            buildLock.lock(lockTimeout.get());
            return taskService.add(task);
        }finally {
            buildLock.unlock();
        }
    }
    public List<Long> addTask(List<OpsTask> tasks)throws Exception{
        List<Long> result = new ArrayList<>();
        for (OpsTask task : tasks){
            result.add(addTask(task));
        }
        return result;
    }

    public List<TaskResult> getResult(List<Long> taskIds , Long timeOut) throws Exception{
        Long deadLine = System.currentTimeMillis() + timeOut;
        while (true) {
            Thread.sleep(taskCheckStatusInterval.get());
            List<TaskResult> results = taskService.getResult(taskIds);
            if (results != null) {
                for (TaskResult taskResult : results){
                    if (!taskResult.isSuccess()){
                        throw new Exception("Some Task Failed! TaskResults: "+taskResult.toString());
                    }
                }
                return results;
            } else if (System.currentTimeMillis() > deadLine) {
                throw new Exception("Get Operation Result TimeOut, Operation is still in task list .TaskIds: "+taskIds.toString());
            }
        }
    }

    public TaskResult getResult(Long taskId, Long timeOut) throws Exception{
        Long deadLine = System.currentTimeMillis() + timeOut;
        while (true) {
            Thread.sleep(taskCheckStatusInterval.get());
            TaskResult results = taskService.getResult(taskId);
            if (results != null) {
                return results;
            } else if (System.currentTimeMillis() > deadLine) {
                throw new Exception("Get Operation Result TimeOut, Operation is still in task list .TaskId: "+taskId.toString());
            }
        }
    }

}
