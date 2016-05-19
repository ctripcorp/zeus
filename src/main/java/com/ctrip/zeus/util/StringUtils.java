package com.ctrip.zeus.util;

/**
 * Created by zhoumy on 2016/3/30.
 */
public class StringUtils {
    public static int prefixOverlapped(String string1, String string2) {
        int i = 0;
        int len1 = string1.length();
        int len2 = string2.length();
        while (i < string1.length() && i < string2.length()) {
            if (string1.charAt(i) == string2.charAt(i) || Character.toLowerCase(string1.charAt(i)) == Character.toLowerCase(string2.charAt(i))) {
                i++;
                continue;
            } else {
                return -1;
            }
        }
        return len1 == len2 ? 0 : (len1 < len2 ? 2 : 1);
    }
}