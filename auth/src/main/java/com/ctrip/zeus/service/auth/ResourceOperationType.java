package com.ctrip.zeus.service.auth;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by fanqq on 2016/7/22.
 */
public enum ResourceOperationType {
    READ,
    UPDATE,
    DELETE,
    ACTIVATE,
    DEACTIVATE,
    OP_PULL,
    OP_HEALTH_CHECK,
    OP_MEMBER,
    OP_SERVER,
    PROPERTY,
    CERT,
    SYNC,
    WAF,
    ADMIN_INFO,
    MAINTENANCE,
    FORCE,
    NEW,
    FLOW,
    BLACK_LIST,
    AUTH;

    public static EnumSet<ResourceOperationType> getStatusOperationSet() {
        return STATUS_OPERATION_SET;
    }

    private static EnumSet<ResourceOperationType> STATUS_OPERATION_SET = EnumSet.of(OP_MEMBER, OP_SERVER, OP_HEALTH_CHECK, OP_PULL);

    private static Set<String> names = new HashSet<>();

    static {
        for (ResourceOperationType r : values()) {
            names.add(r.getType());
        }
    }

    public String getType() {
        return name();
    }

    ResourceOperationType getByName(String name) {
        return valueOf(name);
    }

    static public boolean contain(String name) {
        return names.contains(name);
    }

    static public String getNames() {
        return names.toString();
    }
}
