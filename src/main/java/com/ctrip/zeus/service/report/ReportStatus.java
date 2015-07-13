package com.ctrip.zeus.service.report;

/**
 * Created by zhoumy on 2015/7/10.
 */
public enum ReportStatus {
    WAITING(0, "Waiting to be synced."),
    SUCCESS(1, "Success."),
    ERROR(-1, "Error");

    private int value;
    private String description;

    private ReportStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public int getValue() {
        return value;
    }

    public static ReportStatus getReportStatus(int value) {
        switch (value) {
            case -1: return ERROR;
            case 0: return WAITING;
            case 1: return SUCCESS;
        }
        return ERROR;
    }
}
