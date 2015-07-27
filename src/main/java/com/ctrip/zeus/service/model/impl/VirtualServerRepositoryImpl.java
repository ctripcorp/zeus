package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.Domain;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.VirtualServerRepository;
import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhoumy on 2015/7/27.
 */
@Component("virtualServerRepository")
public class VirtualServerRepositoryImpl implements VirtualServerRepository {
    @Resource
    private GroupSlbDao groupSlbDao;
    @Resource
    private SlbVirtualServerDao slbVirtualServerDao;
    @Resource
    private SlbDomainDao slbDomainDao;

    @Override
    public List<GroupVirtualServer> listGroupVsByGroups(Long[] groupIds) throws Exception {
        return batchFetch(groupSlbDao.findAllByGroups(groupIds, GroupSlbEntity.READSET_FULL));
    }

    @Override
    public List<GroupVirtualServer> listGroupVsByVsId(Long virtualServerId) throws Exception {
        return batchFetch(groupSlbDao.findAllByVirtualServer(virtualServerId, GroupSlbEntity.READSET_FULL));
    }

    @Override
    public List<GroupVirtualServer> listGroupVsBySlb(Long slbId) throws Exception {
        return batchFetch(groupSlbDao.findAllBySlb(slbId, GroupSlbEntity.READSET_FULL));
    }

    @Override
    public List<VirtualServer> listVirtualServerBySlb(Long slbId) throws Exception {
        List<VirtualServer> result = new ArrayList<>();
        for (SlbVirtualServerDo d : slbVirtualServerDao.findAllBySlb(slbId, SlbVirtualServerEntity.READSET_FULL)) {
            VirtualServer e = C.toVirtualServer(d);
            if (e == null)
                continue;
            else
                result.add(e);
            querySlbDomains(d.getId(), e);
        }
        return result;
    }

    @Override
    public VirtualServer getById(Long virtualServerId) throws Exception {
        SlbVirtualServerDo d = slbVirtualServerDao.findByPK(virtualServerId, SlbVirtualServerEntity.READSET_FULL);
        if (d == null)
            return null;
        VirtualServer vs = C.toVirtualServer(d);
        querySlbDomains(d.getId(), vs);
        return C.toVirtualServer(d);
    }

    @Override
    public VirtualServer getBySlbAndName(Long slbId, String virtualServerName) throws Exception {
        SlbVirtualServerDo d = slbVirtualServerDao.findBySlbAndName(slbId, virtualServerName, SlbVirtualServerEntity.READSET_FULL);
        if (d == null)
            return null;
        VirtualServer vs = C.toVirtualServer(d);
        querySlbDomains(d.getId(), vs);
        return C.toVirtualServer(d);
    }

    @Override
    public Long[] findGroupsByVirtualServer(Long virtualServerId) throws Exception {
        List<GroupSlbDo> l = groupSlbDao.findAllByVirtualServer(virtualServerId, GroupSlbEntity.READSET_FULL);
        Long[] result = new Long[l.size()];
        for (int i = 0; i < l.size(); i++) {
            result[i] = l.get(i).getId();
        }
        return result;
    }

    private List<GroupVirtualServer> batchFetch(List<GroupSlbDo> list) throws DalException {
        List<GroupVirtualServer> result = new ArrayList<>();
        for (GroupSlbDo groupSlbDo : list) {
            GroupVirtualServer gvs = toGroupVirtualServer(groupSlbDo);
            result.add(gvs);
            SlbVirtualServerDo svsd = slbVirtualServerDao.findByPK(groupSlbDo.getSlbVirtualServerId(), SlbVirtualServerEntity.READSET_FULL);
            if (svsd != null)
                gvs.setVirtualServer(toVirtualServer(svsd));
        }
        return result;
    }

    private void querySlbDomains(Long slbVirtualServerId, VirtualServer virtualServer) throws DalException {
        List<SlbDomainDo> list = slbDomainDao.findAllBySlbVirtualServer(slbVirtualServerId, SlbDomainEntity.READSET_FULL);
        for (SlbDomainDo d : list) {
            Domain e = C.toDomain(d);
            virtualServer.addDomain(e);
        }
    }

    private static GroupVirtualServer toGroupVirtualServer(GroupSlbDo d) {
        return new GroupVirtualServer()
                .setPath(d.getPath())
                .setRewrite(d.getRewrite())
                .setPriority(d.getPriority());
    }

    private static VirtualServer toVirtualServer(SlbVirtualServerDo d) {
        return new VirtualServer()
                .setId(d.getId())
                .setPort(d.getPort())
                .setName(d.getName())
                .setSsl(d.isIsSsl());
    }
}
