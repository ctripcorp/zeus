package com.ctrip.zeus.service.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.Server;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.service.DbSync;
import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
@Component("dbSync")
public class DbSyncImpl implements DbSync {
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
    public SlbDo sync(Slb slb) throws DalException {
        SlbDo d = C.toSlbDo(slb);
        d.setCreatedTime(new Date());
        slbDao.insert(d);
        return null;
    }

    @Override
    public AppDo sync(App app) {
        return null;
    }

    @Override
    public ServerDo sync(Server server) {
        return null;
    }
}
