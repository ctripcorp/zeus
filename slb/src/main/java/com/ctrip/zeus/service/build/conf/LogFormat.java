package com.ctrip.zeus.service.build.conf;

import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

/**
 * Created by zhoumy on 2015/11/20.
 */
public class LogFormat {
    public static final String VAR_UPSTREAM_NAME = "$upstream_name";
    public static final String VAR_POLICY_NAME = "$policy_name";
    public static final String VAR_DR_NAME = "$dr_name";
    public static final String CANARY_FLAG = "$canary_req";
    public static final String VIA_HEADER = "$http_x_via";
    public static final String CAT_META_HEADER = "$http_cat_meta";
    public static final String HEADER_SIZE = "$header_size";
    public static final String COOKIE_SIZE = "$cookie_size";
    public static final String PAGE_ID_HEADER = "$x_ctrip_pageid";
    public static final String HEADER_VALUE = "$header_value";
    public static final String SET_COOKIE_VALUE = "$set_cookie_value";
    public static final String METHOD = "$method";
    public static final String SLB_CLIENT_ADDR = "$slb_client_addr";
    public static final String REMOTE_ADDR = "$remote_addr";
    public static final String WAF_COST = "$waf_cost";
    public static final String RESPONSE_HEADER_MESSAGE_ID = "$sent_http_RootMessageId";
    public static final String REQUEST_HEADER_ORGIN = "$http_origin";
    public static final String SSL_PROTOCOL = "$ssl_protocol";
    public static final String SBU = "$sbu";
    public static final String INTERCEPT_STATUS = "$intercept_status";
    public static final String APP = "$app";

    private static final DynamicBooleanProperty useDefaultFormat = DynamicPropertyFactory.getInstance().getBooleanProperty("slb.use.default.log.format.version", false);

    private static DynamicStringProperty logFormat = DynamicPropertyFactory.getInstance().getStringProperty("slb.nginx.log-format",
            "'[$time_local]^A$host^A$hostname^A$server_addr^A$method^A$request_uri^A'\n" +
                    "'$server_port^A$remote_user^A$remote_addr^A$http_x_forwarded_for^A'\n" +
                    "'$server_protocol^A\"$http_user_agent\"^A\"$http_cookie\"^A\"$http_referer\"^A'\n" +
                    "'$status^A$request_length^A$bytes_sent^A$request_time^A$upstream_response_time^A'\n" +
                    "'$upstream_addr^A$upstream_status^A" + VAR_UPSTREAM_NAME + "^A" + CANARY_FLAG + "^A" + VAR_POLICY_NAME +
                    "^A\"" + VIA_HEADER + "\"^A\"" + CAT_META_HEADER + "\"^A" + HEADER_SIZE +
                    "^A" + COOKIE_SIZE + "^A\"" + HEADER_VALUE + "\"^A" + PAGE_ID_HEADER + "^A'\n" +
                    "'\"" + SET_COOKIE_VALUE + "\"^A" + VAR_DR_NAME + "^A" + RESPONSE_HEADER_MESSAGE_ID + "^A" + WAF_COST + "^A" + REQUEST_HEADER_ORGIN +
                    "^A" + SSL_PROTOCOL + "^A" + SBU + "^A" + APP + "^A" + INTERCEPT_STATUS + "^A'\n");
                
    private static DynamicStringProperty separator = DynamicPropertyFactory.getInstance().getStringProperty("slb.nginx.log-format", "^A");

    private static DynamicStringProperty defaultLogFormat = DynamicPropertyFactory.getInstance().getStringProperty("slb.nginx.default.log-format",
            "'[$time_local] $host $hostname $server_addr $method $request_uri '\n" +
                    "'$server_port $remote_user $remote_addr $http_x_forwarded_for '\n" +
                    "'$server_protocol \"$http_user_agent\" \"$http_cookie\" \"$http_referer\" '\n" +
                    "'$status $request_length $bytes_sent $request_time $upstream_response_time '\n" +
                    "'$upstream_addr $upstream_status " + VAR_UPSTREAM_NAME + " " + CANARY_FLAG + " " + VAR_POLICY_NAME +
                    " \"" + VIA_HEADER + "\" \"" + CAT_META_HEADER + "\" " + HEADER_SIZE +
                    " " + COOKIE_SIZE + " \"" + HEADER_VALUE + "\" " + PAGE_ID_HEADER + " '\n" +
                    "'\"" + SET_COOKIE_VALUE + "\" " + VAR_DR_NAME + " " + RESPONSE_HEADER_MESSAGE_ID + " " + WAF_COST + " " + REQUEST_HEADER_ORGIN
                    + " " + SSL_PROTOCOL + " " + SBU + " " + APP + " " + INTERCEPT_STATUS + "'\n");
                  
    private static DynamicStringProperty defaultSeparator = DynamicPropertyFactory.getInstance().getStringProperty("slb.nginx.default.log-format", " ");


    public static String getMain() {
        if (useDefaultFormat.get()) {
            return defaultLogFormat.get();
        } else {
            return logFormat.get();
        }
    }

    public static String getSeparator() {
        return separator.get();
    }

    public static String getMainCompactString() {
        return logFormat.get().replaceAll("'", "").replaceAll("\\n", "");
    }


    public static String getDefaultMain() {
        return defaultLogFormat.get();
    }

    public static String getDefaultSeparator() {
        return defaultSeparator.get();
    }

    public static String getDefaultMainCompactString() {
        return defaultLogFormat.get().replaceAll("'", "").replaceAll("\\n", "");
    }
}
