package com.ctrip.zeus.service.rule.model;

public enum ConditionTargetType {
    HEADER,
    COOKIE,
    ARG,
    CIP,
    URL,
    URI,
    COUNTCOOKIE,
    COUNTHEADER,
    MERGE,
    METHOD;

    public String getName() {
        return name();
    }

    public static ConditionTargetType getTarget(String target) {
        if (target == null) return null;
        ConditionTargetType[] constants = ConditionTargetType.class.getEnumConstants();
        for (ConditionTargetType constant : constants) {
            if (constant.getName().equalsIgnoreCase(target)) {
                return constant;
            }
        }
        return null;
    }
}
