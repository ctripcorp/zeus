package com.ctrip.zeus.service.report;

import com.ctrip.zeus.task.log.AccessLogStatsReporter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Service
public abstract class MetricsReporterAbstract implements MetricsReporter {
    @Resource
    private AccessLogStatsReporter accessLogStatsReporter;

    @PostConstruct
    public void init() {
        accessLogStatsReporter.registerReporter(this);
    }
}
