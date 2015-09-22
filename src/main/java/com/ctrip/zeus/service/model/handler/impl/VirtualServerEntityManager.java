package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.Domain;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.handler.VirtualServerSync;
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
 * Created by zhoumy on 2015/9/22.
 */
@Component("virtualServerEntityManager")
public class VirtualServerEntityManager implements VirtualServerSync {
    @Resource
    private SlbVirtualServerDao slbVirtualServerDao;
    @Resource
    private RVsDomainDao rVsDomainDao;
    @Resource
    private RVsSlbDao rVsSlbDao;
    @Resource
    private MVsContentDao mVsContentDao;

    @Override
    public void addVirtualServer(VirtualServer virtualServer) throws Exception {
        SlbVirtualServerDo d = C.toSlbVirtualServerDo(0L, virtualServer.getSlbId(), virtualServer);
        slbVirtualServerDao.insert(d);
        Long vsId = d.getId();
        virtualServer.setId(vsId);
        mVsContentDao.insertOrUpdate(new MetaVsContentDo().setVsId(vsId).setContent(ContentWriters.writeVirtualServerContent(virtualServer)));

        rVsSlbDao.insert(new RelVsSlbDo().setVsId(vsId).setSlbId(virtualServer.getSlbId()));
        relSyncDomain(vsId, virtualServer.getDomains());
    }

    @Override
    public void updateVirtualServer(VirtualServer virtualServer) throws Exception {
        Long vsId = virtualServer.getId();
        SlbVirtualServerDo d = C.toSlbVirtualServerDo(vsId, virtualServer.getSlbId(), virtualServer);
        slbVirtualServerDao.insertOrUpdate(d);
        mVsContentDao.insertOrUpdate(new MetaVsContentDo().setVsId(vsId).setContent(ContentWriters.writeVirtualServerContent(virtualServer)));

        if (rVsSlbDao.findSlbByVs(virtualServer.getId(), RVsSlbEntity.READSET_FULL).getSlbId() != virtualServer.getSlbId().longValue()) {
            RelVsSlbDo rel = new RelVsSlbDo().setVsId(vsId).setSlbId(virtualServer.getSlbId());
            rVsSlbDao.deleteByVs(rel);
            rVsSlbDao.insert(rel);
        }
        relSyncDomain(vsId, virtualServer.getDomains());
    }

    @Override
    public void deleteVirtualServer(Long vsId) throws Exception {
        rVsSlbDao.deleteByVs(new RelVsSlbDo().setVsId(vsId));
        rVsDomainDao.deleteAllByVs(new RelVsDomainDo().setVsId(vsId));
        slbVirtualServerDao.deleteByPK(new SlbVirtualServerDo().setId(vsId));
    }

    @Override
    public void deleteVirtualServers(Long[] vsIds) throws Exception {
        int size = vsIds.length;
        RelVsSlbDo[] relSlbs = new RelVsSlbDo[size];
        RelVsDomainDo[] relDomains = new RelVsDomainDo[size];
        SlbVirtualServerDo[] vses = new SlbVirtualServerDo[size];
        for (int i = 0; i < size; i++) {
            relSlbs[i] = new RelVsSlbDo().setVsId(vsIds[i]);
            relDomains[i] = new RelVsDomainDo().setVsId(vsIds[i]);
            vses[i] = new SlbVirtualServerDo().setId(vsIds[i]);
        }
        rVsSlbDao.deleteByVs(relSlbs);
        rVsDomainDao.deleteAllByVs(relDomains);
        slbVirtualServerDao.deleteById(vses);
    }

    @Override
    public List<Long> port(VirtualServer[] vses) {
        List<Long> fails = new ArrayList<>();
        for (VirtualServer vs : vses) {
            try {
                mVsContentDao.insertOrUpdate(new MetaVsContentDo().setVsId(vs.getId()).setContent(ContentWriters.writeVirtualServerContent(vs)));
                rVsSlbDao.insert(new RelVsSlbDo().setVsId(vs.getId()).setSlbId(vs.getSlbId()));
                relSyncDomain(vs.getId(), vs.getDomains());
            } catch (Exception ex) {
                fails.add(vs.getId());
            }
        }
        return fails;
    }

    @Override
    public void port(VirtualServer vs) throws Exception {
        mVsContentDao.insertOrUpdate(new MetaVsContentDo().setVsId(vs.getId()).setContent(ContentWriters.writeVirtualServerContent(vs)));
        RelVsSlbDo d = new RelVsSlbDo().setVsId(vs.getId()).setSlbId(vs.getSlbId());
        rVsSlbDao.deleteByVs(d);
        rVsSlbDao.insert(d);
        relSyncDomain(vs.getId(), vs.getDomains());
    }

    private void relSyncDomain(Long vsId, List<Domain> domains) throws DalException {
        // add/remove domains
        List<RelVsDomainDo> originDomains = rVsDomainDao.findAllDomainsByVses(new Long[]{vsId}, RVsDomainEntity.READSET_FULL);
        Map<String, RelVsDomainDo> uniqueCheck = Maps.uniqueIndex(
                originDomains, new Function<RelVsDomainDo, String>() {
                    @Override
                    public String apply(RelVsDomainDo input) {
                        return input.getDomain();
                    }
                });
        for (Domain domain : domains) {
            RelVsDomainDo originDomain = uniqueCheck.get(domain.getName());
            if (originDomain != null) {
                originDomains.remove(originDomain);
                continue;
            }
            rVsDomainDao.insert(new RelVsDomainDo().setVsId(vsId).setDomain(domain.getName()));
        }
        for (RelVsDomainDo rel : originDomains) {
            rVsDomainDao.deleteByVsAndDomain(rel);
        }
    }

}
