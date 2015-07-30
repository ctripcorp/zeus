package com.ctrip.zeus.service.woker.impl;

import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.service.task.TaskService;
import com.ctrip.zeus.service.woker.TaskExecutor;
import com.ctrip.zeus.task.entity.OpsTask;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by fanqq on 2015/7/29.
 */
@Component("taskExecutor")
public class TaskExecutorImpl implements TaskExecutor {

    @Resource
    private DbLockFactory dbLockFactory;
    @Resource
    private TaskService taskService;

    private static DynamicIntProperty lockTimeout = DynamicPropertyFactory.getInstance().getIntProperty("lock.timeout", 5000);
    Logger logger = LoggerFactory.getLogger(this.getClass());


    @Override
    public void execute(Long slbId) {
        DistLock buildLock = dbLockFactory.newLock( "TaskWorker_" + slbId );
        try {
            buildLock.lock(lockTimeout.get());
            executeJob(slbId);
        }catch (Exception e){
            logger.warn("TaskWorker get lock failed! TaskWorker: "+slbId);
        } finally{
            buildLock.unlock();
        }
    }

    private void executeJob(Long slbId){
        //1. get pending tasks , if size == 0 return
        List<OpsTask> tasks = null;
        try {
             tasks = taskService.getPendingTasks(slbId);
        }catch (Exception e){
            logger.warn("Task Executor get pending tasks failed! ", e);
            return;
        }
        if (tasks.size()==0) return;

        //2. get all tasks datas


    }
}
