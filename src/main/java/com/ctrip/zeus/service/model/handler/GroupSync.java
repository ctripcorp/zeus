package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.entity.Group;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
public interface GroupSync {

    void add(Group group) throws Exception;

    void add(Group group, boolean isVirtual) throws Exception;

    void update(Group group) throws Exception;

    void updateVersion(Long[] groupIds) throws Exception;

    int delete(Long groupId) throws Exception;

    @Deprecated
    List<Long> port(Group[] groups) throws Exception;

    @Deprecated
    void port(Group group) throws Exception;
}
