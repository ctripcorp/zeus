package com.ctrip.zeus.exceptions;

/**
 * Created by zhoumy on 2016/3/17.
 */
public class NginxProcessingException extends Exception {

    public NginxProcessingException(String message) {
        super(message);
    }

    public NginxProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}