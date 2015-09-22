package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Domain;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.VirtualServerRepository;
import com.ctrip.zeus.service.model.handler.GroupValidator;
import com.ctrip.zeus.service.model.handler.SlbValidator;
import com.ctrip.zeus.service.model.handler.impl.ContentReaders;
import com.ctrip.zeus.service.model.handler.impl.VirtualServerEntityManager;
import com.ctrip.zeus.service.query.ArchiveCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.support.C;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2015/7/27.
 */
@Component("virtualServerRepository")
public class VirtualServerRepositoryImpl implements VirtualServerRepository {
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    private VirtualServerEntityManager virtualServerEntityManager;
    @Resource
    private MVsContentDao mVsContentDao;
    @Resource
    private GroupSlbDao groupSlbDao;
    @Resource
    private SlbVirtualServerDao slbVirtualServerDao;
    @Resource
    private SlbDomainDao slbDomainDao;
    @Resource
    private SlbDao slbDao;
    @Resource
    private SlbValidator slbModelValidator;
    @Resource
    private GroupValidator groupModelValidator;

    @Override
    public List<GroupVirtualServer> listGroupVsByGroups(Long[] groupIds) throws Exception {
        return batchFetch(groupSlbDao.findAllByGroups(groupIds, GroupSlbEntity.READSET_FULL));
    }

    @Override
    public List<VirtualServer> listAll(Long[] vsIds) throws Exception {
        List<VirtualServer> result = new ArrayList<>();
        for (MetaVsContentDo metaVsContentDo : mVsContentDao.findAllByIds(vsIds, MVsContentEntity.READSET_FULL)) {
            result.add(ContentReaders.readVirtualServerContent(metaVsContentDo.getContent()));
        }
        return result;
    }

    @Override
    public VirtualServer getById(Long virtualServerId) throws Exception {
        MetaVsContentDo d = mVsContentDao.findById(virtualServerId, MVsContentEntity.READSET_FULL);
        return d == null ? null : ContentReaders.readVirtualServerContent(d.getContent());
    }

    @Override
    public VirtualServer addVirtualServer(Long slbId, VirtualServer virtualServer) throws Exception {
        virtualServer.setSlbId(slbId);

        Set<Long> checkIds = virtualServerCriteriaQuery.queryBySlbId(virtualServer.getSlbId());
        List<VirtualServer> check = listAll(checkIds.toArray(new Long[checkIds.size()]));
        check.add(virtualServer);
        slbModelValidator.validateVirtualServer(check.toArray(new VirtualServer[check.size()]));
        virtualServerEntityManager.addVirtualServer(virtualServer);
        return virtualServer;
    }

    @Override
    public void updateVirtualServer(VirtualServer virtualServer) throws Exception {
        if (virtualServer.getId() == null || virtualServer.getId().longValue() <= 0L)
            throw new ValidationException("Invalid virtual server id.");
        Set<Long> checkIds = virtualServerCriteriaQuery.queryBySlbId(virtualServer.getSlbId());
        Map<Long, VirtualServer> check = new HashMap<>();
        for (VirtualServer vs : listAll(checkIds.toArray(new Long[checkIds.size()]))) {
            check.put(vs.getId(), vs);
        }
        if (!check.keySet().contains(virtualServer.getId()))
            throw new ValidationException("Virtual server doesn't exist, please new one first.");
        check.put(virtualServer.getId(), virtualServer);
        slbModelValidator.validateVirtualServer(check.values().toArray(new VirtualServer[check.size()]));
        virtualServerEntityManager.updateVirtualServer(virtualServer);
    }

    @Override
    public void deleteVirtualServer(Long virtualServerId) throws Exception {
        slbModelValidator.checkVirtualServerDependencies(new VirtualServer[]{getById(virtualServerId)});
        virtualServerEntityManager.deleteVirtualServer(virtualServerId);
    }

    @Override
    public void batchDeleteVirtualServers(Long slbId) throws Exception {
        //TODO validation?
        Set<Long> vsIds = virtualServerCriteriaQuery.queryBySlbId(slbId);
        virtualServerEntityManager.deleteVirtualServers(vsIds.toArray(new Long[vsIds.size()]));
    }

    @Override
    public void batchDeleteGroupVirtualServers(Long groupId) throws Exception {
        groupSlbDao.deleteByGroup(new GroupSlbDo().setGroupId(groupId));
    }

    @Override
    public void updateGroupVirtualServers(Long groupId, List<GroupVirtualServer> groupVirtualServers) throws Exception {
        groupModelValidator.validateGroupVirtualServers(groupId, groupVirtualServers);
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

    @Override
    public List<Long> portVirtualServerRel() throws Exception {
        List<SlbVirtualServerDo> l = slbVirtualServerDao.findAll(SlbVirtualServerEntity.READSET_FULL);
        VirtualServer[] vses = new VirtualServer[l.size()];
        for (int i = 0; i < l.size(); i++) {
            vses[i] = createVirtualServer(l.get(i));
        }
        return virtualServerEntityManager.port(vses);
    }

    @Override
    public void portVirtualServerRel(Long vsId) throws Exception {
        SlbVirtualServerDo d = slbVirtualServerDao.findByPK(vsId, SlbVirtualServerEntity.READSET_FULL);
        VirtualServer vs = createVirtualServer(d);
        virtualServerEntityManager.port(vs);
    }

    private List<GroupVirtualServer> batchFetch(List<GroupSlbDo> list) throws Exception {
        List<GroupVirtualServer> result = new ArrayList<>();
        for (GroupSlbDo groupSlbDo : list) {
            GroupVirtualServer gvs = toGroupVirtualServer(groupSlbDo);
            result.add(gvs);
            gvs.setVirtualServer(getById(groupSlbDo.getSlbVirtualServerId()));
        }
        return result;
    }

    private VirtualServer createVirtualServer(SlbVirtualServerDo d) throws DalException {
        VirtualServer vs = C.toVirtualServer(d);
        querySlbDomains(d.getId(), vs);
        return vs;
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
