package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupServer;
import com.ctrip.zeus.service.Repository;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
public interface GroupRepository extends Repository {

    List<Group> list() throws Exception;

    List<Group> list(String slbName, String virtualServerName) throws Exception;

    List<Group> listLimit(long fromId, int maxCount) throws Exception;

    Group get(String groupName) throws Exception;

    Group getByAppId(String groupId) throws Exception;

    /**
     * add an group
     * @param group the group to be added
     * @return the primary key of the group
     * @throws Exception
     */
    long add(Group group) throws Exception;

    void update(Group group) throws Exception;

    /**
     * delete the group by its primary key
     * @param groupId the group primary key
     * @return the number of rows deleted
     * @throws Exception
     */
    int delete(long groupId) throws Exception;

    /**
     * get the name list of groups which are deployed at the specified server
     * @param groupServerIp the group server ip of the specified server
     * @return the list of group names
     * @throws Exception
     */
    List<String> listGroupsByGroupServer(String groupServerIp) throws Exception;

    /**
     * get the list of group server ips where the specified group is deployed
     * @param groupId the group primary key
     * @return the list of group server ips
     * @throws Exception
     */
    List<String> listGroupServerIpsByGroup(long groupId) throws Exception;

    List<GroupServer> listGroupServersByGroup(long groupId) throws Exception;
}