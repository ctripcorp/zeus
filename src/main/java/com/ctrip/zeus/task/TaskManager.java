package com.ctrip.zeus.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author:xingchaowang
 * @date: 12/7/2015.
 */
@Component("taskManager2")
public class TaskManager {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public void add(Task task) {
        logger.info("Add task " + task.getClass().getSimpleName());
        new TaskExecutor(task).start();
    }
}
