package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.AppList;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.service.Repository;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
public interface AppRepository extends Repository {

    AppList list() throws Exception;

    AppList list(String slbName, String virtualServerName) throws Exception;

    AppList listLimit(long fromId, int maxCount) throws Exception;

    App get(String appName) throws Exception;

    App getByAppId(String appId) throws Exception;

    long add(App app) throws Exception;

    void update(App app) throws Exception;

    int delete(String appName) throws Exception;
}
