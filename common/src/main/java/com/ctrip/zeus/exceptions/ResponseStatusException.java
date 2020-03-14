package com.ctrip.zeus.exceptions;

/**
 * Created by lu.wang on 2016/4/19.
 */
public class ResponseStatusException extends Exception {
    public ResponseStatusException(String message) {
        super(message);
    }

    public ResponseStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}
