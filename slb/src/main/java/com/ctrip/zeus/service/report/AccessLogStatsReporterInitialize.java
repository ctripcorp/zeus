package com.ctrip.zeus.service.report;

import com.ctrip.zeus.task.log.AccessLogStatsReporter;

public interface AccessLogStatsReporterInitialize {
    void init(AccessLogStatsReporter reporter);
}
