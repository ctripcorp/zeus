package com.ctrip.zeus.task.operation;

import com.ctrip.zeus.executor.scheduler.SlbTaskStarter;
import com.ctrip.zeus.task.AbstractTask;
import com.ctrip.zeus.util.EnvHelper;
import com.netflix.config.DynamicPropertyFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("slbTaskStarterTask")
public class SlbTaskStarterTask extends AbstractTask {
    @Resource
    private SlbTaskStarter slbTaskStarter;

    @Override
    public void start() {
        if (!EnvHelper.portal()) {
            return;
        }
        slbTaskStarter.startUp();
    }

    @Override
    public void run() throws Exception {
        if (!EnvHelper.portal()) {
            return;
        }
        slbTaskStarter.startUp();
    }

    @Override
    public void stop() {

    }

    @Override
    public long getInterval() {
        return 60000;
    }
}
