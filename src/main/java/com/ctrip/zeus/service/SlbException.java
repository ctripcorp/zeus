package com.ctrip.zeus.service;

/**
 * User: mag
 * Date: 3/26/2015
 * Time: 10:33 AM
 */
public class SlbException extends Exception{
    public SlbException(){
       super();
    }

    public SlbException(String message, Throwable t){
        super(message,t);
    }

    public SlbException(Throwable t){
        super(t);
    }
}
