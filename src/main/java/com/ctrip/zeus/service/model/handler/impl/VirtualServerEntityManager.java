package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.VersionUtils;
import com.ctrip.zeus.service.model.handler.VirtualServerSync;
import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Component;

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
        archiveVsDao.insert(new MetaVsArchiveDo().setVsId(vsId).setVersion(virtualServer.getVersion())
                .setContent(ContentWriters.writeVirtualServerContent(virtualServer))
                .setHash(VersionUtils.getHash(virtualServer.getId(), virtualServer.getVersion())));

        rVsStatusDao.insert(new RelVsStatusDo().setVsId(vsId).setOfflineVersion(virtualServer.getVersion()));

        rVsSlbDao.insert(new RelVsSlbDo().setVsId(vsId).setSlbId(virtualServer.getSlbId()).setVsVersion(virtualServer.getVersion()));
        vsDomainRelMaintainer.addRel(virtualServer);
    }

    @Override
    public void update(VirtualServer virtualServer) throws Exception {
        Long vsId = virtualServer.getId();
        RelVsStatusDo check = rVsStatusDao.findByVs(vsId, RVsStatusEntity.READSET_FULL);
        if (check.getOfflineVersion() > virtualServer.getVersion())
            throw new ValidationException("Newer virtual server version is detected.");
        virtualServer.setVersion(virtualServer.getVersion() + 1);

        SlbVirtualServerDo d = C.toSlbVirtualServerDo(vsId, virtualServer.getSlbId(), virtualServer);
        slbVirtualServerDao.updateByPK(d, SlbVirtualServerEntity.UPDATESET_FULL);

        archiveVsDao.insert(new MetaVsArchiveDo().setVsId(vsId).setContent(ContentWriters.writeVirtualServerContent(virtualServer))
                .setVersion(virtualServer.getVersion())
                .setHash(VersionUtils.getHash(virtualServer.getId(), virtualServer.getVersion())));

        rVsStatusDao.insertOrUpdate(check.setOfflineVersion(virtualServer.getVersion()));

        List<RelVsSlbDo> rel = rVsSlbDao.findByVs(vsId, RVsSlbEntity.READSET_FULL);
        Iterator<RelVsSlbDo> iter = rel.iterator();
        while (iter.hasNext()) {
            RelVsSlbDo r = iter.next();
            if (r.getVsVersion() == check.getOnlineVersion()) {
                iter.remove();
            }
        }
        if (rel.size() == 0) {
            rVsSlbDao.insert(new RelVsSlbDo().setVsId(virtualServer.getId()).setSlbId(virtualServer.getSlbId()).setVsVersion(virtualServer.getVersion()));
        } else {
            RelVsSlbDo r = rel.get(0);
            r.setSlbId(virtualServer.getSlbId()).setVsVersion(virtualServer.getVersion());
            rVsSlbDao.update(r, RVsSlbEntity.UPDATESET_FULL);
            rVsSlbDao.delete(rel.subList(1, rel.size()).toArray(new RelVsSlbDo[rel.size() - 1]));
        }
        vsDomainRelMaintainer.updateRel(virtualServer);
    }

    @Override
    public void updateStatus(List<VirtualServer> virtualServers) throws Exception {
        RelVsStatusDo[] dos = new RelVsStatusDo[virtualServers.size()];
        for (int i = 0; i < dos.length; i++) {
            dos[i] = new RelVsStatusDo().setVsId(virtualServers.get(i).getId()).setOnlineVersion(virtualServers.get(i).getVersion());
        }

        VirtualServer[] array =virtualServers.toArray(new VirtualServer[virtualServers.size()]);
        updateRelVsSlbStatus(array);
        vsDomainRelMaintainer.updateStatus(array);

        rVsStatusDao.updateOnlineVersionByVs(dos, RVsStatusEntity.UPDATESET_UPDATE_ONLINE_STATUS);
    }

    @Override
    public void delete(Long vsId) throws Exception {
        rVsSlbDao.deleteByVs(new RelVsSlbDo().setVsId(vsId));
        vsDomainRelMaintainer.deleteRel(vsId);
        rVsStatusDao.deleteAllByVs(new RelVsStatusDo().setVsId(vsId));
        slbVirtualServerDao.deleteById(new SlbVirtualServerDo().setId(vsId));
        archiveVsDao.deleteByVs(new MetaVsArchiveDo().setVsId(vsId));
    }

    private void updateRelVsSlbStatus(VirtualServer[] objects) throws Exception {
        Long[] ids = new Long[objects.length];
        Map<Long, Integer> idx = new HashMap<>();


        Integer[][] versionRef = new Integer[2][objects.length];

        for (int i = 0; i < objects.length; i++) {
            Long id = objects[i].getSlbId();
            idx.put(id, i);
            ids[i] = id;
        }

        for (RelVsStatusDo d : rVsStatusDao.findByVses(ids, RVsStatusEntity.READSET_FULL)) {
            versionRef[idx.get(d.getVsId())] = new Integer[]{d.getOfflineVersion(), d.getOnlineVersion()};
        }

        List<RelVsSlbDo> add = new ArrayList<>();
        List<RelVsSlbDo> update = new ArrayList<>();
        for (RelVsSlbDo d : rVsSlbDao.findByVses(ids, RVsSlbEntity.READSET_FULL)) {
            Integer objIdx = idx.get(d.getVsId());
            Integer[] versions = versionRef[objIdx];
            if (versions[0].intValue() == versions[1].intValue() || versions[1].intValue() == 0) {
                VirtualServer vs = objects[objIdx];
                d.setSlbId(vs.getSlbId()).setVsVersion(vs.getVersion()).setVsVersion(VersionUtils.getHash(vs.getId(), vs.getVersion()));
                add.add(d);
                continue;
            } else if (d.getVsVersion() == versions[1]) {
                VirtualServer vs = objects[objIdx];
                d.setSlbId(vs.getSlbId()).setVsVersion(vs.getVersion()).setVsVersion(VersionUtils.getHash(vs.getId(), vs.getVersion()));
                update.add(d);
            }
        }
    }

    @Override
    public Set<Long> port(Long[] vsIds) throws Exception {
        Map<Long, VirtualServer> toUpdate = new HashMap<>();
        Set<Long> failed = new HashSet<>();
        for (MetaVsArchiveDo d : archiveVsDao.findMaxVersionByVses(vsIds, ArchiveVsEntity.READSET_FULL)) {
            try {
                VirtualServer vs = ContentReaders.readVirtualServerContent(d.getContent());
                toUpdate.put(vs.getId(), vs);
            } catch (Exception ex) {
                failed.add(d.getVsId());
            }
        }
        RelVsStatusDo[] rel1 = new RelVsStatusDo[toUpdate.size()];
        int i = 0;
        for (VirtualServer vs : toUpdate.values()) {
            rel1[i] = new RelVsStatusDo().setVsId(vs.getId()).setOfflineVersion(vs.getVersion());
            i++;
        }

        rVsStatusDao.insertOrUpdate(rel1);

        List<RelVsSlbDo> rel2 = rVsSlbDao.findByVses(vsIds, RVsSlbEntity.READSET_FULL);
        for (RelVsSlbDo d : rel2) {
            VirtualServer vs = toUpdate.get(d.getVsId());
            d.setVsVersion(vs.getVersion());
        }
        rVsSlbDao.update(rel2.toArray(new RelVsSlbDo[rel2.size()]), RVsSlbEntity.UPDATESET_FULL);
        for (VirtualServer virtualServer : toUpdate.values()) {
            vsDomainRelMaintainer.port(virtualServer);
        }

        vsIds = new Long[toUpdate.size()];
        i = 0;
        for (VirtualServer vs : toUpdate.values()) {
            vsIds[i] = vs.getId();
            i++;
        }
        List<ConfSlbVirtualServerActiveDo> ref = confSlbVirtualServerActiveDao.findBySlbVirtualServerIds(vsIds, ConfSlbVirtualServerActiveEntity.READSET_FULL);
        toUpdate.clear();
        for (ConfSlbVirtualServerActiveDo d : ref) {
            try {
                VirtualServer vs = ContentReaders.readVirtualServerContent(d.getContent());
                toUpdate.put(vs.getId(), vs);
            } catch (Exception ex) {
                failed.add(d.getSlbVirtualServerId());
            }
        }

        updateStatus(new ArrayList<>(toUpdate.values()));

        return failed;
    }
}
