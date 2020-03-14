package com.ctrip.zeus.service.model.common;

import com.ctrip.zeus.exceptions.ValidationException;

public enum RuleTargetType {
    GROUP(1, true),
    VS(2, true),
    SLB(3, true),
    TRAFFIC_POLICY(4, true),
    APP(5, false),
    BU(6, false),
    DEFAULT(7, false);

    final int id;
    final boolean needTargetBoolean;

    RuleTargetType(int id, boolean needTargetBoolean) {
        this.id = id;
        this.needTargetBoolean = needTargetBoolean;
    }

    public String getName() {
        return name();
    }

    public int getId() {
        return id;
    }

    public boolean isNeedTarget() {
        return this.needTargetBoolean;
    }

    public static RuleTargetType getTargetType(int i) {
        RuleTargetType result = null;

        if (i > 0) {
            RuleTargetType[] constants = RuleTargetType.class.getEnumConstants();
            for (int j = 0; j < constants.length; j++) {
                if (i == constants[j].getId()) {
                    result = constants[j];
                    break;
                }
            }
        }
        return result;
    }

    public static RuleTargetType getTargetType(String type) {
        RuleTargetType result = null;

        if (type == null) return result;

        RuleTargetType[] constants = RuleTargetType.class.getEnumConstants();
        for (int i = 0; i < constants.length; i++) {
            if (type.equalsIgnoreCase(constants[i].getName())) {
                result = constants[i];
                break;
            }
        }
        return result;
    }

    public static Long parseLongTargetId(String target) throws ValidationException {
        try {
            return Long.parseLong(target);
        } catch (NumberFormatException ne) {
            throw new ValidationException("Target id is required to be long formatted");
        }
    }
}
