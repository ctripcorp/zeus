package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.handler.SlbSync;
import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public void add(Slb slb) throws Exception {
        slb.setVersion(1);
        SlbDo d = C.toSlbDo(0L, slb);
        slbDao.insert(d);
        slb.setId(d.getId());
        for (VirtualServer virtualServer : slb.getVirtualServers()) {
            virtualServer.setSlbId(slb.getId());
            virtualServerEntityManager.add(virtualServer);
        }
        archiveSlbDao.insert(new ArchiveSlbDo().setSlbId(slb.getId()).setVersion(slb.getVersion()).setContent(ContentWriters.writeSlbContent(slb)));
        rSlbStatusDao.insertOrUpdate(new RelSlbStatusDo().setId(slb.getId()).setOfflineVersion(slb.getVersion()));
        slbServerRelMaintainer.relAdd(slb, RelSlbSlbServerDo.class, slb.getSlbServers());
    }

    @Override
    public void update(Slb slb) throws Exception {
        SlbDo check = slbDao.findById(slb.getId(), SlbEntity.READSET_FULL);
        if (check.getVersion() > slb.getVersion())
            throw new ValidationException("Newer Slb version is detected.");
        slb.setVersion(slb.getVersion() + 1);

        SlbDo d = C.toSlbDo(slb.getId(), slb);
        slbDao.updateById(d, SlbEntity.UPDATESET_FULL);
        archiveSlbDao.insert(new ArchiveSlbDo().setSlbId(slb.getId()).setVersion(slb.getVersion()).setContent(ContentWriters.writeSlbContent(slb)));
        rSlbStatusDao.insertOrUpdate(new RelSlbStatusDo().setId(slb.getId()).setOfflineVersion(slb.getVersion()));
        slbServerRelMaintainer.relUpdateOffline(slb, RelSlbSlbServerDo.class, slb.getSlbServers());
    }

    @Override
    public void updateStatus(Slb[] slbs) throws Exception {
        RelSlbStatusDo[] dos = new RelSlbStatusDo[slbs.length];
        for (int i = 0; i < dos.length; i++) {
            dos[i] = new RelSlbStatusDo().setSlbId(slbs[i].getId()).setOnlineVersion(slbs[i].getVersion());
        }
        rSlbStatusDao.updateOnlineVersionBySlb(dos, RSlbStatusEntity.UPDATESET_UPDATE_ONLINE_STATUS);
        Map<Long, Slb> ref = new HashMap<>();
        for (Slb slb : slbs) {
            ref.put(slb.getId(), slb);
        }
        List<RelSlbStatusDo> check = rSlbStatusDao.findBySlbs(ref.keySet().toArray(new Long[ref.size()]), RSlbStatusEntity.READSET_FULL);
        for (RelSlbStatusDo relSlbStatusDo : check) {
            if (relSlbStatusDo.getOnlineVersion() != relSlbStatusDo.getOfflineVersion()) {
                Slb slb = ref.get(relSlbStatusDo.getSlbId());
                slbServerRelMaintainer.relUpdateOnline(slb, RelSlbSlbServerDo.class, slb.getSlbServers());
            }
        }
    }

    @Override
    public int delete(Long slbId) throws Exception {
        slbServerRelMaintainer.relDelete(slbId);
        int count = slbDao.deleteByPK(new SlbDo().setId(slbId));
        archiveSlbDao.deleteBySlb(new ArchiveSlbDo().setSlbId(slbId));
        return count;
    }

    @Override
    public List<Long> port(Slb[] slbs) throws Exception {
        List<Long> fails = new ArrayList<>();
        for (Slb slb : slbs) {
            try {
//                relSyncSlbServer(slb, false);
            } catch (Exception ex) {
                fails.add(slb.getId());
            }
        }
        return fails;
    }
}
