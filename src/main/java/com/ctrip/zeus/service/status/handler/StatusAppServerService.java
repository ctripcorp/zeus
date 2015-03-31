package com.ctrip.zeus.service.status.handler;

import com.ctrip.zeus.dal.core.StatusAppServerDo;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/14/2015.
 */
public interface StatusAppServerService {

    List<StatusAppServerDo> list() throws Exception;

    List<StatusAppServerDo> listAllDownBySlbName(String slbName) throws Exception;

    List<StatusAppServerDo> listByAppName(String appName) throws Exception;

    List<StatusAppServerDo> listByServer(String ip) throws Exception;

    List<StatusAppServerDo> listBySlbNameAndAppNameAndIp(String slbname,String appname,String ip) throws Exception;
    List<StatusAppServerDo> listBySlbNameAndAppName(String slbname,String appname) throws Exception;
    void deleteBySlbNameAndAppNameAndVsName(String slbname,String appname,String vsname) throws Exception;

    void updateStatusAppServer(StatusAppServerDo d) throws Exception;
}
