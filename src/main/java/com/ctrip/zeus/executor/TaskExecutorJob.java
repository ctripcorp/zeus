package com.ctrip.zeus.executor;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * Created by fanqq on 2015/8/7.
 */
public class TaskExecutorJob extends QuartzJobBean {
    TaskWorker taskWorker;
    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        taskWorker.execute();
    }

    public void setTaskExecutor(TaskWorker taskWorker) {
        this.taskWorker = taskWorker;
    }
}
