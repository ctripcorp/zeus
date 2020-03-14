package com.ctrip.zeus.server;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
public interface Server {
    void start() throws Exception;
    void close();
}
