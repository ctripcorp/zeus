package com.ctrip.zeus.util;

/**
 * Created by zhoumy on 2016/3/30.
 */
public class PathUtils {
    // 0 equivalent 1 higher priority 2 lower priority
    public static int prefixOverlapped(String path1, String path2, String stopFlag) {
        int i = 0;
        int idxPath1Suffix = path1.lastIndexOf(stopFlag);
        int idxPath2Suffix = path2.lastIndexOf(stopFlag);

        int len1 = idxPath1Suffix == -1 ? path1.length() : idxPath1Suffix;
        int len2 = idxPath2Suffix == -1 ? path2.length() : idxPath2Suffix;

        while (i < len1 && i < len2) {
            if (path1.charAt(i) == path2.charAt(i) || Character.toLowerCase(path1.charAt(i)) == Character.toLowerCase(path2.charAt(i))) {
                i++;
                continue;
            } else {
                return -1;
            }
        }
        if (len1 == len2) {
            return (idxPath1Suffix == idxPath2Suffix) ? 0 : (idxPath1Suffix > idxPath2Suffix ? 1 : 2);
        }
        return len1 < len2 ? 2 : 1;
    }
}