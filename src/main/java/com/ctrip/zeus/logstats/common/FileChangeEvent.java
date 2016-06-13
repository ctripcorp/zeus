package com.ctrip.zeus.logstats.common;

/**
 * Created by zhoumy on 2016/6/13.
 */
public class FileChangeEvent {
    private String event;
    private String filename;

    public FileChangeEvent(String event, String filename) {
        this.event = event;
        this.filename = filename;
    }

    public String getEvent() {
        return event;
    }

    public String getFilename() {
        return filename;
    }
}
