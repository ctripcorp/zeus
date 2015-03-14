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
    AppList list();

    AppList list(String slbName, String virtualServerName);

    App get(String appName);

    void addOrUpdate(App app);
}
