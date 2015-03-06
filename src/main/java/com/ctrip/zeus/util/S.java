package com.ctrip.zeus.util;

/**
 * @author:xingchaowang
 * @date: 3/6/2015.
 */
public class S {

    public static void setPropertyDefaultValue(String propertyName, String defaultValue) {
        String val = System.getProperty(propertyName);
        if(val==null || val.trim().isEmpty()){
            System.setProperty(propertyName, defaultValue);
        }
    }
}
