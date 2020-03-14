package com.ctrip.zeus.exceptions;

/**
 * Created by fanqq on 2015/6/16.
 */
/*
 * return 400
 */
public class BadRequestException extends Exception {
    public BadRequestException(String errorMessage) {
        super(errorMessage);
    }
}
