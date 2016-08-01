package com.ctrip.zeus.service.report.meta;

/**
 * Created by zhoumy on 2016/7/29.
 */
public enum ReportTopic {
    GROUP_CREATE(0),
    GROUP_UPDATE(1),
    GROUP_DELETE(2),
    VS_CREATE(3),
    VS_UPDATE(4),
    VS_DELETE(5),
    UNKNOWN(-1);

    private int value;

    ReportTopic(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ReportTopic get(int num) {
        switch (num) {
            case 0:
                return GROUP_CREATE;
            case 1:
                return GROUP_UPDATE;
            case 2:
                return GROUP_DELETE;
            case 3:
                return VS_CREATE;
            case 4:
                return VS_UPDATE;
            case 5:
                return VS_DELETE;
        }
        return UNKNOWN;
    }

    public static final boolean STATUS_CREATED = false;
    public static final boolean STATUS_COMSUMED = true;
}
