package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.GroupSlb;
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
     * get the slb list which manage the app server ip or/and app name
     * @param appServerIp the app server ip
     * @param appName the app name
     * @return the list of slbs
     * @throws Exception
     */
    List<Slb> listByGroupServerAndGroupName(String appServerIp, String appName) throws Exception;

    /**
     * get the slb list which manage the apps
     * @param appNames the app names
     * @return the list of slbs
     * @throws Exception
     */
    List<Slb> listByGroups(String[] appNames) throws Exception;

    /**
     * get the list of app related slb information by app names
     * @param appNames the app names
     * @return the list of app related slb information
     * @throws Exception
     */
    List<GroupSlb> listGroupSlbsByGroups(String[] appNames) throws Exception;

    /**
     * get thr list of app related slb information by slb name
     * @param slbName the slb name
     * @return the list of app related slb information
     * @throws Exception
     */
    List<GroupSlb> listGroupSlbsBySlb(String slbName) throws Exception;

    void add(Slb slb) throws Exception;

    void update(Slb slb) throws Exception;

    /**
     * delete the slb by its primary id
     * @param slbId the slb primary id
     * @return the number of rows deleted
     * @throws Exception
     */
    int delete(long slbId) throws Exception;

    /**
     * get the server list managed by the given slb
     * @param slbName the slb name
     * @return the list of server ips
     * @throws Exception
     */
    List<String> listGroupServersBySlb(String slbName) throws Exception;
}
