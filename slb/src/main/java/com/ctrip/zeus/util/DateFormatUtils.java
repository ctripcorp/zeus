package com.ctrip.zeus.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by fanqq on 2017/6/15.
 */
public class DateFormatUtils {
    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static synchronized String writeDate(Date date) {
        return formatter.format(date);
    }

    public static synchronized Date parserDate(String date) throws ParseException {
        return formatter.parse(date);
    }
}
