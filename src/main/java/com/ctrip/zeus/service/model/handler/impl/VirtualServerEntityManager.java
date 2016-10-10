package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.VersionUtils;
import com.ctrip.zeus.service.model.handler.VirtualServerSync;
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
    private VsSlbRelMaintainer vsSlbRelMaintainer;
    @Resource
    private ArchiveVsDao archiveVsDao;
    @Resource
    private RVsStatusDao rVsStatusDao;

    @Override
    public void add(VirtualServer virtualServer) throws Exception {
        virtualServer.setVersion(1);
        SlbVirtualServerDo d = toSlbVirtualServerDo(virtualServer);
        slbVirtualServerDao.insert(d);

        Long vsId = d.getId();
        virtualServer.setId(vsId);
        archiveVsDao.insert(new MetaVsArchiveDo().setVsId(vsId).setVersion(virtualServer.getVersion())
                .setContent(ContentWriters.writeVirtualServerContent(virtualServer))
                .setHash(VersionUtils.getHash(virtualServer.getId(), virtualServer.getVersion())));

        rVsStatusDao.insert(new RelVsStatusDo().setVsId(vsId).setOfflineVersion(virtualServer.getVersion()));

        vsSlbRelMaintainer.insert(virtualServer);
        vsDomainRelMaintainer.insert(virtualServer);
    }

    @Override
    public void update(VirtualServer virtualServer) throws Exception {
        RelVsStatusDo check = rVsStatusDao.findByVs(virtualServer.getId(), RVsStatusEntity.READSET_FULL);
        if (check.getOfflineVersion() > virtualServer.getVersion()) {
            throw new ValidationException("Newer virtual server version is detected.");
        }
        if (check.getOfflineVersion() != virtualServer.getVersion()) {
            throw new ValidationException("Incompatible virtual server version.");
        }

        virtualServer.setVersion(virtualServer.getVersion() + 1);

        SlbVirtualServerDo d = toSlbVirtualServerDo(virtualServer);
        slbVirtualServerDao.updateByPK(d, SlbVirtualServerEntity.UPDATESET_FULL);

        archiveVsDao.insert(new MetaVsArchiveDo().setVsId(virtualServer.getId()).setContent(ContentWriters.writeVirtualServerContent(virtualServer))
                .setVersion(virtualServer.getVersion())
                .setHash(VersionUtils.getHash(virtualServer.getId(), virtualServer.getVersion())));

        rVsStatusDao.insertOrUpdate(check.setOfflineVersion(virtualServer.getVersion()));

        vsSlbRelMaintainer.refreshOffline(virtualServer);
        vsDomainRelMaintainer.refreshOffline(virtualServer);
    }

    @Override
    public void updateStatus(List<VirtualServer> virtualServers) throws Exception {
        RelVsStatusDo[] dos = new RelVsStatusDo[virtualServers.size()];
        for (int i = 0; i < dos.length; i++) {
            dos[i] = new RelVsStatusDo().setVsId(virtualServers.get(i).getId()).setOnlineVersion(virtualServers.get(i).getVersion());
        }

        VirtualServer[] array = virtualServers.toArray(new VirtualServer[virtualServers.size()]);
        vsSlbRelMaintainer.refreshOnline(array);
        vsDomainRelMaintainer.refreshOnline(array);

        rVsStatusDao.updateOnlineVersionByVs(dos, RVsStatusEntity.UPDATESET_UPDATE_ONLINE_STATUS);
    }

    @Override
    public void delete(Long vsId) throws Exception {
        vsSlbRelMaintainer.clear(vsId);
        vsDomainRelMaintainer.clear(vsId);
        rVsStatusDao.deleteAllByVs(new RelVsStatusDo().setVsId(vsId));
        slbVirtualServerDao.deleteById(new SlbVirtualServerDo().setId(vsId));
        archiveVsDao.deleteByVs(new MetaVsArchiveDo().setVsId(vsId));
    }

    private static SlbVirtualServerDo toSlbVirtualServerDo(VirtualServer e) {
        return new SlbVirtualServerDo().setId(e.getId() == null ? 0L : e.getId()).setName(e.getName()).setPort(e.getPort()).setIsSsl(e.isSsl()).setVersion(e.getVersion());
    }
}
