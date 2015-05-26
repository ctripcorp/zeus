package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.dal.core.GroupDo;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Group;
import org.unidal.dal.jdbc.DalException;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
public interface AppSync {
    GroupDo add(Group app) throws DalException, ValidationException;

    GroupDo update(Group app) throws DalException, ValidationException;

    int delete(String name) throws DalException;
}
