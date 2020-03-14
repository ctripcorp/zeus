package com.ctrip.zeus.service.rule.model;

public enum ConditionType {
    AND,
    OR,
    SELF;

    public String getName() {
        return name();
    }

    public static ConditionType getType(String type) {
        ConditionType result = null;

        if (type == null) return result;

        ConditionType[] constants = ConditionType.class.getEnumConstants();
        for (int i = 0; i < constants.length; i++) {
            if (type.equalsIgnoreCase(constants[i].getName())) {
                result = constants[i];
                break;
            }
        }
        return result;
    }
}
