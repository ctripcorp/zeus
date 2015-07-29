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

    List<Group> list(Long slbId, String virtualServerName) throws Exception;

    List<Group> list(Long[] ids) throws Exception;

    Group getById(Long id) throws Exception;

    Group get(String groupName) throws Exception;

    Group getByAppId(String appId) throws Exception;

    Group add(Group group) throws Exception;

    Group update(Group group) throws Exception;

    int delete(Long groupId) throws Exception;

    List<String> listGroupsByGroupServer(String groupServerIp) throws Exception;


}