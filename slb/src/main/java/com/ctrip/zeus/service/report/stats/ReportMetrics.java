package com.ctrip.zeus.service.report.stats;

import com.ctrip.zeus.logstats.tools.ReqStats;
import com.ctrip.zeus.logstats.tools.StatsKey;

import java.util.Date;
import java.util.concurrent.ConcurrentMap;

public interface ReportMetrics {
    void reportMetrics(ConcurrentMap<StatsKey, ReqStats> statsKeyStatsConcurrentMap, Date reportTime);
}
