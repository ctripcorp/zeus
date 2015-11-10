package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
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
    private ArchiveVsDao archiveVsDao;

    // TODO remove m_vs_content table
    @Resource
    private MVsContentDao mVsContentDao;


    @Override
    public void addVirtualServer(VirtualServer virtualServer) throws Exception {
        virtualServer.setVersion(1);
        SlbVirtualServerDo d = C.toSlbVirtualServerDo(0L, virtualServer.getSlbId(), virtualServer);
        slbVirtualServerDao.insert(d);
        Long vsId = d.getId();
        virtualServer.setId(vsId);
        archiveVsDao.insert(new MetaVsArchiveDo().setVsId(vsId).setContent(ContentWriters.writeVirtualServerContent(virtualServer)).setVersion(virtualServer.getVersion()));
        rVsSlbDao.insert(new RelVsSlbDo().setVsId(vsId).setSlbId(virtualServer.getSlbId()));
        relSyncDomain(vsId, virtualServer.getDomains());
    }

    @Override
    public void updateVirtualServer(VirtualServer virtualServer) throws Exception {
        Long vsId = virtualServer.getId();
        MetaVsArchiveDo check = archiveVsDao.findMaxVersionByVs(vsId, ArchiveVsEntity.READSET_FULL);
        if (check.getVersion() > virtualServer.getVersion())
            throw new ValidationException("Newer virtual server version is detected.");
        virtualServer.setVersion(virtualServer.getVersion() + 1);

        SlbVirtualServerDo d = C.toSlbVirtualServerDo(vsId, virtualServer.getSlbId(), virtualServer);
        slbVirtualServerDao.updateByPK(d, SlbVirtualServerEntity.UPDATESET_FULL);
        archiveVsDao.insert(new MetaVsArchiveDo().setVsId(vsId).setContent(ContentWriters.writeVirtualServerContent(virtualServer)).setVersion(virtualServer.getVersion()));
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
        archiveVsDao.deleteAllByVsId(new MetaVsArchiveDo().setVsId(vsId));
    }

    @Override
    public void deleteVirtualServers(Long[] vsIds) throws Exception {
        int size = vsIds.length;
        RelVsSlbDo[] relSlbs = new RelVsSlbDo[size];
        RelVsDomainDo[] relDomains = new RelVsDomainDo[size];
        SlbVirtualServerDo[] vses = new SlbVirtualServerDo[size];
        MetaVsArchiveDo[] archs = new MetaVsArchiveDo[size];
        for (int i = 0; i < size; i++) {
            relSlbs[i] = new RelVsSlbDo().setVsId(vsIds[i]);
            relDomains[i] = new RelVsDomainDo().setVsId(vsIds[i]);
            vses[i] = new SlbVirtualServerDo().setId(vsIds[i]);
            archs[i] = new MetaVsArchiveDo().setVsId(vsIds[i]);
        }
        rVsSlbDao.deleteByVs(relSlbs);
        rVsDomainDao.deleteAllByVs(relDomains);
        slbVirtualServerDao.deleteById(vses);
        archiveVsDao.deleteAllByVsId(archs);
    }

    @Override
    public List<Long> port(Long[] vsIds) {
        List<Long> fails = new ArrayList<>();
        for (Long vsId : vsIds) {
            try {
                port(vsId);
            } catch (Exception ex) {
                fails.add(vsId);
            }
        }
        return fails;
    }

    @Override
    public void port(Long vsId) throws Exception {
        MetaVsContentDo orig = mVsContentDao.findById(vsId, MVsContentEntity.READSET_FULL);
        if (orig == null)
            throw new ValidationException("No previous content has found.");
        VirtualServer vs = ContentReaders.readVirtualServerContent(orig.getContent());
        vs.setVersion(1);
        MetaVsArchiveDo arch = new MetaVsArchiveDo().setVsId(orig.getVsId()).setContent(ContentWriters.writeVirtualServerContent(vs)).setVersion(1);
        archiveVsDao.insertOrUpdate(arch);
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
