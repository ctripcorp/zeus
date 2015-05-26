package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupServer;
import com.ctrip.zeus.service.Repository;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
public interface AppRepository extends Repository {

    List<Group> list() throws Exception;

    List<Group> list(String slbName, String virtualServerName) throws Exception;

    List<Group> listLimit(long fromId, int maxCount) throws Exception;

    Group get(String appName) throws Exception;

    Group getByGroupId(String appId) throws Exception;

    /**
     * add an app
     * @param app the app to be added
     * @return the primary key of the app
     * @throws Exception
     */
    long add(Group app) throws Exception;

    void update(Group app) throws Exception;

    /**
     * delete the app by its name
     * @param appName the app name
     * @return the number of rows deleted
     * @throws Exception
     */
    int delete(String appName) throws Exception;

    /**
     * get the name list of apps which are deployed at the specified server
     * @param appServerIp the app server ip of the specified server
     * @return the list of app names
     * @throws Exception
     */
    List<String> listGroupsByGroupServer(String appServerIp) throws Exception;

    /**
     * get the list of app server ips where the specified app is deployed
     * @param appName
     * @return the list of app server ips
     * @throws Exception
     */
    List<String> listGroupServersByGroup(String appName) throws Exception;

    List<GroupServer> getGroupServersByGroup(String appName) throws Exception;
}