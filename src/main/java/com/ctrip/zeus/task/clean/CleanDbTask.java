package com.ctrip.zeus.task.clean;

import com.ctrip.zeus.clean.CleanDbManager;
import com.ctrip.zeus.task.AbstractTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

/**
 * Created by fanqq on 2016/1/5.
 */
public class CleanDbTask extends AbstractTask {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private CleanDbManager cleanDbManager;

    @Override
    public void start() {

    }

    @Override
    public void run() throws Exception {
        try {
            cleanDbManager.run();
        } catch (Exception e) {
            logger.warn("[clean db job] clean db exception." + e.getMessage(), e);
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public long getInterval() {
        return 60000 * 60;
    }
}
