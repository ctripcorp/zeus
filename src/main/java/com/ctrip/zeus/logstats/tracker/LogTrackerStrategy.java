package com.ctrip.zeus.logstats.tracker;

/**
 * Created by mengyizhou on 10/18/15.
 */
public class LogTrackerStrategy {
    private boolean allowLogRotate;
    private boolean allowTrackerMemo;
    private String trackerMemoFilename;
    private String logFilename;
    private boolean doAsRoot;

    public boolean isAllowLogRotate() {
        return allowLogRotate;
    }

    public LogTrackerStrategy setAllowLogRotate(boolean allowLogRotate) {
        this.allowLogRotate = allowLogRotate;
        return this;
    }

    public boolean isAllowTrackerMemo() {
        return allowTrackerMemo;
    }

    public LogTrackerStrategy setAllowTrackerMemo(boolean allowTrackerMemo) {
        this.allowTrackerMemo = allowTrackerMemo;
        return this;
    }

    public String getTrackerMemoFilename() {
        return trackerMemoFilename;
    }

    public LogTrackerStrategy setTrackerMemoFilename(String trackerMemoFilename) {
        this.trackerMemoFilename = trackerMemoFilename;
        return this;
    }

    public String getLogFilename() {
        return logFilename;
    }

    public LogTrackerStrategy setLogFilename(String logFilename) {
        this.logFilename = logFilename;
        return this;
    }

    public boolean isDoAsRoot() {
        return doAsRoot;
    }

    public LogTrackerStrategy setDoAsRoot(boolean doAsRoot) {
        this.doAsRoot = doAsRoot;
        return this;
    }
}
