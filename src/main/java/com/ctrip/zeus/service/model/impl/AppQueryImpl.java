package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.AppQuery;
import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
@Component("appQuery")
public class AppQueryImpl implements AppQuery {
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
    @Resource
    private SlbDomainDao slbDomainDao;
    @Resource
    private SlbVirtualServerDao slbVirtualServerDao;


    @Override
    public App get(String name) throws DalException {
        AppDo d = appDao.findByName(name, AppEntity.READSET_FULL);
        App app = C.toApp(d);

        fillData(d, app);
        return app;
    }

    @Override
    public App getById(long id) throws DalException {
        List<AppDo> list = appDao.findAllByIds(new long[] {id}, AppEntity.READSET_FULL);
        if (list.size() > 0) {
            AppDo d = list.get(0);
            App app = C.toApp(d);
            fillData(d, app);
            return app;
        }
        return null;
    }

    @Override
    public App getByAppId(String appId) throws DalException {
        AppDo d = appDao.findByAppId(appId, AppEntity.READSET_FULL);
        App app = C.toApp(d);

        fillData(d, app);
        return app;
    }

    @Override
    public List<App> getAll() throws DalException {
        List<App> list = new ArrayList<>();
        for (AppDo d : appDao.findAll(AppEntity.READSET_FULL)) {
            App app = C.toApp(d);
            list.add(app);

            fillData(d, app);

        }
        return list;
    }

    @Override
    public List<App> getLimit(long fromId, int maxCount) throws DalException {
        List<App> list = new ArrayList<>();
        for(AppDo d : appDao.findLimit(fromId, maxCount, AppEntity.READSET_FULL)) {
            App app = C.toApp(d);
            list.add(app);

            fillData(d, app);
        }
        return list;
    }

    @Override
    public List<App> getBySlbAndVirtualServer(String slbName, String virtualServerName) throws DalException {
        List<AppSlbDo> l = appSlbDao.findAllBySlbAndVirtualServer(slbName, virtualServerName, AppSlbEntity.READSET_FULL);
        int size = l.size();
        String[] names = new String[size];
        for (int i = 0; i < size; i++) {
            names[i] = l.get(i).getAppName();
        }

        List<App> list = new ArrayList<>();
        for (AppDo d : appDao.findAllByNames(names, AppEntity.READSET_FULL)) {
            App app = C.toApp(d);
            list.add(app);

            fillData(d, app);
        }
        return list;
    }

    @Override
    public List<String> getByAppServer(String appServerIp) throws DalException {
        List<String> appNames = new ArrayList<>();
        for (AppServerDo asd : appServerDao.findAllByIp(appServerIp, AppServerEntity.READSET_FULL)) {
            AppDo ad = appDao.findByPK(asd.getAppId(), AppEntity.READSET_FULL);
            if (ad == null)
                continue;
            appNames.add(ad.getName());
        }
        return appNames;
    }

    @Override
    public List<String> getAppServersByApp(String appName) throws DalException {
        AppDo ad = appDao.findByName(appName, AppEntity.READSET_FULL);
        if (ad == null)
            return null;
        List<String> appServers = new ArrayList<>();
        for (AppServerDo asd : appServerDao.findAllByApp(ad.getId(), AppServerEntity.READSET_FULL)) {
            appServers.add(asd.getIp());
        }
        return appServers;
    }

    private void fillData(AppDo d, App app) throws DalException {
        queryAppSlbs(d.getName(), app);
        queryAppHealthCheck(d.getId(), app);
        queryLoadBalancingMethod(d.getId(), app);
        queryAppServers(d.getId(), app);
    }

    private void queryAppSlbs(String appName, App app) throws DalException {
        List<AppSlbDo> list = appSlbDao.findAllByApp(appName, AppSlbEntity.READSET_FULL);
        for (AppSlbDo d : list) {
            AppSlb e = C.toAppSlb(d);
            app.addAppSlb(e);
            queryVirtualServer(d.getSlbName(), d.getSlbVirtualServerName(), e);
        }
    }

    private void queryVirtualServer(String slbName, String slbVirtualServerName, AppSlb appSlb) throws DalException {
        SlbDo slbDo = slbDao.findByName(slbName, SlbEntity.READSET_FULL);
        SlbVirtualServerDo d = slbVirtualServerDao.findAllBySlbAndName(slbDo.getId(), slbVirtualServerName, SlbVirtualServerEntity.READSET_FULL);

        appSlb.setSlbName(slbDo.getName());

        VirtualServer e = C.toVirtualServer(d);
        appSlb.setVirtualServer(e);
        querySlbDomains(d.getId(), e);
    }

    private void querySlbDomains(long slbVirtualServerId, VirtualServer virtualServer) throws DalException {
        List<SlbDomainDo> list = slbDomainDao.findAllBySlbVirtualServer(slbVirtualServerId, SlbDomainEntity.READSET_FULL);
        for (SlbDomainDo d : list) {
            Domain e = C.toDomain(d);
            virtualServer.addDomain(e);
        }
    }

    private void queryAppHealthCheck(long appKey, App app) throws DalException {
        AppHealthCheckDo d = appHealthCheckDao.findByApp(appKey, AppHealthCheckEntity.READSET_FULL);
        HealthCheck e = C.toHealthCheck(d);
        app.setHealthCheck(e);
    }

    private void queryLoadBalancingMethod(long appKey, App app) throws DalException {
        AppLoadBalancingMethodDo d = appLoadBalancingMethodDao.findByApp(appKey, AppLoadBalancingMethodEntity.READSET_FULL);
        LoadBalancingMethod e = C.toLoadBalancingMethod(d);
        app.setLoadBalancingMethod(e);
    }

    private void queryAppServers(long appKey, App app) throws DalException {
        List<AppServerDo> list = appServerDao.findAllByApp(appKey, AppServerEntity.READSET_FULL);
        for (AppServerDo d : list) {
            AppServer e = C.toAppServer(d);
            app.addAppServer(e);
        }
    }
}
