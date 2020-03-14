package com.ctrip.zeus.exceptions;

/**
 * Created by lu.wang on 2016/4/19.
 */
public class SlowRequestException extends Exception {
    public SlowRequestException(String message) {
        super(message);
    }

    public SlowRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
