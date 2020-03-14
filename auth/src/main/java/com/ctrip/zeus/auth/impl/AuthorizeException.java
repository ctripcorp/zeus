package com.ctrip.zeus.auth.impl;


/**
 * User: mag
 * Date: 4/22/2015
 * Time: 1:43 PM
 */
public class AuthorizeException extends Exception{
    public AuthorizeException(){

    }

    public AuthorizeException(String message){
        super(message);
    }

    public AuthorizeException(String message, Throwable t){
        super(message, t);
    }

    public AuthorizeException(Throwable t){
        super(t);
    }


}
