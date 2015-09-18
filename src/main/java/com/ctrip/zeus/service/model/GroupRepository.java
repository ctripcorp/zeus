package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.service.Repository;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
public interface GroupRepository extends Repository {

    List<Group> list() throws Exception;

    List<Group> list(Long slbId) throws Exception;

    List<Group> list(Long[] ids) throws Exception;

    Group getById(Long id) throws Exception;

    Group get(String groupName) throws Exception;

    List<Group> listByAppId(String appId) throws Exception;

    Group add(Group group) throws Exception;

    Group update(Group group) throws Exception;

    List<Group> updateVersion(Long[] groupIds) throws Exception;

    int delete(Long groupId) throws Exception;

    List<Group> listGroupsByGroupServer(String groupServerIp) throws Exception;
}