package com.ctrip.zeus.util;

public class StringUtils {

    public static String JSON = "%#.3s";

    public static boolean equals(String str1, String str2) {
        return str1 != null ? str1.equals(str2) : str2 == null;
    }

    public static boolean equalsIgnoreCase(String str1, String str2) {
        return str1 != null ? str1.equalsIgnoreCase(str2) : str2 == null;
    }
}
