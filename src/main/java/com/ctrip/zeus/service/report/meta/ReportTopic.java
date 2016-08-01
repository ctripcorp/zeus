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
    VS_DELETE(5);

    private int value;

    ReportTopic(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static final boolean STATUS_CREATED = false;
    public static final boolean STATUS_COMSUMED = true;
}
