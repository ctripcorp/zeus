package com.ctrip.zeus.util;

import com.ctrip.zeus.restful.response.entity.ErrorMessage;

/**
 * Created by zhoumy on 2015/4/2.
 */
public class ExceptionUtils {
    public static String getErrorCode(Throwable throwable) {
        return throwable.getClass().getSimpleName();
    }

    public static String getMessage(Throwable throwable) {
        return throwable.getMessage() != null ? throwable.getMessage() : throwable.getClass().getName();
    }

    public static String getStackTrace(Throwable throwable) {
        String print = "";
        StackTraceElement[] stackTraces = throwable.getStackTrace();
        for (StackTraceElement e : stackTraces) {
            print += e.getClassName();
        }
        return print;
    }

    public static ErrorMessage getErrorMessage(Throwable throwable) {
        return new ErrorMessage().setCode(ExceptionUtils.getErrorCode(throwable))
                .setMessage(ExceptionUtils.getMessage(throwable))
                .setStackTrace(ExceptionUtils.getStackTrace(throwable));
    }
}
