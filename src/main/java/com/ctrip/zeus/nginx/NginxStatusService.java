package com.ctrip.zeus.nginx;

import com.ctrip.zeus.nginx.entity.TrafficStatus;

/**
 * @author:xingchaowang
 * @date: 3/19/2015.
 */
public interface NginxStatusService {

    NginxStatus getNginxStatus(String slbName) throws Exception;
}
