package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.service.model.handler.VirtualServerSync;
import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2015/9/22.
 */
@Component("virtualServerEntityManager")
public class VirtualServerEntityManager implements VirtualServerSync {
    @Resource
    private SlbVirtualServerDao slbVirtualServerDao;
    @Resource
    private VsDomainRelMaintainer vsDomainRelMaintainer;
    @Resource
    private RVsSlbDao rVsSlbDao;
    @Resource
    private ArchiveVsDao archiveVsDao;
    @Resource
    private RVsStatusDao rVsStatusDao;
    @Resource
    private ConfSlbVirtualServerActiveDao confSlbVirtualServerActiveDao;

    @Override
    public void add(VirtualServer virtualServer) throws Exception {
        virtualServer.setVersion(1);
        SlbVirtualServerDo d = C.toSlbVirtualServerDo(0L, virtualServer.getSlbId(), virtualServer);
        slbVirtualServerDao.insert(d);
        Long vsId = d.getId();
        virtualServer.setId(vsId);
        archiveVsDao.insert(new MetaVsArchiveDo().setVsId(vsId).setContent(ContentWriters.writeVirtualServerContent(virtualServer)).setVersion(virtualServer.getVersion()));

        rVsStatusDao.insert(new RelVsStatusDo().setVsId(vsId).setOfflineVersion(virtualServer.getVersion()));
        rVsSlbDao.insert(new RelVsSlbDo().setVsId(vsId).setSlbId(virtualServer.getSlbId()));
        vsDomainRelMaintainer.relAdd(virtualServer, RelVsDomainDo.class, virtualServer.getDomains());
    }

    @Override
    public void update(VirtualServer virtualServer) throws Exception {
        Long vsId = virtualServer.getId();
        MetaVsArchiveDo check = archiveVsDao.findMaxVersionByVs(vsId, ArchiveVsEntity.READSET_FULL);
        if (check.getVersion() > virtualServer.getVersion())
            throw new ValidationException("Newer virtual server version is detected.");
        virtualServer.setVersion(virtualServer.getVersion() + 1);

        SlbVirtualServerDo d = C.toSlbVirtualServerDo(vsId, virtualServer.getSlbId(), virtualServer);
        slbVirtualServerDao.updateByPK(d, SlbVirtualServerEntity.UPDATESET_FULL);
        archiveVsDao.insert(new MetaVsArchiveDo().setVsId(vsId).setContent(ContentWriters.writeVirtualServerContent(virtualServer)).setVersion(virtualServer.getVersion()));

        rVsStatusDao.insertOrUpdate(new RelVsStatusDo().setVsId(vsId).setOfflineVersion(virtualServer.getVersion()));
        RelVsStatusDo status = rVsStatusDao.findByVs(virtualServer.getId(), RVsStatusEntity.READSET_FULL);
        relUpdate(virtualServer, status.getOnlineVersion());
        vsDomainRelMaintainer.relUpdateOffline(virtualServer, RelVsDomainDo.class, virtualServer.getDomains());
    }

    @Override
    public void updateStatus(VirtualServer[] virtualServers) throws Exception {
        RelVsStatusDo[] dos = new RelVsStatusDo[virtualServers.length];
        for (int i = 0; i < dos.length; i++) {
            dos[i] = new RelVsStatusDo().setVsId(virtualServers[i].getId()).setOnlineVersion(virtualServers[i].getVersion());
        }
        rVsStatusDao.updateOnlineVersionByVs(dos, RVsStatusEntity.UPDATESET_UPDATE_ONLINE_STATUS);
        Map<Long, VirtualServer> ref = new HashMap<>();
        for (VirtualServer virtualServer : virtualServers) {
            ref.put(virtualServer.getId(), virtualServer);
        }
        List<RelVsStatusDo> check = rVsStatusDao.findByVses(ref.keySet().toArray(new Long[ref.size()]), RVsStatusEntity.READSET_FULL);
        for (RelVsStatusDo relVsStatusDo : check) {
            if (relVsStatusDo.getOnlineVersion() != relVsStatusDo.getOfflineVersion()) {
                VirtualServer vs = ref.get(relVsStatusDo.getVsId());
                RelVsStatusDo status = rVsStatusDao.findByVs(vs.getId(), RVsStatusEntity.READSET_FULL);
                relUpdate(vs, status.getOfflineVersion());
                vsDomainRelMaintainer.relUpdateOnline(vs, RelVsDomainDo.class, vs.getDomains());
            }
        }
    }

