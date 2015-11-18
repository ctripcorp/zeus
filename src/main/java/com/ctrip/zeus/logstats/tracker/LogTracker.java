package com.ctrip.zeus.logstats.tracker;

import com.ctrip.zeus.logstats.StatsDelegate;

import java.io.IOException;

/**
 * Created by mengyizhou on 10/18/15.
 */
public interface LogTracker {

    String getName();

    String getLogFilename();

    LogTrackerStrategy getStrategy();

    void start() throws IOException;

    void stop() throws IOException;

    String move() throws IOException;

    void fastMove(StatsDelegate<String> delegate) throws IOException;
}