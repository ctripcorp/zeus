package com.ctrip.zeus.service.status;

import java.util.Set;

/**
 * @author:xingchaowang
 * @date: 3/16/2015.
 */
public interface StatusService {

    Set<String> findAllDownServers();
    Set<String> findAllDownAppServers(String slbName);

    void upServer(String ip);

    void downServer(String ip);

    void upMember(String appName, String ip);

    void downMember(String appName, String ip);
}
