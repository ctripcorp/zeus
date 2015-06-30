package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.entity.Group;

/**
 * Created by zhoumy on 2015/6/29.
 */
public interface GroupValidator {

    void validate(Group group) throws Exception;

    void removable(Long groupId) throws Exception;

    boolean validateGroupSlbs(Group group) throws Exception;
}
