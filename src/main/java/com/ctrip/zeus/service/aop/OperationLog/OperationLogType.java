package com.ctrip.zeus.service.aop.OperationLog;

/**
 * Created by fanqq on 2015/7/16.
 */
public enum OperationLogType {
    SLB,
    GROUP,
    VS,
    SERVER;
    public String value() {
        return name();
    }
    public static OperationLogType fromValue(String v) {
        return valueOf(v);
    }
}
