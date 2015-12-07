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
    private void init() {
        taskManager2.add(this);
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public long getInterval() {
        return 60000;
    }

    public abstract void run();
}
