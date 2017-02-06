package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.entity.Group;

/**
 * Created by zhoumy on 2015/6/29.
 */
public interface GroupValidator extends ModelValidator<Group> {

    boolean exists(Long targetId, boolean virtual) throws Exception;
}
