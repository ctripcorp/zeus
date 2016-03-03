package com.ctrip.zeus.task.executor;

import com.ctrip.zeus.executor.TaskWorker;
import com.ctrip.zeus.task.AbstractTask;
import com.ctrip.zeus.util.S;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

/**
 * Created by fanqq on 2016/3/1.
 */
public class TaskExecutorTask extends AbstractTask {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private TaskWorker taskWorker;

    @Override
    public void start() {

    }

    @Override
    public void run() throws Exception {
        try {
            taskWorker.execute();
        } catch (Exception e) {
            logger.error("[TaskExecutorTask] TaskExecutorTask Exception! ", e);
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public long getInterval() {
        return 1000;
    }
}
