package com.ctrip.zeus.service.report.meta;

/**
 * Created by zhoumy on 2015/7/10.
 */
public enum ReportStatus {
    DELETION_ERROR(-2, "Deletion error."),
    SYNC_ERROR(-1, "Sync Error"),
    WAITING(0, "Waiting to be synced."),
    SUCCESS(1, "Success."),
    DELETED(2, "Deleted."),
    WAITING_HUNG(5, "Waiting hung."),
    DELETED_HUNG(6, "Deleted hung."),
    ERROR(9, "Unknown Error.");

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
            case -2:
                return DELETION_ERROR;
            case -1:
                return SYNC_ERROR;
            case 0:
                return WAITING;
            case 1:
                return SUCCESS;
            case 2:
                return DELETED;
            case 5:
                return WAITING_HUNG;
            case 6:
                return DELETED_HUNG;
            case 9:
                return ERROR;
        }
        return ERROR;
    }
}
