package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.service.Repository;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
public interface SlbRepository extends Repository {

    List<Slb> list() throws Exception;

    Slb get(String slbName) throws Exception;

    /**
     * get the slb by its server ip
     * @param slbServerIp the server ip where slb is deployed
     * @return the slb entity
     * @throws Exception
     */
    Slb getBySlbServer(String slbServerIp) throws Exception;

    /**
     * get the slb list which manage the app server ip and app name
     * @param appServerIp the app server ip
     * @param appName the app name
     * @return the list of slbs
     * @throws Exception
     */
    List<Slb> listByAppServerAndAppName(String appServerIp, String appName) throws Exception;

    void add(Slb slb) throws Exception;

    void update(Slb slb) throws Exception;

    /**
     * delete the slb by its name
     * @param slbName the slb name
     * @return the number of rows deleted
     * @throws Exception
     */
    int delete(String slbName) throws Exception;

    /**
     * get the server list managed by the given slb
     * @param slbName the slb name
     * @return the list of server ips
     * @throws Exception
     */
    List<String> listAppServersBySlb(String slbName) throws Exception;
}
