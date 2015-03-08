package com.ctrip.zeus.service.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.AppQuery;
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
    private ServerDao serverDao;
    @Resource
    private SlbDao slbDao;
    @Resource
    private SlbDomainDao slbDomainDao;
    @Resource
    private SlbServerDao slbServerDao;
    @Resource
    private SlbVipDao slbVipDao;
    @Resource
    private SlbVirtualServerDao slbVirtualServerDao;


    @Override
    public App get(String name) throws DalException {
        AppDo d = appDao.findByName(name, AppEntity.READSET_FULL);
        App app = C.toApp(d);

        queryAppSlbs(d.getId(), app);
        queryAppHealthCheck(d.getId(), app);
        queryLoadBalancingMethod(d.getId(), app);
        queryAppServers(d.getId(), app);

        return app;
    }

    @Override
    public List<App> getAll() throws DalException {
        List<App> list = new ArrayList<>();
        for (AppDo d : appDao.findAll(AppEntity.READSET_FULL)) {
            App app = C.toApp(d);
            list.add(app);

            queryAppSlbs(d.getId(), app);
            queryAppHealthCheck(d.getId(), app);
            queryLoadBalancingMethod(d.getId(), app);
            queryAppServers(d.getId(), app);

        }
        return list;
    }

    @Override
    public List<App> getBy(String slbName, String virtualServerName) throws DalException {
        List<AppSlbDo> list = appSlbDao.findAllBySlbAndVirtualServer(slbName, virtualServerName, AppSlbEntity.READSET_FULL);
        StringBuilder builder = new StringBuilder("128");
        for (AppSlbDo d : list) {

        }
        return null;
    }

    private void queryAppSlbs(long appKey, App app) throws DalException {
        List<AppSlbDo> list = appSlbDao.findAllByApp(appKey, AppSlbEntity.READSET_FULL);
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

            queryServer(d.getIp(), e);
        }
    }

    private void queryServer(String ip, AppServer appServer) throws DalException {
        ServerDo d = serverDao.findByIp(ip, ServerEntity.READSET_FULL);
        Server e = C.toServer(d);
        appServer.setServer(e);
    }
}
