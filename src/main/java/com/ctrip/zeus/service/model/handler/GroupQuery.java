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

    Group getById(Long id) throws DalException;

    Group getByAppId(String appId) throws DalException;

    Long[] batchGetByNames(String[] names) throws DalException;

    List<Group> getAll() throws DalException;

    List<Group> getLimit(Long fromId, int maxCount) throws DalException;

    List<Group> getByVirtualServer(Long virtualServerId) throws DalException;

    List<String> getByGroupServer(String groupServerIp) throws DalException;

    List<String> getGroupServerIpsByGroup(Long groupId) throws DalException;

    List<GroupServer> getGroupServersByGroup(Long groupId) throws DalException;
}