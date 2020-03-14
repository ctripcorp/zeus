package com.ctrip.zeus.logstats.tracker;

/**
 * Created by mengyizhou on 10/18/15.
 */
public class LogTrackerStrategy {
    public static final int START_FROM_HEAD = 0;
    public static final int START_FROM_CURRENT = 1;

    public static final String LOGROTATE_COPYTRUNCATE = "copytruncate";
    public static final String LOGROTATE_RENAME = "rename";

    private String logFilename;
    private int readBufferSize;

    private int startMode = START_FROM_CURRENT;

    private boolean allowLogRotate;
    private String logRotateMode;
    private boolean dropOnFileChange;
    private boolean reopenAfterLogRotate;

    private boolean allowTrackerMemo;
    private String trackerMemoFilename;

    private boolean doAsRoot;


    public boolean isAllowLogRotate() {
        return allowLogRotate;
    }

    public String getLogRotateMode() {
        return logRotateMode;
    }

    public boolean isDropOnFileChange() {
        return dropOnFileChange;
    }

    public boolean isReopenAfterLogRotate() {
        return reopenAfterLogRotate;
    }

    public LogTrackerStrategy isDropOnFileChange(boolean dropOnFileChange) {
        this.dropOnFileChange = dropOnFileChange;
        return this;
    }

    public LogTrackerStrategy setAllowLogRotate(boolean allowLogRotate, String logRotateMode) {
        this.allowLogRotate = allowLogRotate;
        this.logRotateMode = logRotateMode;

        switch (logRotateMode) {
            case LOGROTATE_COPYTRUNCATE:
                dropOnFileChange = true;
                reopenAfterLogRotate = false;
                break;
            case LOGROTATE_RENAME:
                dropOnFileChange = false;
                reopenAfterLogRotate = true;
                break;
        }

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

    public int getReadBufferSize() {
        return readBufferSize;
    }

    public LogTrackerStrategy setReadBufferSize(int readBufferSize) {
        this.readBufferSize = readBufferSize;
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
