package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupServer;
import org.unidal.dal.jdbc.DalException;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
public interface GroupQuery {

    Group get(String name) throws DalException;

    Group getById(long id) throws DalException;

    Group getByAppId(String appId) throws DalException;

    List<Group> getAll() throws DalException;

    List<Group> getLimit(long fromId, int maxCount) throws DalException;

    List<Group> getBySlbAndVirtualServer(String slbName, String virtualServerName) throws DalException;

    List<String> getByGroupServer(String appServerIp) throws DalException;

    List<String> getGroupServerIpsByGroup(long groupId) throws DalException;

    List<GroupServer> getGroupServersByGroup(long groupId) throws DalException;
}