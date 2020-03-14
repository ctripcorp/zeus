package com.ctrip.zeus.service.rule.util;

public class ParserUtils {
    public static Integer intValue(Object o) {
        try {
            if (o == null) return null;
            return Integer.parseInt(o.toString());
        } catch (Exception ne) {
            return null;
        }
    }

    public static Long longValue(Object object) {
        try {
            if (object != null) {
                return Long.parseLong(object.toString());
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static Boolean booleanValue(Object o) {
        try {
            if (o == null) return null;
            return Boolean.parseBoolean(o.toString());
        } catch (Exception ne) {
            return null;
        }
    }

    public static String stringValue(Object o) {
        if (o == null) return null;
        return o.toString();
    }
}
