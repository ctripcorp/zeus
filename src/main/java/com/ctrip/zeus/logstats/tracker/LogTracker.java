package com.ctrip.zeus.logstats.tracker;

/**
 * Created by mengyizhou on 10/18/15.
 */
public interface LogTracker {

    String getName();

    String getLogFilename();

    LogTrackerStrategy getStrategy();

    void start();

    void stop();

    String move();
}