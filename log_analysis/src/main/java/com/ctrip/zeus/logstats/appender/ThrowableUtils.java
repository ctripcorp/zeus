package com.ctrip.zeus.logstats.appender;

/**
 * Created by lu.wang on 2016/5/18.
 */
public class ThrowableUtils {
    public static String getStackMsg(Throwable e) {
        if (e != null && e.getClass() != null && e.getClass().getName() != null) {
            StringBuffer sb = new StringBuffer(e.getClass().getName() + ":" + e.getMessage() + "\n");
            StackTraceElement[] stackArray = e.getStackTrace();
            for (int i = 0; i < stackArray.length; i++) {
                StackTraceElement element = stackArray[i];
                sb.append("\tat " + element.toString() + "\n");
            }
            return sb.toString();
        }
        return null;
    }
}
