package com.ctrip.zeus.executor.scheduler;

import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component("TaskScheduler")
public class TaskScheduler {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private static DynamicIntProperty threadPoolSize = DynamicPropertyFactory.getInstance().getIntProperty("task.scheduler.thread.pool.max.size", 200);
    private static DynamicIntProperty threadPoolCoreSize = DynamicPropertyFactory.getInstance().getIntProperty("task.scheduler.thread.pool.core.size", 10);
    private static DynamicIntProperty threadPoolKeepAlive = DynamicPropertyFactory.getInstance().getIntProperty("task.scheduler.thread.pool.keepalive", 60);

    private ScheduledThreadPoolExecutor threadPoolExecutor;
    private Map<Long, SlbTaskWorker> workerMap = new HashMap<>();


    TaskScheduler() {
        threadPoolExecutor = new ScheduledThreadPoolExecutor(threadPoolCoreSize.get());
        threadPoolExecutor.setMaximumPoolSize(threadPoolSize.get());
        threadPoolExecutor.setKeepAliveTime(threadPoolKeepAlive.get(), TimeUnit.SECONDS);
        threadPoolSize.addCallback(() -> {
            try {
                threadPoolExecutor.setMaximumPoolSize(threadPoolSize.get());
            } catch (Exception e) {
                logger.warn("Update Thread Pool Size Failed.", e);
            }
        });
        threadPoolCoreSize.addCallback(() -> {
            try {
                threadPoolExecutor.setCorePoolSize(threadPoolCoreSize.get());
            } catch (Exception e) {
                logger.warn("Update Thread Pool Size Failed.", e);
            }
        });
        threadPoolKeepAlive.addCallback(() -> {
            try {
                threadPoolExecutor.setKeepAliveTime(threadPoolKeepAlive.get(), TimeUnit.SECONDS);
            } catch (Exception e) {
                logger.warn("Update Thread Pool KeepAlive Failed.", e);
            }
        });
    }

    public void add(SlbTaskWorker task) {
        logger.info("Add Slb Task. SlbId: " + task.getTargetSlbId());
        if (workerMap.containsKey(task.getTargetSlbId())) {
            threadPoolExecutor.remove(workerMap.get(task.getTargetSlbId()));
        }
        workerMap.put(task.getTargetSlbId(), task);
        threadPoolExecutor.scheduleWithFixedDelay(task, 0, 1, TimeUnit.SECONDS);
    }

    public void addIfNotExist(SlbTaskWorker task) {
        if (workerMap.containsKey(task.getTargetSlbId())) {
            return;
        }
        logger.info("Add Slb Task. SlbId: " + task.getTargetSlbId());
        workerMap.put(task.getTargetSlbId(), task);
        threadPoolExecutor.scheduleWithFixedDelay(task, 0, 1, TimeUnit.SECONDS);
    }

    public void remove(SlbTaskWorker task) {
        if (workerMap.containsKey(task.getTargetSlbId())) {
            threadPoolExecutor.remove(workerMap.get(task.getTargetSlbId()));
            workerMap.remove(task.getTargetSlbId());
        }
    }
}
