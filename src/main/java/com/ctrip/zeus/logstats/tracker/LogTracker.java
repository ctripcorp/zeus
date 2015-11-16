package com.ctrip.zeus.logstats.tracker;

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
}