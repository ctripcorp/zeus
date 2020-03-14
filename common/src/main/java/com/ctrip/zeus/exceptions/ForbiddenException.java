package com.ctrip.zeus.exceptions;

/**
 * Created by fanqq on 2015/6/16.
 */
/*
 * return 403
 */
public class ForbiddenException extends Exception {
    public ForbiddenException(String errorMessage) {
        super(errorMessage);
    }
}
