package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.NginxServerDao;
import com.ctrip.zeus.dal.core.NginxServerDo;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbList;
import com.ctrip.zeus.model.entity.SlbServer;
import com.ctrip.zeus.service.model.ArchiveService;
import com.ctrip.zeus.service.model.SlbQuery;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.model.SlbSync;
import com.ctrip.zeus.service.nginx.NginxService;
import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Repository;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author:xingchaowang
 * @date: 3/5/2015.
 */
@Repository("slbClusterRepository")
public class SlbRepositoryImpl implements SlbRepository {

    @Resource
    private SlbSync slbSync;
    @Resource
    private SlbQuery slbQuery;

    @Resource
    private ArchiveService archiveService;

    @Resource
    private NginxServerDao nginxServerDao;

    @Override
    public SlbList list() {
        try {
            SlbList list = new SlbList();
            for (Slb slb : slbQuery.getAll()) {
                list.addSlb(slb);
            }
            list.setTotal(list.getSlbs().size());
            return list;
        } catch (DalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Slb get(String slbName) {
        try {
            return slbQuery.get(slbName);
        } catch (DalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void add(Slb slb) {
        if (slb == null)
            return;
        try {
            slb = C.toSlb(slbSync.add(slb));
            archiveService.archiveSlb(slb);

            for (SlbServer slbServer : slb.getSlbServers()) {
                nginxServerDao.insert(new NginxServerDo()
                        .setIp(slbServer.getIp())
                        .setSlbName(slb.getName())
                        .setVersion(0)
                        .setCreatedTime(new Date()));
            }
        } catch (DalException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Slb slb) {
        if (slb == null)
            return;
        try {
            slb = C.toSlb(slbSync.update(slb));
            archiveService.archiveSlb(slb);

            for (SlbServer slbServer : slb.getSlbServers()) {
                nginxServerDao.insert(new NginxServerDo()
                        .setIp(slbServer.getIp())
                        .setSlbName(slb.getName())
                        .setVersion(0)
                        .setCreatedTime(new Date()));
            }
        } catch (DalException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int delete(String slbName) {
        try {
            int count = slbSync.delete(slbName);
            archiveService.deleteSlbArchive(slbName);
            return count;
        } catch (DalException e) {
            e.printStackTrace();
        } catch (ValidationException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
