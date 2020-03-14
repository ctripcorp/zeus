package com.ctrip.zeus.service.rule.model;

public enum ConditionActionType {
    REJECT,
    FLAG,
    REDIRECT,
    PROXYPASS;

    public String getName() {
        return name();
    }

    public static ConditionActionType getAction(String action) {
        ConditionActionType result = null;
        if (action == null) return result;
        ConditionActionType[] constants = ConditionActionType.class.getEnumConstants();
        for (int i = 0; i < constants.length; i++) {
            if (action.equalsIgnoreCase(constants[i].getName())) {
                result = constants[i];
                break;
            }
        }
        return result;
    }
}
