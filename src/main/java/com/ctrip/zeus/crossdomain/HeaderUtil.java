package com.ctrip.zeus.crossdomain;

/**
 * Created by lu.wang on 2016/6/16.
 */
public class HeaderUtil {
    public static final String ORIGIN = "Origin";
    public static final String ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    public static final String REQUEST_METHOD = "Access-Control-Request-Method";
    public static final String REQUEST_HEADERS = "Access-Control-Request-Headers";
    public static final String ALLOW_METHODS = "Access-Control-Allow-Methods";
    public static final String ALLOW_HEADERS = "Access-Control-Allow-Headers";
    public static final String ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";

    public static final String OPTIONS_METHOD = "OPTIONS";

    public static class HeaderValues
    {
        public static final String ALLOW_CREDENTIALS_TRUE = "true";
    }

}
