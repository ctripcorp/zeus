package com.ctrip.zeus.util;

/**
 * Created by zhoumy on 2016/6/13.
 */

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtils {

    /**
     * Convert nginx access log time string to java Date object.
     * It reads date, hour, minute information from the input value
     * and replace others with the local calendar information.
     *
     * @param timeStamp the time stamp
     *                  e.g. 18/Nov/2015:17:10:41 +0800
     * @return Date object
     */
    public static Date convert(String timeStamp) {
        return convert(new GregorianCalendar(), timeStamp);
    }

    public static Date convert(Calendar c, String timeStamp) {
        int dd, hh, mm;
        dd = Integer.parseInt(timeStamp.substring(0, 2));
        hh = Integer.parseInt(timeStamp.substring(12, 14));
        mm = Integer.parseInt(timeStamp.substring(15, 17));

        if (c.get(Calendar.DATE) < dd) {
            int month = c.get(Calendar.MONTH);
            if (month - 1 > 0) {
                month = month - 1;
            } else {
                month = 11;
                c.set(Calendar.YEAR, c.get(Calendar.YEAR) - 1);
            }
            c.set(Calendar.MONTH, month);
        }
        c.set(Calendar.DATE, dd);
        c.set(Calendar.HOUR_OF_DAY, hh);
        c.set(Calendar.MINUTE, mm);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }
}
