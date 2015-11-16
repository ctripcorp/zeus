package com.ctrip.zeus.logstats.tracker;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by zhoumy on 2015/11/13.
 */
public class AccessLogTracker implements LogTracker {
    private final LogTrackerStrategy strategy;
    private final String logFilename;
    private RandomAccessFile raf;

    public AccessLogTracker(LogTrackerStrategy strategy) {
        this.strategy = strategy;
        this.logFilename = strategy.getLogFilename();
    }

    @Override
    public String getName() {
        return "AccessLogTracker";
    }

    @Override
    public String getLogFilename() {
        return logFilename;
    }

    @Override
    public LogTrackerStrategy getStrategy() {
        return strategy;
    }

    @Override
    public void start() throws IOException {
        raf = new RandomAccessFile(getLogFilename(), "r");
    }

    @Override
    public void stop() throws IOException {
        if (raf != null)
            raf.close();
    }

    @Override
    public String move() throws IOException {
        return raf.readLine();
    }
}
