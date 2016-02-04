package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbServer;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.handler.SlbSync;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Component;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.annotation.Resource;
import java.util.ArrayList;
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
    private RSlbSlbServerDao rSlbSlbServerDao;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    private VirtualServerEntityManager virtualServerEntityManager;

    @Override
    public void add(Slb slb) throws Exception {
        slb.setVersion(1);
        SlbDo d = C.toSlbDo(0L, slb);
        slbDao.insert(d);
        slb.setId(d.getId());
        for (VirtualServer virtualServer : slb.getVirtualServers()) {
            virtualServer.setSlbId(slb.getId());
            virtualServerEntityManager.addVirtualServer(virtualServer);
        }
        archiveSlbDao.insert(new ArchiveSlbDo().setSlbId(slb.getId()).setVersion(slb.getVersion()).setContent(ContentWriters.writeSlbContent(slb)));
        relSyncSlbServer(slb, true);
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
        relSyncSlbServer(slb, false);
    }

    @Override
    public void updateVersion(Long slbId) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public int delete(Long slbId) throws Exception {
        Set<Long> vsIds = virtualServerCriteriaQuery.queryBySlbId(slbId);
        virtualServerEntityManager.deleteVirtualServers(vsIds.toArray(new Long[vsIds.size()]));
        rSlbSlbServerDao.deleteAllBySlb(new RelSlbSlbServerDo().setSlbId(slbId));
        int count = slbDao.deleteByPK(new SlbDo().setId(slbId));
        archiveSlbDao.deleteBySlb(new ArchiveSlbDo().setSlbId(slbId));
        return count;
    }

    @Override
    public List<Long> port(Slb[] slbs) throws Exception {
        List<Long> fails = new ArrayList<>();
        for (Slb slb : slbs) {
            try {
                relSyncSlbServer(slb, false);
            } catch (Exception ex) {
                fails.add(slb.getId());
            }
        }
        return fails;
    }

    @Override
    public void port(Slb slb) throws Exception {
        relSyncSlbServer(slb, false);
    }

    private void relSyncSlbServer(Slb slb, boolean isnew) throws Exception {
        if (isnew) {
            RelSlbSlbServerDo[] dos = new RelSlbSlbServerDo[slb.getSlbServers().size()];
            for (int i = 0; i < dos.length; i++) {
                dos[i] = new RelSlbSlbServerDo().setSlbId(slb.getId()).setIp(slb.getSlbServers().get(i).getIp());
            }
            rSlbSlbServerDao.insert(dos);
            return;
        }
        List<RelSlbSlbServerDo> originSses = rSlbSlbServerDao.findAllIpsBySlb(slb.getId(), RSlbSlbServerEntity.READSET_FULL);
        List<SlbServer> newSses = slb.getSlbServers();
        String[] originIps = new String[originSses.size()];
        String[] newIps = new String[newSses.size()];
        for (int i = 0; i < originIps.length; i++) {
            originIps[i] = originSses.get(i).getIp();
        }
        for (int i = 0; i < newIps.length; i++) {
            newIps[i] = newSses.get(i).getIp();
        }
        List<String> removing = new ArrayList<>();
        List<String> adding = new ArrayList<>();
        ArraysUniquePicker.pick(originIps, newIps, removing, adding);

        RelSlbSlbServerDo[] dos = new RelSlbSlbServerDo[removing.size()];
        for (int i = 0; i < dos.length; i++) {
            dos[i] = new RelSlbSlbServerDo().setSlbId(slb.getId()).setIp(removing.get(i));
        }
        rSlbSlbServerDao.deleteBySlbAndIp(dos);

        dos = new RelSlbSlbServerDo[adding.size()];
        for (int i = 0; i < dos.length; i++) {
            dos[i] = new RelSlbSlbServerDo().setSlbId(slb.getId()).setIp(adding.get(i));
        }
        rSlbSlbServerDao.insert(dos);
    }
}
