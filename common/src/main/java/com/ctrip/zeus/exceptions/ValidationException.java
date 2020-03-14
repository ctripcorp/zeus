package com.ctrip.zeus.exceptions;

/**
 * Created by zhoumy on 2015/3/25.
 */
public class ValidationException extends Exception {
    public ValidationException(String errorMessage) {
        super(errorMessage);
    }
}
