package com.ctrip.zeus.service.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.service.DbClean;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
@Component("dbClean")
public class DbCleanImpl implements DbClean {
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
    public void deleteSlbVip(long id) throws DalException {
        slbVipDao.deleteByPK(new SlbVipDo().setId(id));
    }

    @Override
    public void deleteSlbServer(long id) throws DalException {
        slbServerDao.deleteByPK(new SlbServerDo().setId(id));
    }

    @Override
    public void deleteSlbVirtualServer(long id) throws DalException {
        slbDomainDao.deleteAllBySlbVirtualServer(new SlbDomainDo().setSlbVirtualServerId(id));
        slbVirtualServerDao.deleteByPK(new SlbVirtualServerDo().setId(id));
    }

    @Override
    public void deleteSlbDomain(long id) throws DalException {
        slbDomainDao.deleteByPK(new SlbDomainDo().setId(id));
    }

    @Override
    public void deleteAppSlb(long id) throws DalException {
        appSlbDao.deleteByPK(new AppSlbDo().setId(id));
    }

    @Override
    public void deleteAppServer(long id) throws DalException {
        appServerDao.deleteByPK(new AppServerDo().setId(id));
    }
}
