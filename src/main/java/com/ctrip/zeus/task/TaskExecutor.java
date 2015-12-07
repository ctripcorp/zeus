package com.ctrip.zeus.task;

import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicLongProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author:xingchaowang
 * @date: 12/7/2015.
 */
public class TaskExecutor extends Thread{
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private DynamicBooleanProperty taskEnabled = null;
    private DynamicLongProperty taskInterval = null;

    private Task task;

    public TaskExecutor(Task task) {
        this.task = task;

        taskEnabled = DynamicPropertyFactory.getInstance().getBooleanProperty("task."+task.getName()+".enabled", true);
        taskInterval = DynamicPropertyFactory.getInstance().getLongProperty("task."+task.getName()+".interval", task.getInterval());

        setName("Task-Executor-" + task.getName());
        setDaemon(true);
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (!taskEnabled.get()) continue;
                task.run();
            } catch (Exception e) {
                logger.error("Encounter an error while task executing.", e);
            } finally {
                try {
                    sleep(taskInterval.get());
                } catch (Exception e) {
                    logger.warn("Encounter an error while task sleeping.", e);
                }
            }
        }
    }
}
