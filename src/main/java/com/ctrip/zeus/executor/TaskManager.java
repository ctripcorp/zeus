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
    private static final DynamicIntProperty taskCheckStatusInterval = DynamicPropertyFactory.getInstance().getIntProperty("task.check.status.interval", 300);


    public Long addTask(OpsTask task)throws Exception{
        return taskService.add(task);
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
        Thread.sleep(taskCheckStatusInterval.get());
        while (true) {
            Thread.sleep(taskCheckStatusInterval.get());
            List<TaskResult> results = taskService.getResult(taskIds);
            if (results != null) {
                for (TaskResult taskResult : results){
                    if (!taskResult.isSuccess()){
                        throw new Exception("Task Failed! TaskResults: \n"+String.format(TaskResult.JSON,taskResult));
                    }
                }
                return results;
            } else if (System.currentTimeMillis() > deadLine) {
                List<Long> cancelFail = new ArrayList<>();
                for (Long tid : taskIds){
                    if (!taskService.taskCancel(tid)){
                        cancelFail.add(tid);
                    }
                }
                throw new Exception("Task Timeout. TasksIds: "+taskIds.toString() + "\nCanceled TaskIds: "+cancelFail.toString());
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
