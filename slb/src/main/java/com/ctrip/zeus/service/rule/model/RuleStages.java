package com.ctrip.zeus.service.rule.model;

public class RuleStages {

    // Server conf

    /* Request restriction
     ---- Supported Engines---
     NULL
    */
    public final static String STAGE_HTTP_INIT_BY_LUA = "STAGE_HTTP_ACCESS";
    public final static String STAGE_SERVER_ACCESS = "STAGE_SERVER_ACCESS";
    public final static String STAGE_SERVER_HTTP2_CONFIG = "STAGE_SERVER_HTTP2_CONFIG";

    public final static String STAGE_SERVER_BOTTOM = "STAGE_SERVER_BOTTOM";


    // Location conf

    /* Request restriction
     ---- Supported Engines---
     CLIENT_MAX_BODY_SIZE
     CLIENT_BODY_BUFFER_SIZE
    */
    public final static String STAGE_LOCATION_REQ_RESTRICTIONS = "STAGE_LOCATION_REQ_RESTRICTIONS";

    /* Request Interception
   ---- Supported Engines---
  */
    public final static String STAGE_REQUEST_INTERCEPTION = "STAGE_REQUEST_INTERCEPTION";

    /* Request restriction
     ---- Supported Engines---
     ENABLE_HSTS
    */
    public final static String STAGE_LOCATION_HSTS_SUPPORT = "STAGE_LOCATION_HSTS_SUPPORT";

    /* Location Access
    ---- Supported Engines---
    PROXY_READ_TIMEOUT
    CLIENT_BODY_BUFFER_SIZE
    */
    public final static String STAGE_LOCATION_ACCESS = "STAGE_LOCATION_ACCESS";

    /* Location LUA REWRITE
    ---- Supported Engines---
    NULL
    */
    public final static String STAGE_LOCATION_LUA_REWRITE = "STAGE_LOCATION_LUA_REWRITE";

    /* Location LUA ACCESS
    ---- Supported Engines---
    NULL
    */
    public final static String STAGE_LOCATION_LUA_ACCESS = "STAGE_LOCATION_LUA_ACCESS";

    /* Location LUA HEADER FILTER
    ---- Supported Engines---
    NULL
    */
    public final static String STAGE_LOCATION_LUA_HEADER_FILTER = "STAGE_LOCATION_LUA_HEADER_FILTER";

    /* Location LUA INIT
    ---- Supported Engines---
    NULL
    */
    public final static String STAGE_LOCATION_LUA_INIT = "STAGE_LOCATION_LUA_INIT";

    /* Location BOTTOM
   ---- Supported Engines---
   ENABLE_HSTS
   */
    public final static String STAGE_LOCATION_BOTTOM = "STAGE_LOCATION_BOTTOM";
    public static final String STAGE_LOCATION_UPSTREAM_ACCESS = "STAGE_LOCATION_UPSTREAM_ACCESS";
    public static final String STAGE_LOCATION_UPSTREAM_BOTTOM = "STAGE_LOCATION_UPSTREAM_BOTTOM";

    //Default Server
    public static final String STAGE_DEFAULT_SERVER_LISTEN_80 = "STAGE_DEFAULT_SERVER_LISTEN_80";
    public static final String STAGE_DEFAULT_SERVER_LISTEN_443 = "STAGE_DEFAULT_SERVER_LISTEN_443";

    //Server SSL
    public static final String STAGE_SERVER_SSL_CONFIG = "STAGE_SERVER_SSL_CONFIG";
    public static final String STAGE_DEFAULT_SERVER_SSL_CONFIG = "STAGE_DEFAULT_SERVER_SSL_CONFIG";

    //Error Page
    public static final String STAGE_SERVER_ERROR_PAGE = "STAGE_SERVER_ERROR_PAGE";
    public static final String STAGE_DEFAULT_SERVER_ERROR_PAGE = "STAGE_DEFAULT_SERVER_ERROR_PAGE";
    public static final String STAGE_LOCATION_ENABLE_ERROR_PAGE = "STAGE_LOCATION_ENABLE_ERROR_PAGE";

    // Favor icon
    public static final String STAGE_FAVOR_ICON = "STAGE_FAVOR_ICON";
    public static final String STAGE_LOCATION_ENABLE_SOCKET_IO = "STAGE_LOCATION_ENABLE_SOCKET_IO";

    // nginx conf
    public static final String STAGE_NGINX_CONF = "STAGE_NGINX_CONF";
}
