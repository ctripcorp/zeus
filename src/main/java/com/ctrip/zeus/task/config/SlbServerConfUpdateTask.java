package com.ctrip.zeus.task.config;

import com.ctrip.zeus.service.update.SlbServerConfManager;
import com.ctrip.zeus.task.AbstractTask;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by fanqq on 2016/3/17.
 */
@Component("slbServerConfUpdateTask")
public class SlbServerConfUpdateTask extends AbstractTask {
    @Resource
    private SlbServerConfManager slbServerConfManager;

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static DynamicIntProperty interval = DynamicPropertyFactory.getInstance().getIntProperty("slb.server.conf.update.task.interval", 30000);

    @Override
    public void start() {

    }

    @Override
    public void run() throws Exception {
        try {
            slbServerConfManager.update(false, false);
        } catch (Exception e) {
            logger.error("[SlbServerConfUpdateTask] Execute Failed.", e);
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public long getInterval() {
        return interval.get();
    }
}
