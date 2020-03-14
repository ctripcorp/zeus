package com.ctrip.zeus.exceptions;

public class TagValidationException extends Exception{
    public TagValidationException(String errorMessage) {
        super(errorMessage);
    }
}
