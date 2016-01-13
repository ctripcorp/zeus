package com.ctrip.zeus.task.operation;

import com.ctrip.zeus.executor.TaskWorker;
import com.ctrip.zeus.task.AbstractTask;
import com.ctrip.zeus.util.S;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by fanqq on 2015/12/28.
 */
@Component("operationTask")
public class OperationTask extends AbstractTask {
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
            logger.error("[OperationTask] OperationTask Exception! ServerIp:" + S.getIp(), e);
        }
    }

    @Override
    public long getInterval() {
        return 1000;
    }

    @Override
    public void stop() {

    }
}
