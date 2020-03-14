package com.ctrip.zeus.service.rule.model;

public enum RuleType {
    DIRECTIVE(1, true),
    PROXY_READ_TIMEOUT(2, false),
    CLIENT_MAX_BODY_SIZE(3, false),
    CLIENT_BODY_BUFFER_SIZE(4, false),
    ENABLE_HSTS(5, false),
    GZIP(6, false),
    KEEP_ALIVE_TIMEOUT(7, false),
    LARGE_CLIENT_HEADER(8, false),
    UPSTREAM_KEEP_ALIVE_TIMEOUT(9, false),
    PROTOCOL_RESPONSE_HEADER(10, false),
    PROXY_REQUEST_BUFFER_ENABLE(11, false),
    HIDE_HEADER(12, true),
    ADD_HEADER(13, true),
    REWRITE_BY_LUA(14, false),
    INIT_BY_LUA(15, false),
    ACCESS_BY_LUA(16, false),
    SET_BY_LUA(17, true),
    REQUEST_ID_ENABLE(18, false),
    DEFAULT_LISTEN_RULE(19, false),
    SERVER_HTTP2_CONFIG_RULE(20, false),
    SERVER_PROXY_BUFFER_SIZE_RULE(21, false),
    DIRECTIVE_CONFIG(22, true),
    SSL_CONFIG(23, false),
    CONDITION_REDIRECT(24, true),
    ERROR_PAGE(25, false),
    DEFAULT_ERROR_PAGE(26, false),
    DEFAULT_SSL_CONFIG(27, false),
    UPSTREAM_KEEP_ALIVE_COUNT(28, false),
    FAVICON_RULE(29, false),
    REQUEST_INTERCEPT_RULE(30, true),
    GROUP_ERROR_PAGE_ENABLE(31, false),
    SOCKET_IO_ENABLED(32, false),
    SET_REQUEST_HEADER(33, true),
    LOG_SET_COOKIE_VALUE(34, false),
    REQUEST_INTERCEPT_FOR_IP_BLACK_LIST_RULE(35, false),


    LOG_LARGE_HEADER_SIZE(36, false),
    HEADER_X_CTRIP_SOURCE_REGION(37, false),
    DEFAULT_DOWNLOAD_IMAGE(38, false),
    PAGE_ID(39, false),
    SSL_SESSION_CACHE(40, false),
    ACCESS_CONTROL(41, false),
    SERVER_NAME_HASH(42, false),
    REQUEST_INTERCEPT_FOR_ANTIBOT_RULE(43,true),
    HTTP_REDIRECT_RULE(44, false),
    SHARDING_RULE(45, false),;

    final int id;
    final boolean allowMultiple;

    RuleType(int i, boolean allowMultiple) {
        this.id = i;
        this.allowMultiple = allowMultiple;
    }

    public String getName() {
        return name();
    }

    public int getId() {
        return id;
    }

    public boolean isAllowMultiple() {
        return allowMultiple;
    }

    public static RuleType getRuleType(int i) {
        RuleType result = null;

        if (i > 0) {
            RuleType[] constants = RuleType.class.getEnumConstants();
            for (int j = 0; j < constants.length; j++) {
                if (i == constants[j].getId()) {
                    result = constants[j];
                    break;
                }
            }
        }
        return result;
    }

    public static RuleType getRuleType(String type) {
        RuleType result = null;

        if (type == null) return result;

        RuleType[] constants = RuleType.class.getEnumConstants();
        for (int i = 0; i < constants.length; i++) {
            if (type.equalsIgnoreCase(constants[i].getName())) {
                result = constants[i];
                break;
            }
        }
        return result;
    }
}
