package com.ctrip.zeus.service.model;

/**
 * Created by zhoumy on 2015/12/21.
 */
public enum SelectionMode {
    ONLINE_EXCLUSIVE, OFFLINE_EXCLUSIVE,
    OFFLINE_FIRST, ONLINE_FIRST,
    REDUNDANT;

    public static SelectionMode getMode(String value) {
        if (value == null)
            return OFFLINE_FIRST;
        if ("ONLINE".equals(value.toUpperCase())) return ONLINE_EXCLUSIVE;
        if ("OFFLINE".equals(value.toUpperCase())) return OFFLINE_EXCLUSIVE;
        if ("REDUNDANT".equals(value.toUpperCase())) return REDUNDANT;
        if ("MERGE".equals(value.toUpperCase())) return OFFLINE_FIRST;
        return OFFLINE_FIRST;
    }
}