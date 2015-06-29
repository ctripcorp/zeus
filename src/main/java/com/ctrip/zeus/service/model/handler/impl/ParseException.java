package com.ctrip.zeus.service.model.handler.impl;

/**
 * Created by zhoumy on 2015/6/29.
 */
public class ParseException extends Exception {
    public ParseException(String moreInfo) {
        super("Fail to parse the string value: " + moreInfo);
    }
}
