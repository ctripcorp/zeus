package com.ctrip.zeus.logstats.analyzer;

/**
 * Created by mengyizhou on 10/18/15.
 */
public interface LogStatsAnalyzer {

    LogStatsAnalyzerConfig getConfig();

    LogRecord shoot();
}
