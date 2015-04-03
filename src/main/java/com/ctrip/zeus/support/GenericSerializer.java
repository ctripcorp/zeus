package com.ctrip.zeus.support;

/**
 * Created by zhoumy on 2015/4/3.
 */
public class GenericSerializer {
    public static final String JSON = "%#.3s";
    public static final String JSON_COMPACT = "%#s";
    public static final String XML = "%.3s";
    public static final String XML_COMPACT = "%s";

    public static String writeJson(Object object) {
        return writeJson(object, true);
    }

    public static String writeJson(Object object, boolean pretty) {
        if (pretty) {
            return String.format(JSON, object);
        } else {
            return String.format(JSON_COMPACT, object);
        }
    }

    public static String writeXml(Object object) {
        return writeXml(object, true);
    }

    public static String writeXml(Object object, boolean pretty) {
        if (pretty) {
            return String.format(XML, object);
        } else {
            return String.format(XML_COMPACT, object);
        }
    }
}
