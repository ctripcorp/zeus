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
    private GroupDao groupDao;
    @Resource
    private GroupSlbDao groupSlbDao;
    @Resource
    private GroupServerDao groupServerDao;
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
        return createSlb(d);
    }

    @Override
    public Slb getById(long id) throws DalException {
        SlbDo d = slbDao.findByPK(id, SlbEntity.READSET_FULL);
        return createSlb(d);
    }

    @Override
    public Slb getBySlbServer(String slbServerIp) throws DalException {
        List<SlbServerDo> list = slbServerDao.findAllByIp(slbServerIp, SlbServerEntity.READSET_FULL);
        if (list.size() == 0)
            return null;
        return getById(list.get(0).getSlbId());
    }

    @Override
    public List<Slb> getAll() throws DalException {
        List<Slb> list = new ArrayList<>();
        for (SlbDo d : slbDao.findAll(SlbEntity.READSET_FULL)) {
            Slb slb = createSlb(d);
            if (slb != null)
                list.add(slb);
        }
        return list;
    }

    @Override
    public List<Slb> getByGroupServer(String groupServerIp) throws DalException {
        List<GroupServerDo> asvrDoList = groupServerDao.findAllByIp(groupServerIp, GroupServerEntity.READSET_FULL);
        if (asvrDoList.size() == 0)
            return null;

        long[] groupIds = new long[asvrDoList.size()];
        int i = 0;
        for (GroupServerDo asd : asvrDoList) {
            groupIds[i++] = asd.getGroupId();
        }
        List<GroupDo> adList = groupDao.findAllByIds(groupIds, GroupEntity.READSET_FULL);
        if (adList.size() == 0)
            return null;

        String[] groupNames = new String[adList.size()];
        int j = 0;
        for (GroupDo ad : adList) {
            groupNames[j++] = ad.getName();
        }
        List<GroupSlbDo> aslbDoList = groupSlbDao.findAllByGroups(groupNames, GroupSlbEntity.READSET_FULL);
        if (aslbDoList.size() == 0)
            return null;

        List<String> slbNames = new ArrayList<>();
        for (GroupSlbDo asd : aslbDoList) {
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
    public List<Slb> getByGroupNames(String[] groupNames) throws DalException {
        List<GroupSlbDo> asdList = groupSlbDao.findAllByGroups(groupNames, GroupSlbEntity.READSET_FULL);
        if (asdList.size() == 0)
            return null;

        List<String> slbNames = new ArrayList<>();
        for (GroupSlbDo asd : asdList) {
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
    public List<Slb> getByGroupServerAndGroupName(String groupServerIp, String groupName) throws DalException {
        if ((groupServerIp == null || groupServerIp.isEmpty())
            && (groupName == null || groupName.isEmpty())) {
            return null;
        }
        if (groupServerIp == null || groupServerIp.isEmpty())
            return getByGroupNames(new String[]{groupName});
        if (groupName == null || groupName.isEmpty())
            return getByGroupServer(groupServerIp);

        List<Slb> slbSet1 = getByGroupNames(new String[]{groupName});
        List<Slb> slbSet2 = getByGroupServer(groupServerIp);
        if (slbSet1 == null || slbSet2 == null)
            return null;
        // Intersection, get slbSet1 as the result.
        slbSet1.retainAll(slbSet2);
        return slbSet1;
    }

    @Override
    public List<String> getGroupServersBySlb(String slbName) throws DalException {
        List<GroupSlbDo> aslbDoList = groupSlbDao.findAllBySlb(slbName, GroupSlbEntity.READSET_FULL);
        if (aslbDoList.size() == 0)
            return null;

        String[] groupNames = new String[aslbDoList.size()];
        int i = 0;
        for (GroupSlbDo asd : aslbDoList) {
            groupNames[i++] = asd.getGroupName();
        }
        List<GroupDo> adList = groupDao.findAllByNames(groupNames, GroupEntity.READSET_FULL);
        if (adList.size() == 0)
            return null;

        List<GroupServerDo> asvrDoList = new ArrayList<>();
        for (GroupDo ad : adList) {
            asvrDoList.addAll(groupServerDao.findAllByGroup(ad.getId(), GroupServerEntity.READSET_FULL));
        }
        if (asvrDoList.size() == 0)
            return null;

        List<String> list = new ArrayList<>();
        for (GroupServerDo asd : asvrDoList) {
            if (list.contains(asd.getIp()))
                continue;
            list.add(asd.getIp());
        }
        return list;
    }

    @Override
    public List<GroupSlb> getGroupSlbsByGroups(String[] groupNames) throws DalException {
        List<GroupSlb> list = new ArrayList<>();
        for (GroupSlbDo asd : groupSlbDao.findAllByGroups(groupNames, GroupSlbEntity.READSET_FULL)) {
            GroupSlb as = C.toGroupSlb(asd);
            list.add(as);
            SlbDo sd = slbDao.findByName(as.getSlbName(), SlbEntity.READSET_FULL);
            SlbVirtualServerDo svsd = slbVirtualServerDao.findAllBySlbAndName(sd.getId(), asd.getSlbVirtualServerName(), SlbVirtualServerEntity.READSET_FULL);
            if (svsd != null)
                as.setVirtualServer(C.toVirtualServer(svsd));
            querySlbVips(sd.getId(), as);
        }
        return list;
    }

    @Override
    public List<GroupSlb> getGroupSlbsBySlb(String slbName) throws DalException {
        List<GroupSlb> list = new ArrayList<>();
        for (GroupSlbDo asd : groupSlbDao.findAllBySlb(slbName, GroupSlbEntity.READSET_FULL)) {
            GroupSlb as = C.toGroupSlb(asd);
            list.add(as);
            SlbDo sd = slbDao.findByName(slbName, SlbEntity.READSET_FULL);
            SlbVirtualServerDo svsd = slbVirtualServerDao.findAllBySlbAndName(sd.getId(), asd.getSlbVirtualServerName(), SlbVirtualServerEntity.READSET_FULL);
            if (svsd != null)
                as.setVirtualServer(C.toVirtualServer(svsd));
            querySlbVips(sd.getId(), as);
        }
        return list;
    }

    private Slb createSlb(SlbDo d) throws DalException {
        if (d == null)
            return null;
        if (d.getName() == null || d.getName().isEmpty())
            return null;
        Slb slb = C.toSlb(d);
        cascadeQuery(d, slb);
        return slb;
    }

    private void cascadeQuery(SlbDo d, Slb slb) throws DalException {
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

    private void querySlbVips(long slbId, GroupSlb groupSlb) throws DalException {
        List<SlbVipDo> list = slbVipDao.findAllBySlb(slbId, SlbVipEntity.READSET_FULL);
        for (SlbVipDo d : list) {
            Vip e = C.toVip(d);
            groupSlb.addVip(e);
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
