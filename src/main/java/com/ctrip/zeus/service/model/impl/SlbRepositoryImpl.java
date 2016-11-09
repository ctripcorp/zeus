package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbServer;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.handler.SlbQuery;
import com.ctrip.zeus.service.model.handler.SlbSync;
import com.ctrip.zeus.service.model.handler.SlbValidator;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.handler.impl.ContentReaders;
import com.ctrip.zeus.service.nginx.CertificateService;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import org.springframework.stereotype.Repository;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author:xingchaowang
 * @date: 3/5/2015.
 */
@Repository("slbRepository")
public class SlbRepositoryImpl implements SlbRepository {
    @Resource
    private NginxServerDao nginxServerDao;
    @Resource
    private SlbSync slbEntityManager;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private SlbQuery slbQuery;
    @Resource
    private SlbValidator slbModelValidator;
    @Resource
    private AutoFiller autoFiller;
    @Resource
    private CertificateService certificateService;
    @Resource
    private ArchiveSlbDao archiveSlbDao;

    @Override
    public List<Slb> list(Long[] slbIds) throws Exception {
        Set<IdVersion> keys = slbCriteriaQuery.queryByIdsAndMode(slbIds, SelectionMode.OFFLINE_FIRST);
        return list(keys.toArray(new IdVersion[keys.size()]));
    }

    @Override
    public List<Slb> list(IdVersion[] keys) throws Exception {
        Long[] slbIds = new Long[keys.length];
        Integer[] hashes = new Integer[keys.length];
        String[] values = new String[keys.length];
        for (int i = 0; i < hashes.length; i++) {
            hashes[i] = keys[i].hashCode();
            values[i] = keys[i].toString();
            slbIds[i] = keys[i].getId();
        }

        List<Slb> result = new ArrayList<>();
        for (ArchiveSlbDo d : archiveSlbDao.findAllByIdVersion(hashes, values, ArchiveSlbEntity.READSET_FULL)) {
            Slb slb = ContentReaders.readSlbContent(d.getContent());
            slb.setCreatedTime(d.getDataChangeLastTime());
            result.add(slb);
        }

        return result;
    }

    @Override
    public Slb getById(Long slbId) throws Exception {
        IdVersion[] key = slbCriteriaQuery.queryByIdAndMode(slbId, SelectionMode.OFFLINE_FIRST);
        if (key.length == 0)
            return null;
        return getByKey(key[0]);
    }

    @Override
    public Slb getByKey(IdVersion key) throws Exception {
        ArchiveSlbDo d = archiveSlbDao.findBySlbAndVersion(key.getId(), key.getVersion(), ArchiveSlbEntity.READSET_FULL);
        if (d == null) return null;

        Slb result = ContentReaders.readSlbContent(d.getContent());
        result.setCreatedTime(d.getDataChangeLastTime());

        return result;
    }

    @Override
    public Slb add(Slb slb) throws Exception {
        slbModelValidator.validate(slb);
        autoFiller.autofill(slb);
        slbEntityManager.add(slb);

        for (SlbServer slbServer : slb.getSlbServers()) {
            nginxServerDao.insert(new NginxServerDo()
                    .setIp(slbServer.getIp())
                    .setSlbId(slb.getId())
                    .setVersion(0)
                    .setCreatedTime(new Date()));
        }
        return slb;
    }

    @Override
    public Slb update(Slb slb) throws Exception {
        slbModelValidator.validate(slb);
        autoFiller.autofill(slb);

        Set<String> checkList = new HashSet<>();
        for (SlbServer ss : slb.getSlbServers()) {
            checkList.add(ss.getIp());
        }
        for (String ss : slbQuery.getSlbIps(slb.getId())) {
            checkList.remove(ss);
        }

        slbEntityManager.update(slb);
        certificateService.install(slb.getId(), new ArrayList<>(checkList), false);

        for (SlbServer slbServer : slb.getSlbServers()) {
            nginxServerDao.insert(new NginxServerDo()
                    .setIp(slbServer.getIp())
                    .setSlbId(slb.getId())
                    .setVersion(0)
                    .setCreatedTime(new Date()));
        }
        return slb;
    }

    @Override
    public int delete(Long slbId) throws Exception {
        slbModelValidator.removable(slbId);
        return slbEntityManager.delete(slbId);
    }

    @Override
    public void updateStatus(IdVersion[] slbs, SelectionMode state) throws Exception {
        switch (state) {
            case ONLINE_EXCLUSIVE:
                List<Slb> result = new ArrayList<>();
                for (int i = 0; i < slbs.length; i++) {
                    if (slbs[i].getVersion() == 0) {
                        result.add(new Slb().setId(slbs[i].getId()).setVersion(slbs[i].getVersion()));
                    }
                }
                result.addAll(list(slbs));
                slbEntityManager.updateStatus(result);
                return;
            default:
                throw new NotImplementedException();
        }
    }

    @Override
    public void updateStatus(IdVersion[] slbs) throws Exception {
        updateStatus(slbs, SelectionMode.ONLINE_EXCLUSIVE);
    }
}