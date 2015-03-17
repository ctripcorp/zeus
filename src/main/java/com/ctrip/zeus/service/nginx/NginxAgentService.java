package com.ctrip.zeus.service.nginx;

/**
 * @author:xingchaowang
 * @date: 3/17/2015.
 */
public interface NginxAgentService {
    void reloadConf(String slbName);
}
