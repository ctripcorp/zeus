package com.ctrip.zeus.service.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.DbClean;
import com.ctrip.zeus.service.SlbSync;
import com.ctrip.zeus.support.C;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
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
@Component("dbSync")
public class SlbSyncImpl implements SlbSync {
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

    @Resource
    private DbClean dbClean;

    @Override
    public SlbDo sync(Slb slb) throws DalException {
        SlbDo d = C.toSlbDo(slb);
        d.setCreatedTime(new Date());
        slbDao.insert(d);

        syncSlbVips(d.getId(), slb.getVips());
        syncSlbServers(d.getId(), slb.getSlbServers());
        syncVirtualServers(d.getId(), slb.getVirtualServers());

        return d;
    }

    private void syncSlbVips(long slbId, List<Vip> vips) throws DalException {
        List<SlbVipDo> oldList = slbVipDao.findAllBySlb(slbId,SlbVipEntity.READSET_FULL);
        Map<String, SlbVipDo> oldMap = Maps.uniqueIndex(oldList, new Function<SlbVipDo, String>() {
            @Override
            public String apply(SlbVipDo input) {
                return input.getIp();
            }
        });

        //Update existed if necessary, and insert new ones.
        for (Vip e : vips) {
            SlbVipDo old = oldMap.get(e.getIp());
            if (old != null) {
                oldList.remove(old);
            }
            slbVipDao.insert(C.toSlbVipDo(e).setSlbId(slbId).setCreatedTime(new Date()));
        }

        //Remove unused ones.
        for (SlbVipDo d : oldList) {
            dbClean.deleteSlbVip(d.getId());
        }
    }

    private void syncSlbServers(long slbId, List<SlbServer> slbServers) throws DalException {
        List<SlbServerDo> oldList = slbServerDao.findAllBySlb(slbId,SlbServerEntity.READSET_FULL);
        Map<String, SlbServerDo> oldMap = Maps.uniqueIndex(oldList, new Function<SlbServerDo, String>() {
            @Override
            public String apply(SlbServerDo input) {
                return input.getIp();
            }
        });

        //Update existed if necessary, and insert new ones.
        for (SlbServer e : slbServers) {
            SlbServerDo old = oldMap.get(e.getIp());
            if (old != null) {
                oldList.remove(old);
            }
            slbServerDao.insert(C.toSlbServerDo(e).setSlbId(slbId).setCreatedTime(new Date()));
        }

        //Remove unused ones.
        for (SlbServerDo d : oldList) {
            dbClean.deleteSlbServer(d.getId());
        }
    }

    private void syncVirtualServers(long slbId, List<VirtualServer> virtualServers) throws DalException {
        List<SlbVirtualServerDo> oldList = slbVirtualServerDao.findAllBySlb(slbId,SlbVirtualServerEntity.READSET_FULL);
        Map<String, SlbVirtualServerDo> oldMap = Maps.uniqueIndex(oldList, new Function<SlbVirtualServerDo, String>() {
            @Override
            public String apply(SlbVirtualServerDo input) {
                return input.getName();
            }
        });

        //Update existed if necessary, and insert new ones.
        for (VirtualServer e : virtualServers) {
            SlbVirtualServerDo old = oldMap.get(e.getName());
            if (old != null) {
                oldList.remove(old);
            }
            SlbVirtualServerDo d = C.toSlbVirtualServerDo(e).setSlbId(slbId).setCreatedTime(new Date());
            slbVirtualServerDao.insert(d);

            //Domain
            syncSlbDomain(d.getId(), e.getDomains());
        }

        //Remove unused ones.
        for (SlbVirtualServerDo d : oldList) {
            dbClean.deleteSlbVirtualServer(d.getId());
        }
    }

    private void syncSlbDomain(long slbVirtualServerId, List<Domain> domains) throws DalException {
        List<SlbDomainDo> oldList = slbDomainDao.findAllBySlbVirtualServer(slbVirtualServerId, SlbDomainEntity.READSET_FULL);
        Map<String, SlbDomainDo> oldMap = Maps.uniqueIndex(oldList, new Function<SlbDomainDo, String>() {
            @Override
            public String apply(SlbDomainDo input) {
                return input.getName() + input.getPort();
            }
        });

        //Update existed if necessary, and insert new ones.
        for (Domain e : domains) {
            SlbDomainDo old = oldMap.get(e.getName() + e.getPort());
            if (old != null) {
                oldList.remove(old);
            }
            slbDomainDao.insert(C.toSlbDomainDo(e).setSlbVirtualServerId(slbVirtualServerId).setCreatedTime(new Date()));
        }

        //Remove unused ones.
        for (SlbDomainDo d : oldList) {
            dbClean.deleteSlbDomain(d.getId());
        }
    }
}
