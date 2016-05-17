package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.VersionUtils;
import com.ctrip.zeus.service.model.handler.SlbSync;
import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zhoumy on 2015/9/29.
 */
@Component("slbEntityManager")
public class SlbEntityManager implements SlbSync {
    @Resource
    private SlbDao slbDao;
    @Resource
    private ArchiveSlbDao archiveSlbDao;
    @Resource
    private VirtualServerEntityManager virtualServerEntityManager;
    @Resource
    private SlbServerRelMaintainer slbServerRelMaintainer;
    @Resource
    private RSlbStatusDao rSlbStatusDao;
    @Resource
    private ConfSlbActiveDao confSlbActiveDao;

    @Override
    public void add(Slb slb) throws Exception {
        slb.setVersion(1);
        SlbDo d = C.toSlbDo(0L, slb);
        slbDao.insert(d);

        Long slbId = d.getId();
        slb.setId(slbId);
        for (VirtualServer virtualServer : slb.getVirtualServers()) {
            virtualServer.setSlbId(slbId);
            virtualServerEntityManager.add(virtualServer);
        }

        archiveSlbDao.insert(new ArchiveSlbDo().setSlbId(slbId).setVersion(slb.getVersion())
                .setContent(ContentWriters.writeSlbContent(slb))
                .setHash(VersionUtils.getHash(slb.getId(), slb.getVersion())));

        rSlbStatusDao.insertOrUpdate(new RelSlbStatusDo().setSlbId(slb.getId()).setOfflineVersion(slb.getVersion()));

        slbServerRelMaintainer.addRel(slb);
    }

    @Override
    public void update(Slb slb) throws Exception {
        RelSlbStatusDo check = rSlbStatusDao.findBySlb(slb.getId(), RSlbStatusEntity.READSET_FULL);
        if (check.getOfflineVersion() > slb.getVersion()) {
            throw new ValidationException("Newer slb version is detected.");
        }
        if (check.getOfflineVersion() != slb.getVersion()) {
            throw new ValidationException("Incompatible slb version.");
        }

        slb.setVersion(slb.getVersion() + 1);
        SlbDo d = C.toSlbDo(slb.getId(), slb);
        slbDao.updateById(d, SlbEntity.UPDATESET_FULL);

        archiveSlbDao.insert(new ArchiveSlbDo().setSlbId(slb.getId()).setVersion(slb.getVersion())
                .setContent(ContentWriters.writeSlbContent(slb))
                .setHash(VersionUtils.getHash(slb.getId(), slb.getVersion())));

        rSlbStatusDao.insertOrUpdate(check.setOfflineVersion(slb.getVersion()));

        slbServerRelMaintainer.updateRel(slb);
    }

    @Override
    public void updateStatus(List<Slb> slbs) throws Exception {
        RelSlbStatusDo[] dos = new RelSlbStatusDo[slbs.size()];
        for (int i = 0; i < dos.length; i++) {
            dos[i] = new RelSlbStatusDo().setSlbId(slbs.get(i).getId()).setOnlineVersion(slbs.get(i).getVersion());
        }

        Slb[] array = slbs.toArray(new Slb[slbs.size()]);
        slbServerRelMaintainer.updateStatus(array);

        rSlbStatusDao.updateOnlineVersionBySlb(dos, RSlbStatusEntity.UPDATESET_UPDATE_ONLINE_STATUS);
    }

    @Override
    public int delete(Long slbId) throws Exception {
        slbServerRelMaintainer.deleteRel(slbId);
        rSlbStatusDao.deleteAllBySlb(new RelSlbStatusDo().setSlbId(slbId));
        int count = slbDao.deleteByPK(new SlbDo().setId(slbId));
        archiveSlbDao.deleteBySlb(new ArchiveSlbDo().setSlbId(slbId));
        return count;
    }
}
