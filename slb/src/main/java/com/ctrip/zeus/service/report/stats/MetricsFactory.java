package com.ctrip.zeus.service.report.stats;

import com.ctrip.zeus.logstats.tools.LongSlots;
import com.ctrip.zeus.logstats.tools.ReqStatsCollector;
import com.ctrip.zeus.service.messaging.MessagingService;
import com.ctrip.zeus.service.tools.local.LocalInfoService;
import com.google.common.collect.ImmutableMap;
import com.netflix.config.DynamicLongProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by zhoumy on 2015/12/7.
 */
@Component("metricsFactory")
public class MetricsFactory {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private static final DynamicLongProperty ReportInterval = DynamicPropertyFactory.getInstance().getLongProperty("metrics.reporter.interval", 60 * 1000);

    private static final Map<String, ReqStatsCollector> metricsCollectorMap = new HashMap<>();
    private static final Lock l = new ReentrantLock();
    private final LongSlots timeSlots;
    private final LongSlots sizeSlots;
    private final LongSlots diffSlots;
    private final ImmutableMap<String, String> keyMapping;

    @Autowired(required = false)
    private MessagingService messagingService;
    @Resource
    private LocalInfoService localInfoService;
    
    public MetricsFactory() {
        timeSlots = new LongSlots(">100s")
                .slot("0~10ms", 10l)
                .slot("10~50ms", 50l)
                .slot("50~100ms", 100l)
                .slot("100~200ms", 200l)
                .slot("200~500ms", 500l)
                .slot("500ms~1s", 1000l)
                .slot("1~5s", 5000l)
                .slot("5~10s", 10000l)
                .slot("10~20s", 20000l)
                .slot("20~30s", 30000l)
                .slot("30~50s", 50000l)
                .slot("50~100s", 100000l);
        diffSlots = new LongSlots(">50s")
                .slot("0ms", 0L)
                .slot("1~5ms", 5l)
                .slot("5~10ms", 10l)
                .slot("10~30ms", 30l)
                .slot("30~60ms", 60l)
                .slot("60~120ms", 120l)
                .slot("120~300ms", 300l)
                .slot("300ms~500ms", 500l)
                .slot("500ms~1s", 1000l)
                .slot("1~5s", 5000l)
                .slot("5~10s", 10000l)
                .slot("10~20s", 20000l)
                .slot("20~30s", 30000l)
                .slot("30~50s", 50000l);
        sizeSlots = new LongSlots(">30M")
                .slot("0~1K", 1024 * 1l)
                .slot("1~3K", 1024 * 3l)
                .slot("3~5K", 1024 * 5l)
                .slot("5~10K", 1024 * 10l)
                .slot("10~50K", 1024 * 50l)
                .slot("50~100K", 1024 * 100l)
                .slot("100~200K", 1024 * 200l)
                .slot("200~500K", 1024 * 500l)
                .slot("500k~1M", 1024 * 1024l)
                .slot("1~5M", 1024 * 1024 * 5l)
                .slot("5~10M", 1024 * 1024 * 10l)
                .slot("10~20M", 1024 * 1024 * 20l)
                .slot("20~30M", 1024 * 1024 * 30l);
        keyMapping = ImmutableMap.<String, String>builder()
                .put("host", "domain")
                .put("method", "method")
                .put("upstream_name", "group_id")
                .put("server_port", "port")
                .put("server_protocol", "protocol")
                .put("request_time", "cost")
                .put("upstream_addr", "group_server")
                .put("upstream_response_time", "group_server_cost")
                .put("upstream_status", "group_server_status").build();
    }

    public ReqStatsCollector getDefault() {
        return createIfAbsent("default", ReqStatsCollector.class);
    }

    public ReqStatsCollector getInstance(String name) {
        return metricsCollectorMap.get(name);
    }

    public ReqStatsCollector createIfAbsent(String name, Class<? extends ReqStatsCollector> clazz) {
        final ReqStatsCollector instance = metricsCollectorMap.get(name);
        if (instance != null) {
            return instance;
        }
        l.lock();
        try {
            final ReqStatsCollector c = clazz.getConstructor(LongSlots.class, LongSlots.class, LongSlots.class, long.class)
                    .newInstance(getSizeSlots(), getSizeSlots(), getTimeSlots(), ReportInterval.get());
            if (c instanceof MessagingServiceRequired && messagingService != null) {
                ((MessagingServiceRequired)c).setMessagingService(messagingService);
            }
            if (c instanceof LocalInfoServiceRequired) {
                ((LocalInfoServiceRequired)c).setLocalInfoService(localInfoService);
            }
            ReportInterval.addCallback(new Runnable() {
                @Override
                public void run() {
                    c.setReportInterval(ReportInterval.get());
                }
            });
            metricsCollectorMap.put(name, c);
            return c;
        } catch (Exception ex) {
            logger.error("Try create ReqStatsCollector with name " + name + " and type " + clazz.getSimpleName() + " failed.", ex);
            return null;
        } finally {
            l.unlock();
        }
    }

    public ReqStatsCollector setIfAbsent(String name, final ReqStatsCollector c) throws Exception {
        ReqStatsCollector instance = metricsCollectorMap.get(name);
        if (instance != null) {
            throw new Exception("Existing instance with name " + name + ". Extra deamon thread is running.");
        }
        l.lock();
        try {
            c.setReportInterval(ReportInterval.get());
            ReportInterval.addCallback(new Runnable() {
                @Override
                public void run() {
                    c.setReportInterval(ReportInterval.get());
                }
            });
            return metricsCollectorMap.put(name, c);
        } finally {
            l.unlock();
        }
    }

    public LongSlots getTimeSlots() {
        return timeSlots;
    }

    public LongSlots getDiffSlots() {
        return diffSlots;
    }

    public LongSlots getSizeSlots() {
        return sizeSlots;
    }

    public String getKey(String key) {
        String result = keyMapping.get(key);
        return result == null ? key : result;
    }
}