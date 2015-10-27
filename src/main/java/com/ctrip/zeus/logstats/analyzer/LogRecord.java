package com.ctrip.zeus.logstats.analyzer;

/**
 * Created by mengyizhou on 10/18/15.
 */

/**
 * Get a single record. Data is returned by json string whose keys are defined by $ in LogFormat.
 */
public class LogRecord {
    private String value;
    private LogRecord next;

    public LogRecord(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public LogRecord next() {
        LogRecord retained = next;
        // GC
        next = null;
        value = null;
        return retained;
    }

    public void setNext(LogRecord next) {
        this.next = next;
    }
}
