package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.service.model.IdVersion;

import java.util.Set;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
public interface GroupSync {

    void add(Group group) throws Exception;

    void add(Group group, boolean isVirtual) throws Exception;

    void update(Group group) throws Exception;

    void updateStatus(IdVersion[] groups) throws Exception;

    int delete(Long groupId) throws Exception;

    @Deprecated
    Set<Long> port(Long[] groupIds) throws Exception;
}
