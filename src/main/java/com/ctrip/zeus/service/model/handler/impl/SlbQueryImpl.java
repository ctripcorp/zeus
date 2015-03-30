package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.handler.SlbQuery;
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
    private AppSlbDao appSlbDao;
    @Resource
    private AppServerDao appServerDao;
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
    public Slb get(String slbName) throws DalException {
        SlbDo d = slbDao.findByName(slbName, SlbEntity.READSET_FULL);

        Slb slb = C.toSlb(d);
        fillData(d, slb);
        return slb;
    }

    @Override
    public Slb getById(long id) throws DalException {
        SlbDo d = slbDao.findByPK(id, SlbEntity.READSET_FULL);

        Slb slb = C.toSlb(d);
        fillData(d, slb);
        return slb;
    }

    @Override
    public Slb getBySlbServer(String slbServerIp) throws DalException {
        Slb slb = null;
        for (SlbServerDo ssd : slbServerDao.findAllByIp(slbServerIp, SlbServerEntity.READSET_FULL)) {
            slb = getById(ssd.getSlbId());
            break;
        }
        return slb;
    }

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

    @Override
    public List<Slb> getByAppServer(String appServerIp) throws DalException {
        List<AppServerDo> asvrDoList = appServerDao.findAllByIp(appServerIp, AppServerEntity.READSET_FULL);
        if (asvrDoList.size() == 0)
            return null;

        long[] appIds = new long[asvrDoList.size()];
        int i = 0;
        for (AppServerDo asd : asvrDoList) {
            appIds[i++] = asd.getAppId();
        }
        List<AppDo> adList = appDao.findAllByIds(appIds, AppEntity.READSET_FULL);
        if (adList.size() == 0)
            return null;

        String[] appNames = new String[adList.size()];
        int j = 0;
        for (AppDo ad : adList) {
            appNames[j++] = ad.getName();
        }
        List<AppSlbDo> aslbDoList = appSlbDao.findAllByApps(appNames, AppSlbEntity.READSET_FULL);
        if (aslbDoList.size() == 0)
            return null;

        List<String> slbNames = new ArrayList<>();
        for (AppSlbDo asd : aslbDoList) {
            if (slbNames.contains(asd.getSlbName()))
                continue;
            slbNames.add(asd.getSlbName());
        }
        List<Slb> list = new ArrayList<>();
        for (String sn : slbNames) {
            Slb slb = get(sn);
            if (slb == null)
                continue;
            list.add(slb);
        }
        return list;
    }

    @Override
    public List<Slb> getByAppName(String appName) throws DalException {
        List<AppSlbDo> asdList = appSlbDao.findAllByApp(appName, AppSlbEntity.READSET_FULL);
        if (asdList.size() == 0)
            return null;

        List<String> slbNames = new ArrayList<>();
        for (AppSlbDo asd : asdList) {
            if (slbNames.contains(asd.getSlbName()))
                continue;
            slbNames.add(asd.getSlbName());
        }
        List<Slb> list = new ArrayList<>();
        for (String sn : slbNames) {
            Slb slb = get(sn);
            if (slb == null)
                continue;
            list.add(slb);
        }
        return list;
    }

    @Override
    public List<Slb> getByAppServerAndAppName(String appServerIp, String appName) throws DalException {
        List<Slb> slbSet1 = getByAppName(appName);
        List<Slb> slbSet2 = getByAppServer(appServerIp);
        if (slbSet1 == null || slbSet2 == null)
            return null;
        // Intersection, get slbSet1 as the result.
        slbSet1.retainAll(slbSet2);
        return slbSet1;
    }

    @Override
    public List<String> getAppServersBySlb(String slbName) throws DalException {
        List<String> appServerIps = new ArrayList<>();
        List<AppSlbDo> aslbDoList = appSlbDao.findAllBySlb(slbName, AppSlbEntity.READSET_FULL);
        if (aslbDoList.size() == 0)
            return null;

        String[] appNames = new String[aslbDoList.size()];
        int i = 0;
        for (AppSlbDo asd : aslbDoList) {
            appNames[i++] = asd.getAppName();
        }
        List<AppDo> adList = appDao.findAllByNames(appNames, AppEntity.READSET_FULL);
        if (adList.size() == 0)
            return null;

        List<AppServerDo> asvrDoList = new ArrayList<>();
        for (AppDo ad : adList) {
            asvrDoList.addAll(appServerDao.findAllByApp(ad.getId(), AppServerEntity.READSET_FULL));
        }
        if (asvrDoList.size() == 0)
            return null;

        List<String> list = new ArrayList<>();
        for (AppServerDo asd : asvrDoList) {
            if (list.contains(asd.getIp()))
                continue;
            list.add(asd.getIp());
        }
        return list;
    }

    private void fillData(SlbDo d, Slb slb) throws DalException {
        querySlbVips(d.getId(), slb);
        querySlbServers(d.getId(), slb);
        queryVirtualServers(d.getId(), slb);
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
