package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.dal.core.AppDo;
import com.ctrip.zeus.dal.core.SlbDo;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.Slb;
import org.unidal.dal.jdbc.DalException;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
public interface AppSync {
    AppDo add(App app) throws DalException, ValidationException;

    AppDo update(App app) throws DalException, ValidationException;

    int delete(String name) throws DalException;
}
