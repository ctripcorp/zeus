package com.ctrip.zeus.domain;

public enum RouteRuleOp {

    UNKNOWN(""),
    REGEX("regex");

    private String value;

    RouteRuleOp(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RouteRuleOp getType(String type) {
        if (RouteRuleOp.REGEX.value.equalsIgnoreCase(type)) {
            return RouteRuleOp.REGEX;
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return value;
    }
}
