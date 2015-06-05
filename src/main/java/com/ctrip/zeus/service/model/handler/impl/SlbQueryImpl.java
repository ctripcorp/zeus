package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.handler.SlbQuery;
import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
@Component("slbQuery")
public class SlbQueryImpl implements SlbQuery {
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
    public Slb getById(Long id) throws DalException {
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
    public VirtualServer getBySlbAndName(String slbName, String virtualServerName) throws DalException {
        SlbDo d = slbDao.findByName(slbName, SlbEntity.READSET_FULL);
        if (d ==  null || d.getId() == 0)
            return null;
        SlbVirtualServerDo vsd = slbVirtualServerDao.findBySlbAndName(d.getId(), virtualServerName, SlbVirtualServerEntity.READSET_FULL);
        return C.toVirtualServer(vsd);
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
        List<GroupServerDo> gslist = groupServerDao.findAllByIp(groupServerIp, GroupServerEntity.READSET_FULL);
        if (gslist.size() == 0)
            return null;

        Long[] groupIds = new Long[gslist.size()];
        int i = 0;
        for (GroupServerDo gsd : gslist) {
            groupIds[i++] = gsd.getGroupId();
        }
        List<GroupSlbDo> list = groupSlbDao.findAllByGroups(groupIds, GroupSlbEntity.READSET_FULL);
        return getAllByGroupSlbs(list);
    }

    @Override
    public List<Slb> getByGroups(Long[] groupIds) throws DalException {
        List<GroupSlbDo> list = groupSlbDao.findAllByGroups(groupIds, GroupSlbEntity.READSET_FULL);
        return getAllByGroupSlbs(list);
    }

    @Override
    public List<Slb> getByGroupServerAndGroup(String groupServerIp, Long groupId) throws DalException {
        List<Slb> slbSet1 = getByGroups(new Long[] {groupId});
        List<Slb> slbSet2 = getByGroupServer(groupServerIp);
        if (slbSet1 == null || slbSet2 == null)
            return null;
        // Intersection, get slbSet1 as the result.
        slbSet1.retainAll(slbSet2);
        return slbSet1;
    }

    @Override
    public List<String> getGroupServersBySlb(String slbName) throws DalException {
        Long slbId = slbDao.findByName(slbName, SlbEntity.READSET_FULL).getId();
        List<GroupSlbDo> gslbDoList = groupSlbDao.findAllBySlb(slbId, GroupSlbEntity.READSET_FULL);
        if (gslbDoList.size() == 0)
            return null;

        List<GroupServerDo> asvrDoList = new ArrayList<>();
        for (GroupSlbDo gsd : gslbDoList) {
            asvrDoList.addAll(groupServerDao.findAllByGroup(gsd.getGroupId(), GroupServerEntity.READSET_FULL));
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
    public List<GroupSlb> getGroupSlbsByGroups(Long[] groupIds) throws DalException {
        List<GroupSlb> list = new ArrayList<>();
        for (GroupSlbDo asd : groupSlbDao.findAllByGroups(groupIds, GroupSlbEntity.READSET_FULL)) {
            GroupSlb as = C.toGroupSlb(asd);
            list.add(as);
            SlbVirtualServerDo svsd = slbVirtualServerDao.findByPK(asd.getSlbVirtualServerId(), SlbVirtualServerEntity.READSET_FULL);
            if (svsd != null)
                as.setVirtualServer(C.toVirtualServer(svsd));
            querySlbVips(svsd.getSlbId(), as);
        }
        return list;
    }

    @Override
    public List<GroupSlb> getGroupSlbsBySlb(Long slbId) throws DalException {
        List<GroupSlb> list = new ArrayList<>();
        for (GroupSlbDo asd : groupSlbDao.findAllBySlb(slbId, GroupSlbEntity.READSET_FULL)) {
            GroupSlb as = C.toGroupSlb(asd);
            list.add(as);
            SlbVirtualServerDo svsd = slbVirtualServerDao.findByPK(asd.getSlbVirtualServerId(), SlbVirtualServerEntity.READSET_FULL);
            if (svsd != null)
                as.setVirtualServer(C.toVirtualServer(svsd));
            querySlbVips(svsd.getSlbId(), as);
        }
        return list;
    }

    private List<Slb> getAllByGroupSlbs(List<GroupSlbDo> list) throws DalException {
        if (list.size() == 0)
            return null;
        Set<Long> visitedIds = new HashSet<>();
        List<Slb> l = new ArrayList<>();
        for (GroupSlbDo d : list) {
            if (visitedIds.contains(d.getSlbId()))
                continue;
            Slb slb = getById(d.getSlbId());
            visitedIds.add(d.getSlbId());
            if (slb == null)
                continue;
            l.add(slb);
        }
        return l;
    }

    private Slb createSlb(SlbDo d) throws DalException {
        if (d == null)
            return null;
        if (d.getName() == null || d.getName().isEmpty())
            return null;
        Slb slb = C.toSlb(d);
        cascadeQuery(slb);
        return slb;
    }

    private void cascadeQuery(Slb slb) throws DalException {
        querySlbVips(slb);
        querySlbServers(slb);
        queryVirtualServers(slb);
    }

    private void querySlbVips(Slb slb) throws DalException {
        List<SlbVipDo> list = slbVipDao.findAllBySlb(slb.getId(), SlbVipEntity.READSET_FULL);
        for (SlbVipDo d : list) {
            Vip e = C.toVip(d);
            slb.addVip(e);
        }
    }

    private void querySlbVips(Long slbId, GroupSlb groupSlb) throws DalException {
        List<SlbVipDo> list = slbVipDao.findAllBySlb(slbId, SlbVipEntity.READSET_FULL);
        for (SlbVipDo d : list) {
            Vip e = C.toVip(d);
            groupSlb.addVip(e);
        }
    }

    private void querySlbServers(Slb slb) throws DalException {
        List<SlbServerDo> list = slbServerDao.findAllBySlb(slb.getId(), SlbServerEntity.READSET_FULL);
        for (SlbServerDo d : list) {
            SlbServer e = C.toSlbServer(d);
            slb.addSlbServer(e);
        }
    }

    private void queryVirtualServers(Slb slb) throws DalException {
        List<SlbVirtualServerDo> list = slbVirtualServerDao.findAllBySlb(slb.getId(), SlbVirtualServerEntity.READSET_FULL);
        for (SlbVirtualServerDo d : list) {
            VirtualServer e = C.toVirtualServer(d);
            slb.addVirtualServer(e);
            querySlbDomains(d.getId(), e);
        }
    }

    private void querySlbDomains(Long slbVirtualServerId, VirtualServer virtualServer) throws DalException {
        List<SlbDomainDo> list = slbDomainDao.findAllBySlbVirtualServer(slbVirtualServerId, SlbDomainEntity.READSET_FULL);
        for (SlbDomainDo d : list) {
            Domain e = C.toDomain(d);
            virtualServer.addDomain(e);
        }
    }
}
