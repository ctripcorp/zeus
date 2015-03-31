package com.ctrip.zeus.service.status;


import com.ctrip.zeus.service.Repository;

import java.util.Set;

/**
 * @author:xingchaowang
 * @date: 3/16/2015.
 */
public interface StatusService extends Repository {

    /**
     * get all down app servers
     * @return app server ip list
     * @throws Exception
     */
    Set<String> findAllDownServers() throws Exception;

    /**
     * get all down app servers by slbname
     * @param slbName the slb name
     * @return app server ip list
     * @throws Exception
     */
    Set<String> findAllDownAppServersBySlbName(String slbName) throws Exception;

    /**
     * up server by app server ip
     * @param ip the app server ip
     * @return
     * @throws Exception
     */
    void upServer(String ip) throws Exception;

    /**
     * down server by app server ip
     * @param ip the app server ip
     * @return
     * @throws Exception
     */
    void downServer(String ip) throws Exception;

    /**
     * up member by app server ip and appname
     * @param ip the app server ip
     * @param appName  app name
     * @return
     * @throws Exception
     */
    void upMember(String appName, String ip)throws Exception;
    /**
     * down member by app server ip and appname
     * @param ip the app server ip
     * @param appName  app name
     * @return
     * @throws Exception
     */
    void downMember(String appName, String ip)throws Exception;

    /**
     * get App server status by app name and slbname and virtual server ip
     * @param slbname the slb name
     * @param appName  app name
     * @param vsip  virtual server ip
     * @return true : status=up false : status = down
     * @throws Exception
     */
    boolean getAppServerStatus(String slbname,String appName, String vsip)throws Exception;

    /**
     * get server status by virtual server ip
     * @param vsip  virtual server ip
     * @return true : status=up false : status = down
     * @throws Exception
     */
    boolean getServerStatus(String vsip )throws  Exception;
}
