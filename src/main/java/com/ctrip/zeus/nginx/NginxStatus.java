package com.ctrip.zeus.nginx;

/**
 * @author:xingchaowang
 * @date: 3/19/2015.
 */
public interface NginxStatus {
    boolean hasServer(String ip);

    boolean appHasServer(String vhostName, String appName, String ip);

    boolean serverIsUp(String ip);

    boolean appServerIsUp(String vhostName, String appName, String ip);

    boolean appServerIsUp(String appName, String ip);
}
