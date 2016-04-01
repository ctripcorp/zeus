package com.ctrip.zeus.util;

/**
 * Created by zhoumy on 2016/3/30.
 */
public class StringUtils {
    public static boolean prefixOverlapped(String string1, String string2) {
        int i = 0;
        while (i < string1.length() && i < string2.length()) {
            if (string1.charAt(i) == string2.charAt(i) || Character.toLowerCase(string1.charAt(i)) == Character.toLowerCase(string2.charAt(i))) {
                i++;
                continue;
            } else return false;
        }
        return true;
    }
}