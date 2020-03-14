package com.ctrip.zeus.service.rule.model;

import org.apache.commons.lang.StringUtils;

public class ConditionTarget {
    private ConditionTargetType type;
    private String key;

    public ConditionTarget(ConditionTargetType type, String key) {
        this.type = type;
        this.key = key;
    }

    public static ConditionTarget parserFromTarget(String target) {
        if (target == null) return null;
        if (target.startsWith("$")) {
            target = target.substring(1);
        }
        if (target.indexOf("(") > 0) {
            int first = target.indexOf("(");
            int last = target.lastIndexOf(")");
            if (first > 0 && last > first) {
                ConditionTargetType type = ConditionTargetType.getTarget(target.substring(0, first));
                if (type == null) {
                    return null;
                }
                return new ConditionTarget(type, target.substring(first + 1, last));

            } else {
                return null;
            }
        }
        

        String[] targetArray = StringUtils.split(target, "_");
        if (targetArray.length >= 1) {
            ConditionTargetType type = ConditionTargetType.getTarget(targetArray[0]);
            if (type == null) {
                return null;
            }

            return new ConditionTarget(type, targetArray.length > 1 ? target.substring(targetArray[0].length() + 1) : null);
        } else {
            return null;
        }
    }

    public ConditionTargetType getType() {
        return type;
    }

    public ConditionTarget setType(ConditionTargetType type) {
        this.type = type;
        return this;
    }

    public String getKey() {
        return key;
    }

    public ConditionTarget setKey(String key) {
        this.key = key;
        return this;
    }
}
