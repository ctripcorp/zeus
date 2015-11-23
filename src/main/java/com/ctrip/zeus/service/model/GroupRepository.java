package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.service.Repository;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
public interface GroupRepository extends Repository {

    List<Group> list(Long[] ids) throws Exception;

    Group getById(Long id) throws Exception;

    Group add(Group group) throws Exception;

    Group addVGroup(Group group) throws Exception;

    Group update(Group group) throws Exception;

    Group updateVGroup(Group group) throws Exception;

    List<Group> updateVersion(Long[] groupIds) throws Exception;

    int delete(Long groupId) throws Exception;

    int deleteVGroup(Long groupId) throws Exception;

    @Deprecated
    List<Group> listGroupsByGroupServer(String groupServerIp) throws Exception;

    @Deprecated
    List<Long> portGroupRel() throws Exception;

    @Deprecated
    void portGroupRel(Long groupId) throws Exception;

    @Deprecated
    Group get(String groupName) throws Exception;

    @Deprecated
    List<Group> list(Long slbId) throws Exception;
}