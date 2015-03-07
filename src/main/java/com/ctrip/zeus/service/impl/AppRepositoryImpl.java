package com.ctrip.zeus.service.impl;

import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.service.AppRepository;
import com.ctrip.zeus.service.AppSync;
import org.springframework.stereotype.Repository;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
@Repository("appRepository")
public class AppRepositoryImpl implements AppRepository {
    @Resource
    private AppSync appSync;

    @Override
    public List<App> list() {
        return null;
    }

    @Override
    public App get(String appName) {
        return null;
    }

    @Override
    public void add(App app) {
        try {
            appSync.sync(app);
        } catch (DalException e) {
            e.printStackTrace();
        }
    }
}
