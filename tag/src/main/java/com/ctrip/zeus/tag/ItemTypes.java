package com.ctrip.zeus.tag;

import com.ctrip.zeus.exceptions.ValidationException;

public final class ItemTypes {

    private ItemTypes() {
    }

    public static final String SLB = "slb";

    public static final String VS = "vs";

    public static final String GROUP = "group";

    public static final String POLICY = "policy";

    public static String parse(String type) throws Exception {
        if (SLB.equalsIgnoreCase(type)) {
            return SLB;
        } else if (VS.equalsIgnoreCase(type)) {
            return VS;
        } else if (GROUP.equalsIgnoreCase(type)) {
            return GROUP;
        } else if (POLICY.equalsIgnoreCase(type)) {
            return POLICY;
        }
        throw new ValidationException("Invalid item type: " + type);
    }
}
