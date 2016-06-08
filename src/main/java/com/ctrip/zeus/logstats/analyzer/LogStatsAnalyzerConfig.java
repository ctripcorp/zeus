package com.ctrip.zeus.logstats.analyzer;

import com.ctrip.zeus.logstats.StatsDelegate;
import com.ctrip.zeus.logstats.common.LineFormat;
import com.ctrip.zeus.logstats.tracker.LogTracker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mengyizhou on 10/18/15.
 */
public class LogStatsAnalyzerConfig {
    private final List<LineFormat> lineFormats;
    private final List<StatsDelegate> logStatsDelegators;
    private LogTracker logTracker;
    private int numberOfConsumers;

    public LogStatsAnalyzerConfig() {
        this(new ArrayList<StatsDelegate>());
    }

    public LogStatsAnalyzerConfig(List<StatsDelegate> logStatsDelegators) {
        this.lineFormats = new ArrayList<>();
        this.logStatsDelegators = logStatsDelegators;
    }

    public List<LineFormat> getLineFormats() {
        return lineFormats;
    }

    public LogStatsAnalyzerConfig addFormat(LineFormat lineFormat) {
        lineFormats.add(lineFormat);
        return this;
    }

    public LogStatsAnalyzerConfig setLogTracker(LogTracker logTracker) {
        this.logTracker = logTracker;
        return this;
    }

    public LogStatsAnalyzerConfig setNumberOfConsumers(int count) {
        numberOfConsumers = count;
        return this;
    }

    public LogTracker getLogTracker() {
        return logTracker;
    }

    public boolean allowDelegate() {
        return logStatsDelegators.size() > 0;
    }

    public List<StatsDelegate> getDelegators() {
        return logStatsDelegators;
    }

    public int getNumberOfConsumers() {
        return numberOfConsumers;
    }
}
