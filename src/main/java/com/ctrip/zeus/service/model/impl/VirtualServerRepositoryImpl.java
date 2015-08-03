package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Domain;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.VirtualServerRepository;
import com.ctrip.zeus.support.C;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    @Resource
    private SlbDao slbDao;

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
            if (d == null)
                continue;
            else
                result.add(createVirtualServer(d));
        }
        return result;
    }

    @Override
    public VirtualServer getById(Long virtualServerId) throws Exception {
        SlbVirtualServerDo d = slbVirtualServerDao.findByPK(virtualServerId, SlbVirtualServerEntity.READSET_FULL);
        if (d == null)
            return null;
        return createVirtualServer(d);
    }

    @Override
    public VirtualServer getBySlbAndName(Long slbId, String virtualServerName) throws Exception {
        SlbVirtualServerDo d = slbVirtualServerDao.findBySlbAndName(slbId, virtualServerName, SlbVirtualServerEntity.READSET_FULL);
        if (d == null)
            return null;
        return createVirtualServer(d);
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

    @Override
    public void addVirtualServer(Long slbId, VirtualServer virtualServer) throws Exception {
        SlbVirtualServerDo d = C.toSlbVirtualServerDo(0L, slbId, virtualServer);
        slbVirtualServerDao.insert(d);
        syncDomains(d.getId(), virtualServer.getDomains());
    }

    @Override
    public void updateVirtualServers(VirtualServer[] virtualServers) throws Exception {
        for (VirtualServer virtualServer : virtualServers) {
            if (virtualServer.getId() == null || virtualServer.getId().longValue() <= 0L)
                throw new ValidationException("Invalid virtual server id.");
            SlbVirtualServerDo d = C.toSlbVirtualServerDo(virtualServer.getId(), virtualServer.getSlbId(), virtualServer);
            slbVirtualServerDao.insertOrUpdate(d);
            syncDomains(d.getId(), virtualServer.getDomains());
        }
    }

    @Override
    public void deleteVirtualServer(Long virtualServerId) throws Exception {
        slbDomainDao.deleteAllBySlbVirtualServer(new SlbDomainDo().setSlbVirtualServerId(virtualServerId));
        slbVirtualServerDao.deleteByPK(new SlbVirtualServerDo().setId(virtualServerId));
    }

    @Override
    public void batchDeleteVirtualServers(Long slbId) throws Exception {
        for (SlbVirtualServerDo slbVirtualServerDo : slbVirtualServerDao.findAllBySlb(slbId, SlbVirtualServerEntity.READSET_FULL)) {
            deleteVirtualServer(slbVirtualServerDo.getId());
        }
    }

    @Override
    public void batchDeleteGroupVirtualServers(Long groupId) throws Exception {
        groupSlbDao.deleteByGroup(new GroupSlbDo().setGroupId(groupId));
    }

    @Override
    public void updateGroupVirtualServers(Long groupId, List<GroupVirtualServer> groupVirtualServers) throws Exception {
        List<GroupSlbDo> originServers = groupSlbDao.findAllByGroup(groupId, GroupSlbEntity.READSET_FULL);
        Map<Long, GroupSlbDo> uniqueCheck = Maps.uniqueIndex(
                originServers, new Function<GroupSlbDo, Long>() {
                    @Override
                    public Long apply(GroupSlbDo input) {
                        return input.getSlbVirtualServerId();
                    }
                });
        for (GroupVirtualServer groupVirtualServer : groupVirtualServers) {
            GroupSlbDo originServer = uniqueCheck.get(groupVirtualServer.getVirtualServer().getId());
            if (originServer != null)
                originServers.remove(originServer);
            SlbVirtualServerDo d = slbVirtualServerDao.findByPK(groupVirtualServer.getVirtualServer().getId(), SlbVirtualServerEntity.READSET_FULL);
            if (d == null)
                throw new ValidationException("Virtual server with id " + groupVirtualServer.getVirtualServer().getId() + " cannot be found.");
            SlbDo slb = slbDao.findById(d.getSlbId(), SlbEntity.READSET_FULL);
            if (slb == null)
                throw new ValidationException("Cannot find the corresponding slb from virtual server with id " + d.getId() + ".");
            groupVirtualServer.getVirtualServer().setSlbId(slb.getId());
            groupSlbDao.insertOrUpdate(toGroupSlbDo(groupId, groupVirtualServer));
        }
        for (GroupSlbDo d : originServers) {
            groupSlbDao.deleteByPK(d);
        }
    }

    private List<GroupVirtualServer> batchFetch(List<GroupSlbDo> list) throws DalException {
        List<GroupVirtualServer> result = new ArrayList<>();
        for (GroupSlbDo groupSlbDo : list) {
            GroupVirtualServer gvs = toGroupVirtualServer(groupSlbDo);
            result.add(gvs);
            SlbVirtualServerDo svsd = slbVirtualServerDao.findByPK(groupSlbDo.getSlbVirtualServerId(), SlbVirtualServerEntity.READSET_FULL);
            if (svsd != null)
                gvs.setVirtualServer(createVirtualServer(svsd));
        }
        return result;
    }

    private VirtualServer createVirtualServer(SlbVirtualServerDo d) throws DalException {
        VirtualServer vs = C.toVirtualServer(d);
        querySlbDomains(d.getId(), vs);
        return vs;
    }

    private void syncDomains(Long slbVirtualServerId, List<Domain> domains) throws DalException {
        List<SlbDomainDo> originDomains = slbDomainDao.findAllBySlbVirtualServer(slbVirtualServerId, SlbDomainEntity.READSET_FULL);
        Map<String, SlbDomainDo> uniqueCheck = Maps.uniqueIndex(
                originDomains, new Function<SlbDomainDo, String>() {
                    @Override
                    public String apply(SlbDomainDo input) {
                        return input.getName();
                    }
                });
        for (Domain domain : domains) {
            SlbDomainDo originDomain = uniqueCheck.get(domain.getName());
            if (originDomain != null) {
                originDomains.remove(originDomain);
                continue;
            }
            slbDomainDao.insert(new SlbDomainDo().setSlbVirtualServerId(slbVirtualServerId).setName(domain.getName()));
        }
        for (SlbDomainDo d : originDomains) {
            slbDomainDao.deleteByPK(d);
        }
    }

    private void querySlbDomains(Long slbVirtualServerId, VirtualServer virtualServer) throws DalException {
        List<SlbDomainDo> list = slbDomainDao.findAllBySlbVirtualServer(slbVirtualServerId, SlbDomainEntity.READSET_FULL);
        for (SlbDomainDo d : list) {
            virtualServer.addDomain(new Domain().setName(d.getName()));
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

    private static GroupSlbDo toGroupSlbDo(Long groupId, GroupVirtualServer groupVirtualServer) {
        VirtualServer vs = groupVirtualServer.getVirtualServer();
        return new GroupSlbDo()
                .setGroupId(groupId)
                .setSlbId(vs.getSlbId())
                .setSlbVirtualServerId(vs.getId())
                .setPath(groupVirtualServer.getPath())
                .setRewrite(groupVirtualServer.getRewrite())
                .setPriority(groupVirtualServer.getPriority() == null ? 1000 : groupVirtualServer.getPriority());
    }
}
