package com.ctrip.zeus.service.model;

import com.ctrip.zeus.dal.core.AppDo;
import com.ctrip.zeus.dal.core.SlbDo;
import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.Slb;
import org.unidal.dal.jdbc.DalException;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
public interface AppSync {
    AppDo sync(App app) throws DalException;
}
