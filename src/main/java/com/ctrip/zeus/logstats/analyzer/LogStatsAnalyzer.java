package com.ctrip.zeus.logstats.analyzer;

import java.io.IOException;

/**
 * Created by mengyizhou on 10/18/15.
 */
public interface LogStatsAnalyzer {

    LogStatsAnalyzerConfig getConfig();

    String shoot() throws IOException;
}
