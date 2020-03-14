package com.ctrip.zeus.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author:xingchaowang
 * @date: 12/7/2015.
 */
@Component("taskManager2")
public class TaskManager {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private Map<String, TaskExecutor> taskExecutors = new HashMap<>();

    public void add(Task task) {
        logger.info("Add task " + task.getClass().getSimpleName());
        TaskExecutor ex = new TaskExecutor(task);
        if (taskExecutors.containsKey(task.getName())) {
            logger.warn("Task " + task.getName() + " already exists, add duplicate task may cause memory leak problem.");
        }
        taskExecutors.put(task.getName(), ex);
        ex.start();
    }

    public void remove(Task task) {
        TaskExecutor ex = taskExecutors.get(task.getName());
        if (ex != null)
            ex.shutDown();
        taskExecutors.remove(task.getName());
    }
}