package com.ctrip.zeus.domain;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
public enum LBMethod {
    ROUND_ROBIN(""),
    LESS_CONN("less_conn"),
    IP_HASH("ip_hash"),
    HASH("hash");

    private String value;

    LBMethod(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static final String TYPE_ROUND_ROBIN = "roundrobin";
    public static final String TYPE_LESS_CONN = "less_conn";
    public static final String TYPE_IP_HASH = "ip_hash";
    public static final String TYPE_HASH = "hash";

    public static LBMethod getMethod(String type) {
        switch (type) {
            case LBMethod.TYPE_LESS_CONN:
                return LESS_CONN;
            case TYPE_IP_HASH:
                return IP_HASH;
            case TYPE_HASH:
                return HASH;
            default:
                return ROUND_ROBIN;
        }
    }
}
