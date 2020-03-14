package com.ctrip.zeus.exceptions;

/**
 * Created by zhoumy on 2016/4/14.
 */
public class NginxTimeoutException extends Exception {
    public NginxTimeoutException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
