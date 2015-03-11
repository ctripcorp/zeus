package com.ctrip.zeus.service;

import com.ctrip.zeus.dal.core.AppDao;
import com.ctrip.zeus.dal.core.AppDo;
import com.ctrip.zeus.dal.core.AppEntity;
import com.ctrip.zeus.service.AppQuery;
import com.ctrip.zeus.service.AppSync;
import org.springframework.stereotype.Repository;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * @author:xingchaowang
 * @date: 3/10/2015.
 */
@Repository
public class DemoRepository {
    @Resource(name = "embeddedDataSource")
    private DataSource embeddedDataSource;


    @Resource
    private AppDao appDao;

    public void addApp(String name) {
        try {
            appDao.insert(new AppDo().setAppId("test").setName(name));
        } catch (DalException e) {
            e.printStackTrace();
        }
    }

    public void addAppError(String name) throws Exception {
        try {
            appDao.insert(new AppDo().setAppId("test").setName(name));
        } catch (DalException e) {
            e.printStackTrace();
        }

        throw new Exception("rollback");
    }

    public AppDo getApp(String name) {
        try {
            return appDao.findByName(name, AppEntity.READSET_FULL);
        } catch (DalException e) {
            e.printStackTrace();
        }
        return new AppDo();
    }

    public void deleteApp(AppDo appDo) {
        try {
            appDao.deleteByPK(appDo);
        } catch (DalException e) {
            e.printStackTrace();
        }
    }
}
