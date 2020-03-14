package com.ctrip.zeus.exceptions;

/**
 * Created by fanqq on 2015/6/16.
 */
/*
 * return 404
 */
public class NotFoundException extends Exception {
    public NotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
