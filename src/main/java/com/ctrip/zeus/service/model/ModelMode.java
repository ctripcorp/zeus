package com.ctrip.zeus.service.model;

/**
 * Created by zhoumy on 2015/12/21.
 */
public enum ModelMode {
    MODEL_MODE_ONLINE, MODEL_MODE_OFFLINE,
    MODEL_MODE_MERGE_OFFLINE, MODEL_MODE_MERGE_ONLINE,
    MODEL_MODE_REDUNDANT;

    public static ModelMode getMode(String value) {
        if (value == null)
            return MODEL_MODE_MERGE_OFFLINE;
        if ("ONLINE".equals(value.toUpperCase())) return MODEL_MODE_ONLINE;
        if ("OFFLINE".equals(value.toUpperCase())) return MODEL_MODE_OFFLINE;
        if ("REDUNDANT".equals(value.toUpperCase())) return MODEL_MODE_REDUNDANT;
        if ("MERGE".equals(value.toUpperCase())) return MODEL_MODE_MERGE_OFFLINE;
        return MODEL_MODE_MERGE_OFFLINE;
    }
}