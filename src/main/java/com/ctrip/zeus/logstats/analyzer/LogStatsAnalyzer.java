package com.ctrip.zeus.logstats.analyzer;

import com.ctrip.zeus.logstats.StatsDelegate;

import java.io.IOException;

/**
 * Created by mengyizhou on 10/18/15.
 */
public interface LogStatsAnalyzer {

    LogStatsAnalyzerConfig getConfig();

    void start() throws IOException;

    void stop() throws IOException;

    String analyze() throws IOException;

    void analyze(StatsDelegate<String> delegate) throws IOException;
}
