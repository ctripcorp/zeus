package com.ctrip.zeus.logstats.analyzer;

import com.ctrip.zeus.logstats.common.LineFormat;
import com.ctrip.zeus.logstats.tracker.LogTracker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mengyizhou on 10/18/15.
 */
public class LogStatsAnalyzerConfig {
    private final List<LineFormat> lineFormats;
    private String logFilename;
    private LogTracker logTracker;

    public LogStatsAnalyzerConfig() {
        lineFormats = new ArrayList<LineFormat>();
    }

    public List<LineFormat> getLineFormats() {
        return lineFormats;
    }

    public void addFormat(LineFormat lineFormat) {
        lineFormats.add(lineFormat);
    }

    public String getLogFilename() {
        return logFilename;
    }

    public void setLogFilename(String logFilename) {
        this.logFilename = logFilename;
    }

    public void setLogTracker(LogTracker logTracker) {
        this.logTracker = logTracker;
    }

    public LogTracker getLogTracker() {
        return logTracker;
    }

}
