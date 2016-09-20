package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.entity.Group;

/**
 * Created by zhoumy on 2015/11/23.
 */
public interface VGroupValidator extends ModelValidator<Group> {

    void validate(Group target, boolean escapePathValidation) throws Exception;
}
