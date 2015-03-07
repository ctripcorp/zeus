package com.ctrip.zeus.service.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.SlbQuery;
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
@Component("slbQuery")
public class SlbQueryImpl implements SlbQuery {
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
    public List<Slb> getAll() throws DalException {
        List<Slb> list = new ArrayList<>();
        for (SlbDo d : slbDao.findAll(SlbEntity.READSET_FULL)) {
            Slb slb = C.toSlb(d);
            list.add(slb);

            querySlbVips(d.getId(), slb);
            querySlbServers(d.getId(), slb);
            queryVirtualServers(d.getId(), slb);

        }
        return list;
    }

    private void querySlbVips(long slbId, Slb slb) throws DalException {
        List<SlbVipDo> list = slbVipDao.findAllBySlb(slbId, SlbVipEntity.READSET_FULL);
        for (SlbVipDo d : list) {
            Vip e = C.toVip(d);
            slb.addVip(e);
        }
    }

    private void querySlbServers(long slbId, Slb slb) throws DalException {
        List<SlbServerDo> list = slbServerDao.findAllBySlb(slbId, SlbServerEntity.READSET_FULL);
        for (SlbServerDo d : list) {
            SlbServer e = C.toSlbServer(d);
            slb.addSlbServer(e);
        }
    }

    private void queryVirtualServers(long slbId, Slb slb) throws DalException {
        List<SlbVirtualServerDo> list = slbVirtualServerDao.findAllBySlb(slbId, SlbVirtualServerEntity.READSET_FULL);
        for (SlbVirtualServerDo d : list) {
            VirtualServer e = C.toVirtualServer(d);
            slb.addVirtualServer(e);
            querySlbDomains(d.getId(), e);
        }
    }

    private void querySlbDomains(long slbVirtualServerId, VirtualServer virtualServer) throws DalException {
        List<SlbDomainDo> list = slbDomainDao.findAllBySlbVirtualServer(slbVirtualServerId, SlbDomainEntity.READSET_FULL);
        for (SlbDomainDo d : list) {
            Domain e = C.toDomain(d);
            virtualServer.addDomain(e);
        }
    }
}
