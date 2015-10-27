package com.ctrip.zeus.logstats.tracker;

/**
 * Created by mengyizhou on 10/18/15.
 */
public class LogTrackerStrategy {
    private boolean allowLogRotate;
    private boolean allowTrackerMemo;
    private String trackerMemoFilename;
    private boolean doAsRoot;

    public boolean isAllowLogRotate() {
        return allowLogRotate;
    }

    public void setAllowLogRotate(boolean allowLogRotate) {
        this.allowLogRotate = allowLogRotate;
    }

    public boolean isAllowTrackerMemo() {
        return allowTrackerMemo;
    }

    public void setAllowTrackerMemo(boolean allowTrackerMemo) {
        this.allowTrackerMemo = allowTrackerMemo;
    }

    public String getTrackerMemoFilename() {
        return trackerMemoFilename;
    }

    public void setTrackerMemoFilename(String trackerMemoFilename) {
        this.trackerMemoFilename = trackerMemoFilename;
    }

    public boolean isDoAsRoot() {
        return doAsRoot;
    }

    public void setDoAsRoot(boolean doAsRoot) {
        this.doAsRoot = doAsRoot;
    }
}
