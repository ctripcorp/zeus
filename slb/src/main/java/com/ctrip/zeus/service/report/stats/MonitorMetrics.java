package com.ctrip.zeus.service.report.stats;

import com.ctrip.zeus.logstats.tools.ReqStats;
import com.ctrip.zeus.logstats.tools.StatsKey;
import com.ctrip.zeus.service.traffic.TrafficMonitorService;
import com.ctrip.zeus.util.S;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by fanqq on 2019/09/27.
 */
@Component
public class MonitorMetrics implements ReportMetrics {

    private static DynamicBooleanProperty enableAll = DynamicPropertyFactory.getInstance().getBooleanProperty("monitor.req.enable.all", false);
    private static DynamicPropertyFactory factory = DynamicPropertyFactory.getInstance();
    private String localIp = null;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private TrafficMonitorService trafficMonitorService;

    public MonitorMetrics() {
        localIp = S.getIp();
    }

    public MonitorMetrics setTrafficMonitorService(TrafficMonitorService service) {
        trafficMonitorService = service;
        return this;
    }


    @Override
    public void reportMetrics(ConcurrentMap<StatsKey, ReqStats> statsKeyStatsConcurrentMap, Date reportTime) {
        if (localIp == null) {
            localIp = S.getIp();
        }
        if (trafficMonitorService == null) {
            return;
        }
        if (factory.getBooleanProperty("monitor.req.enable.ip." + localIp, true).get() || enableAll.get()) {
            try {
                trafficMonitorService.refresh(statsKeyStatsConcurrentMap);
            } catch (Exception e) {
                logger.warn("Refresh Failed.", e);
            }
        }
    }

}
