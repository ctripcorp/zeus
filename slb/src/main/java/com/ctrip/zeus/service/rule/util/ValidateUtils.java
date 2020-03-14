package com.ctrip.zeus.service.rule.util;

import com.ctrip.zeus.exceptions.ValidationException;

import java.util.ArrayList;

public class ValidateUtils {
    public static void isIntValue(Object o, String errorMessage) throws ValidationException {
        try {
            if (o == null) return;
            Integer.parseInt(o.toString().trim());
        } catch (Exception ne) {
            throw new ValidationException(errorMessage);
        }
    }

    public static void isLongValue(Object o, String errorMessage) throws ValidationException {
        try {
            if (o == null) return;
            Long.parseLong(o.toString().trim());
        } catch (Exception ne) {
            throw new ValidationException(errorMessage);
        }
    }

    public static void isBooleanValue(Object o, String errorMessage) throws ValidationException {
        if (o == null) return;
        String s = o.toString();
        if (!s.equalsIgnoreCase("true") && !s.equalsIgnoreCase("false")) {
            throw new ValidationException(errorMessage);
        }
    }

    public static void notNullAndEmpty(Object o, String errorMessage) throws ValidationException {
        if (o == null) {
            throw new ValidationException(errorMessage);
        }
        if (o instanceof String) {
            notNullString((String) o, errorMessage);
        }
        if(o instanceof ArrayList){
            notNullList((ArrayList)o, errorMessage);
        }
    }

    private static void notNullString(String o, String errorMessage) throws ValidationException {
        if (o.isEmpty()) {
            throw new ValidationException(errorMessage);
        }
    }

    private static void notNullList(ArrayList o, String errorMessage) throws ValidationException {
        if (o.isEmpty()) {
            throw new ValidationException(errorMessage);
        }
    }
}
