package com.ctrip.zeus.nginx;

/**
 * @author:xingchaowang
 * @date: 3/19/2015.
 */
public interface NginxStatusService {

    NginxStatus getNginxStatus(String slbName) throws Exception;
}
