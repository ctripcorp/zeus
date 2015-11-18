package com.ctrip.zeus.logstats;

import com.ctrip.zeus.logstats.analyzer.AccessLogStatsAnalyzer;
import com.ctrip.zeus.logstats.analyzer.LogStatsAnalyzer;

import java.io.IOException;

/**
 * Created by zhoumy on 2015/11/17.
 */
public class LogStatsReportWorker implements LogStatsWorker {
    private static final int SwitchInterval = 60 * 1000;
    private final LogStatsAnalyzer analyzer;
    private final StatsDelegate reporter;
    private int nextStartTime;

    public LogStatsReportWorker() throws IOException {
        analyzer = new AccessLogStatsAnalyzer();
        reporter = new StatsDelegate<String>() {
            @Override
            public void delegate(String input) {
            }
        };
    }

    @Override
    public void doJob() {
        if (nextStartTime + SwitchInterval < System.currentTimeMillis()) {
            try {
                analyzer.analyze(reporter);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        nextStartTime += SwitchInterval;
    }
}
