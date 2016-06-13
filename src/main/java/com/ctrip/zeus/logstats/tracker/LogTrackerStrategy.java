package com.ctrip.zeus.logstats.tracker;

/**
 * Created by mengyizhou on 10/18/15.
 */
public class LogTrackerStrategy {
    public static final int START_FROM_HEAD = 0;
    public static final int START_FROM_CURRENT = 1;

    private boolean allowLogRotate;
    private boolean allowTrackerMemo;
    private int startMode = START_FROM_CURRENT;
    private String logRotateMode;
    private String trackerMemoFilename;
    private String logFilename;
    private boolean doAsRoot;
    private int readSize;

    public boolean isAllowLogRotate() {
        return allowLogRotate;
    }

    public String getLogRotateMode() {
        return logRotateMode;
    }

    public LogTrackerStrategy setAllowLogRotate(boolean allowLogRotate, String logRotateMode) {
        this.allowLogRotate = allowLogRotate;
        this.logRotateMode = logRotateMode;
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

    public int getReadSize() {
        return readSize;
    }

    public LogTrackerStrategy setReadSize(int readSize) {
        this.readSize = readSize;
        return this;
    }

    public int getStartMode() {
        return startMode;
    }

    public LogTrackerStrategy setStartMode(int startMode) {
        this.startMode = startMode;
        return this;
    }
}
