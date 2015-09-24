package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.entity.Group;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
public interface GroupSync {

    void add(Group group) throws Exception;

    void update(Group group) throws Exception;

    void updateVersion(Long[] groupIds) throws Exception;

    int delete(Long groupId) throws Exception;
}
