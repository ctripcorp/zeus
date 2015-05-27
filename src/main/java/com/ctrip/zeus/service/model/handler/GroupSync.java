package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.dal.core.GroupDo;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Group;
import org.unidal.dal.jdbc.DalException;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
public interface GroupSync {

    GroupDo add(Group group) throws DalException, ValidationException;

    GroupDo update(Group group) throws DalException, ValidationException;

    int delete(long groupId) throws DalException;
}
