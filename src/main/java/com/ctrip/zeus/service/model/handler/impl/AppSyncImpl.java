package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.handler.AppSync;
import com.ctrip.zeus.support.C;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
@Component("appSync")
public class AppSyncImpl implements AppSync {
    @Resource
    private AppDao appDao;
    @Resource
    private AppHealthCheckDao appHealthCheckDao;
    @Resource
    private AppLoadBalancingMethodDao appLoadBalancingMethodDao;
    @Resource
    private AppServerDao appServerDao;
    @Resource
    private AppSlbDao appSlbDao;
    @Resource
    private SlbDao slbDao;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public AppDo add(App app) throws DalException, ValidationException {
        validate(app);
        AppDo d= C.toAppDo(app);
        d.setCreatedTime(new Date());
        d.setVersion(1);

        appDao.insert(d);
        cascadeSync(d, app);

        return d;
    }

    @Override
    public AppDo update(App app) throws DalException, ValidationException {
        validate(app);
        AppDo check = appDao.findByName(app.getName(), AppEntity.READSET_FULL);
        if (check.getVersion() > app.getVersion())
            throw new ValidationException("Newer App version is detected.");

        AppDo d= C.toAppDo(app);
        appDao.updateByName(d, AppEntity.UPDATESET_FULL);

        AppDo updated = appDao.findByName(app.getName(), AppEntity.READSET_FULL);
        d.setId(updated.getId());
        d.setVersion(updated.getVersion());
        cascadeSync(d, app);
        return d;
    }

    @Override
    public int delete(String name) throws DalException {
        AppDo d = appDao.findByName(name, AppEntity.READSET_FULL);
        if (d == null)
            return 0;
        appSlbDao.deleteByApp(new AppSlbDo().setAppName(d.getName()));
        appServerDao.deleteByApp(new AppServerDo().setAppId(d.getId()));
        appHealthCheckDao.deleteByApp(new AppHealthCheckDo().setAppId(d.getId()));
        appLoadBalancingMethodDao.deleteByApp(new AppLoadBalancingMethodDo().setAppId(d.getId()));

        return appDao.deleteByName(d);
    }

    private void validate(App app) throws DalException, ValidationException {
        if (app == null) {
            throw new ValidationException("App with null value cannot be persisted.");
        }
        if (!validateSlb(app))
            throw new ValidationException("App with invalid slb data cannot be persisted.");
    }

    private boolean validateSlb(App app) throws DalException {
        if (app.getAppSlbs().size() == 0)
            return false;
        for (AppSlb as : app.getAppSlbs()) {
            if (slbDao.findByName(as.getSlbName(), SlbEntity.READSET_FULL) == null)
                return false;
        }
        return true;
    }

    private void cascadeSync(AppDo d, App app) throws DalException {
        syncAppSlbs(app.getName(), app.getAppSlbs());
        syncAppHealthCheck(d.getId(), app.getHealthCheck());
        syncLoadBalancingMethod(d.getId(), app.getLoadBalancingMethod());
        syncAppServers(d.getId(), app.getAppServers());
    }

    private void syncAppSlbs(String appName, List<AppSlb> appSlbs) throws DalException {
        List<AppSlbDo> oldList = appSlbDao.findAllByApp(appName, AppSlbEntity.READSET_FULL);
        Map<String, AppSlbDo> oldMap = Maps.uniqueIndex(oldList, new Function<AppSlbDo, String>() {
            @Override
            public String apply(AppSlbDo input) {
                return input.getAppName() + input.getSlbName() + input.getSlbVirtualServerName();
            }
        });

        //Update existed if necessary, and insert new ones.
        for (AppSlb e : appSlbs) {
            AppSlbDo old = oldMap.get(appName + e.getSlbName() + e.getVirtualServer().getName());
            if (old != null) {
                oldList.remove(old);
            }
            appSlbDao.insert(C.toAppSlbDo(e)
                    .setAppName(appName)
                    .setCreatedTime(new Date()));
        }

        //Remove unused ones.
        for (AppSlbDo d : oldList) {
            appSlbDao.deleteByPK(new AppSlbDo().setId(d.getId()));
        }
    }

    private void syncAppHealthCheck(long appKey, HealthCheck healthCheck) throws DalException {
        if (healthCheck == null) {
            logger.info("No health check method is found when adding/updating app with id " + appKey);
            return;
        }
        appHealthCheckDao.insert(C.toAppHealthCheckDo(healthCheck)
                .setAppId(appKey)
                .setCreatedTime(new Date()));
    }

    private void syncLoadBalancingMethod(long appKey, LoadBalancingMethod loadBalancingMethod) throws DalException {
        if (loadBalancingMethod == null)
            return;
        appLoadBalancingMethodDao.insert(C.toAppLoadBalancingMethodDo(loadBalancingMethod)
                .setAppId(appKey)
                .setCreatedTime(new Date()));
    }

    private void syncAppServers(long appKey, List<AppServer> appServers) throws DalException {
        if (appServers == null || appServers.size() == 0) {
            logger.warn("No app server is given when adding/update app with id " + appKey);
            return;
        }
        List<AppServerDo> oldList = appServerDao.findAllByApp(appKey, AppServerEntity.READSET_FULL);
        Map<String, AppServerDo> oldMap = Maps.uniqueIndex(oldList, new Function<AppServerDo, String>() {
            @Override
            public String apply(AppServerDo input) {
                return input.getAppId() + input.getIp();
            }
        });

        //Update existed if necessary, and insert new ones.
        for (AppServer e : appServers) {
            AppServerDo old = oldMap.get(appKey + e.getIp());
            if (old != null) {
                oldList.remove(old);
            }
            appServerDao.insert(C.toAppServerDo(e)
                    .setAppId(appKey)
                    .setCreatedTime(new Date()));
        }

        //Remove unused ones.
        for (AppServerDo d : oldList) {
            appServerDao.deleteByPK(new AppServerDo().setId(d.getId()));
        }
    }
}
