package com.ctrip.zeus.service.report.stats;

import com.ctrip.zeus.service.messaging.MessagingService;
import com.ctrip.zeus.service.tools.local.LocalInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service("dataCollectorFactory")
public class DataCollectorFactory {

    private static final Logger logger = LoggerFactory.getLogger(DataCollectorFactory.class);

    private static final Map<String, DataCollector> instances = new HashMap<>();
    private static final Lock lock = new ReentrantLock();

    @Autowired(required = false)
    private MessagingService messagingService;
    @Resource
    private LocalInfoService localInfoService;

    public DataCollector getInstance(Class<? extends DataCollector> clazz) {
        String name = clazz.getCanonicalName();
        final DataCollector instance = instances.get(name);
        if (instance != null) {
            return instance;
        }
        lock.lock();
        try {
            final DataCollector collector = clazz.newInstance();
            if (collector instanceof MessagingServiceRequired && messagingService != null) {
                ((MessagingServiceRequired) collector).setMessagingService(messagingService);
            }
            if (collector instanceof LocalInfoServiceRequired) {
                ((LocalInfoServiceRequired) collector).setLocalInfoService(localInfoService);
            }
            instances.put(name, collector);
            return collector;
        } catch (Exception ex) {
            logger.error("Try to create DataCollector with type " + clazz.getSimpleName() + " failed.", ex);
            return null;
        } finally {
            lock.unlock();
        }
    }
}
