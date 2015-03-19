package com.ctrip.zeus.service.status;

import com.ctrip.zeus.model.entity.AppStatus;
import com.ctrip.zeus.model.entity.ServerStatus;

import java.util.Set;

/**
 * @author:xingchaowang
 * @date: 3/16/2015.
 */
public interface StatusService {

    Set<String> findAllDownServers();
    Set<String> findAllDownAppServers(String slbName);
    //Temp method
    Set<String> findAllDownAppServers(String slbName, String appName);

    void upServer(String ip);

    void downServer(String ip);

    void upMember(String appName, String ip);

    void downMember(String appName, String ip);

    AppStatus getAppStatus(String appName);

    ServerStatus getServerStatus(String ip);
}
