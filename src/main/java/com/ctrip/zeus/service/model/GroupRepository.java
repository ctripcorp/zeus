package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.service.Repository;

import java.util.List;
import java.util.Set;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
public interface GroupRepository extends Repository {

    List<Group> list(Long[] ids) throws Exception;

    List<Group> list(Long[] ids, ModelMode mode) throws Exception;

    Group getById(Long id) throws Exception;

    Group getById(Long id, ModelMode mode) throws Exception;

    Group add(Group group) throws Exception;

    Group addVGroup(Group group) throws Exception;

    Group update(Group group) throws Exception;

    Group updateVGroup(Group group) throws Exception;

    void activateGroupVersion(Group[] groups) throws Exception;

    int delete(Long groupId) throws Exception;

    int deleteVGroup(Long groupId) throws Exception;

    @Deprecated
    Set<Long> port(Long[] groupIds) throws Exception;

    @Deprecated
    void syncMemberStatus(Group group) throws Exception;

    @Deprecated
    Group get(String groupName) throws Exception;

    @Deprecated
    List<Group> list(Long slbId) throws Exception;
}