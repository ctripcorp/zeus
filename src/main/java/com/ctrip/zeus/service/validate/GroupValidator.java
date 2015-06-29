package com.ctrip.zeus.service.validate;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Group;
import org.unidal.dal.jdbc.DalException;

/**
 * Created by zhoumy on 2015/6/29.
 */
public interface GroupValidator {

    void validate(Group group) throws Exception;

    void removable(Long groupId) throws Exception;

    boolean validateGroupSlbs(Group group) throws Exception;
}
