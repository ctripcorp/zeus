package com.ctrip.zeus.service.report;

import com.ctrip.zeus.service.build.conf.LogFormat;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class AccessLogReporterConsts {
    public static final String TIME_KEY= "time_local";
    public static final String COST_KEY = "request_time";
    public static final String STATUS_KEY = "status";
    public static final String UP_COST_KEY = "upstream_response_time";
    public static final String URI_KEY = "request_uri";
    public static final String GROUP_KEY = "upstream_name";
    public static final String DR_KEY = "dr_name";
    public static final String WAF_COST_KEY = "waf_cost";
    public static final String SSL_PROTOCOL_KEY = "ssl_protocol";
    public static final String INTERCEPT_STATUS = LogFormat.INTERCEPT_STATUS.replace("$", "");
    public static final Set<String> TAGGABLEKEYS = ImmutableSet.of("host", "method", URI_KEY, "server_port", "server_protocol", STATUS_KEY, COST_KEY,
            UP_COST_KEY, "upstream_addr", "upstream_status", GROUP_KEY, "policy_name", "canary_req", "http_x_via", "sbu", "group_appid", "vsid", "diff", DR_KEY, WAF_COST_KEY, SSL_PROTOCOL_KEY, INTERCEPT_STATUS);

}
