package com.ctrip.zeus.service.model.grammar;

import com.ctrip.zeus.exceptions.ValidationException;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by zhoumy on 2016/3/30.
 */
public class PathUtils {
    private static final Set<String> pathPrefixModifier = Sets.newHashSet("=", "~", "~*", "^~");

    // 0 equivalent 1 higher priority 2 lower priority
    @Deprecated
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


    /**
     * @param path1
     * @param path2
     * @return the value -1 if path1 and path2 are logically non relevant;
     * the value 0 if path1 and path2 is lexicographically equivalent;
     * the value 1 if path1 overlaps path2;
     * the value 2 if path1 is overlapped by path2.
     */
    public static int prefixOverlaps(String path1, String path2) {
        int i = path1.startsWith("/") ? 1 : 0;
        int j = path2.startsWith("/") ? 1 : 0;
        int len1 = path1.length();
        int len2 = path2.length();
        while (i < len1 && j < len2) {
            if (path1.charAt(i) == path2.charAt(j) || Character.toLowerCase(path1.charAt(i)) == Character.toLowerCase(path2.charAt(j))) {
                i++;
                j++;
            } else {
                return -1;
            }
        }
        if (len1 == len2) {
            return 0;
        }
        return len1 > len2 ? 1 : 2;
    }

    public static String pathReformat(String path) throws GrammarException {
        int offset = 0;
        String[] pathValues = new String[2];
        for (String pv : path.split(" ", 0)) {
            if (pv.isEmpty()) continue;
            if (offset >= 2) {
                throw new GrammarException("Invalid number of path modifiers. Path reformat fails.");
            }
            pathValues[offset] = pv;
            offset++;
        }
        if (offset == 0) {
            throw new GrammarException("Empty path value. Path reformat fails.");
        }

        if (offset == 2 && !pathPrefixModifier.contains(pathValues[0])) {
            throw new GrammarException("Invalid path modifier. Path reformat fails.");
        }

        // format path value
        return offset == 1 ? pathValues[0] : pathValues[0] + " " + pathValues[1];
    }

    @Deprecated
    public static String extractUriIgnoresFirstDelimiter(String path) throws ValidationException {
        int idxPrefix = 0;
        int idxModifier = 0;
        boolean quote = false;

        char[] pathArray = path.toCharArray();
        for (char c : pathArray) {
            if (c == '"') {
                quote = true;
                idxPrefix++;
            } else if (c == ' ') {
                idxPrefix++;
                idxModifier = idxPrefix;
            } else if (c == '^' || c == '~' || c == '=' || c == '*') {
                idxPrefix++;
            } else if (c == '/') {
                idxPrefix++;
                if (!quote && idxPrefix < pathArray.length && pathArray[idxPrefix] == '"') {
                    quote = true;
                    idxPrefix++;
                }
                break;
            } else {
                break;
            }
        }

        if (quote && !path.endsWith("\"")) {
            throw new ValidationException("Missing end quote. " + "\"path\" : \"" + path + "\"");
        }
        int idxSuffix = quote ? path.length() - 1 : path.length();
        if (idxPrefix == idxSuffix) {
            if (path.charAt(idxSuffix - 1) == '/') {
                return "/";
            } else {
                throw new ValidationException("Path cannot be validated as uri after extraction. " + "\"path\" : \"" + path + "\"");
            }
        }
        idxPrefix = idxPrefix < idxSuffix ?
                (idxModifier > idxPrefix ? idxModifier : idxPrefix) : idxModifier;
        return path.substring(idxPrefix, idxSuffix);
    }
}