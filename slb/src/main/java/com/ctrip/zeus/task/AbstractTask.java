package com.ctrip.zeus.task;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author:xingchaowang
 * @date: 12/7/2015.
 */
public abstract class AbstractTask implements Task {
    @Resource
    private TaskManager taskManager2;

    @PostConstruct
    protected void init() {
        start();
        taskManager2.add(this);
    }

    public abstract void start();

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public long getInterval() {
        return 60000;
    }

    public abstract void run() throws Exception;

    @Override
    public void shutDown() {
        stop();
        taskManager2.remove(this);
        taskManager2 = null;
    }

    public abstract void stop();
}
