package com.ctrip.zeus.service.impl;

import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.service.AppQuery;
import org.unidal.dal.jdbc.DalException;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
public class AppQueryImpl implements AppQuery {
    @Override
    public List<App> getAll() throws DalException {
        return null;
    }

    @Override
    public App get(String name) throws DalException {
        return null;
    }
}
