package com.ctrip.zeus.domain;

public enum RouteRuleType {

    UNKNOWN(""),
    HEADER("header");

    private String value;

    RouteRuleType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RouteRuleType getType(String type) {
        if (RouteRuleType.HEADER.value.equalsIgnoreCase(type)) {
            return RouteRuleType.HEADER;
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return value;
    }
}
