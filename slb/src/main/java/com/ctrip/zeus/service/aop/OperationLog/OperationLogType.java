package com.ctrip.zeus.service.aop.OperationLog;

/**
 * Created by fanqq on 2015/7/16.
 */
public enum OperationLogType {
    SLB,
    GROUP,
    POLICY,
    DR,
    VS,
    AUTH,
    SERVER,
    FLOW_SLB_CREATING,
    FLOW_SLB_SHARDING,
    FLOW_SLB_DESTROY,
    FLOW_VS_SPLIT,
    FLOW_VS_MERGE,
    SANDBOX;
    public String value() {
        return name();
    }
    public static OperationLogType fromValue(String v) {
        return valueOf(v);
    }
}