    @Override
    public void delete(Long vsId) throws Exception {
        rVsSlbDao.deleteByVs(new RelVsSlbDo().setVsId(vsId));
        vsDomainRelMaintainer.relDelete(vsId);
        rVsStatusDao.deleteAllByVs(new RelVsStatusDo().setVsId(vsId));
        slbVirtualServerDao.deleteByPK(new SlbVirtualServerDo().setId(vsId));
        archiveVsDao.deleteAllByVs(new MetaVsArchiveDo().setVsId(vsId));
    }

    private void relUpdate(VirtualServer virtualServer, int retainedVersion) throws DalException {
        List<RelVsSlbDo> orig = rVsSlbDao.findByVs(virtualServer.getId(), RVsSlbEntity.READSET_FULL);
        Iterator<RelVsSlbDo> iter = orig.iterator();
        while (iter.hasNext()) {
            RelVsSlbDo rel = iter.next();
            if (rel.getVsVersion() == retainedVersion) iter.remove();
        }
        // update existing records, if size(new) > size(old), insert the rest new records.
        if (orig.size() > 0) {
            RelVsSlbDo rel = orig.get(0);
            rel.setSlbId(virtualServer.getSlbId()).setVsVersion(virtualServer.getVersion());
            rVsSlbDao.update(rel, RVsSlbEntity.UPDATESET_FULL);
            // remove the rest old records
            if (orig.size() > 1) {
                rVsSlbDao.delete(orig.subList(1, orig.size()).toArray(new RelVsSlbDo[orig.size() - 1]));
            }
        } else {
            rVsSlbDao.insert(new RelVsSlbDo().setVsId(virtualServer.getId()).setSlbId(virtualServer.getSlbId()));
        }
    }

    @Override
    public Set<Long> port(Long[] vsIds) throws Exception {
        List<VirtualServer> toUpdate = new ArrayList<>();
        Set<Long> failed = new HashSet<>();
        for (MetaVsArchiveDo metaVsArchiveDo : archiveVsDao.findMaxVersionByVses(vsIds, ArchiveVsEntity.READSET_FULL)) {
            try {
                toUpdate.add(DefaultSaxParser.parseEntity(VirtualServer.class, metaVsArchiveDo.getContent()));
            } catch (Exception ex) {
                failed.add(metaVsArchiveDo.getVsId());
            }
        }
        RelVsStatusDo[] dos = new RelVsStatusDo[toUpdate.size()];
        for (int i = 0; i < dos.length; i++) {
            dos[i] = new RelVsStatusDo().setVsId(toUpdate.get(i).getId()).setOfflineVersion(toUpdate.get(i).getVersion());
        }
        rVsStatusDao.insertOrUpdate(dos);
        for (VirtualServer virtualServer : toUpdate) {
            vsDomainRelMaintainer.relUpdateOffline(virtualServer, RelVsDomainDo.class, virtualServer.getDomains());
        }
        vsIds = new Long[toUpdate.size()];
        for (int i = 0; i < vsIds.length; i++) {
            vsIds[i] = toUpdate.get(i).getId();
        }
        List<ConfSlbVirtualServerActiveDo> ref = confSlbVirtualServerActiveDao.findBySlbVirtualServerIds(vsIds, ConfSlbVirtualServerActiveEntity.READSET_FULL);
        toUpdate.clear();

        for (ConfSlbVirtualServerActiveDo confSlbVirtualServerActiveDo : ref) {
            try {
                toUpdate.add(DefaultSaxParser.parseEntity(VirtualServer.class, confSlbVirtualServerActiveDo.getContent()));
            } catch (Exception ex) {
                failed.add(confSlbVirtualServerActiveDo.getSlbVirtualServerId());
            }
        }
        updateStatus(toUpdate.toArray(new VirtualServer[toUpdate.size()]));
        return failed;
    }
}
