package com.ctrip.zeus.domain;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
public enum  LBMethod {
    ROUND_ROBIN,
    LESS_CONN,
    IP_HASH,
    HASH;
}
