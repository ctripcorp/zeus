package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbServer;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.handler.SlbSync;
import com.ctrip.zeus.service.model.handler.SlbValidator;
import com.ctrip.zeus.service.model.handler.VirtualServerValidator;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
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
    private VirtualServerRepository virtualServerRepository;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    private SlbValidator slbModelValidator;
    @Resource
    private VirtualServerValidator virtualServerModelValidator;
    @Resource
    private AutoFiller autoFiller;
    @Resource
    private ArchiveSlbDao archiveSlbDao;

    @Override
    public List<Slb> list(Long[] slbIds) throws Exception {
        Set<IdVersion> keys = slbCriteriaQuery.queryByIdsAndMode(slbIds, ModelMode.MODEL_MODE_MERGE_OFFLINE);
        return list(keys.toArray(new IdVersion[keys.size()]));
    }

    @Override
    public List<Slb> list(IdVersion[] keys) throws Exception {
        List<Slb> result = new ArrayList<>();
        Long[] slbIds = new Long[keys.length];
        Integer[] hashes = new Integer[keys.length];
        String[] values = new String[keys.length];
        for (int i = 0; i < hashes.length; i++) {
            hashes[i] = keys[i].hashCode();
            values[i] = keys[i].toString();
            slbIds[i] = keys[i].getId();
        }
        for (ArchiveSlbDo d : archiveSlbDao.findAllByIdVersion(hashes, values, ArchiveSlbEntity.READSET_FULL)) {
            Slb slb = DefaultSaxParser.parseEntity(Slb.class, d.getContent());
            slb.getVirtualServers().clear();
            result.add(slb);
        }

        Set<IdVersion> vsKeys = virtualServerCriteriaQuery.queryBySlbIds(slbIds);
        vsKeys.retainAll(virtualServerCriteriaQuery.queryAll(ModelMode.MODEL_MODE_ONLINE));
        Map<Long, List<VirtualServer>> map = new HashMap<>();
        for (VirtualServer vs : virtualServerRepository.listAll(vsKeys.toArray(new IdVersion[vsKeys.size()]))) {
            List<VirtualServer> l = map.get(vs.getSlbId());
            if (l == null) {
                l = new ArrayList<>();
                map.put(vs.getSlbId(), l);
            }
            l.add(vs);
        }
        for (Slb slb : result) {
            List<VirtualServer> l = map.get(slb.getId());
            if (l != null) {
                slb.getVirtualServers().addAll(l);
            }
        }
        return result;
    }

    @Override
    public Slb getById(Long slbId) throws Exception {
        IdVersion[] key = slbCriteriaQuery.queryByIdAndMode(slbId, ModelMode.MODEL_MODE_MERGE_OFFLINE);
        if (key.length == 0)
            return null;
        return getByKey(key[0]);
    }

    @Override
    public Slb getByKey(IdVersion key) throws Exception {
        ArchiveSlbDo d = archiveSlbDao.findBySlbAndVersion(key.getId(), key.getVersion(), ArchiveSlbEntity.READSET_FULL);
        if (d == null) return null;
        Slb result = DefaultSaxParser.parseEntity(Slb.class, d.getContent());
        refreshVirtualServer(result);
        return result;
    }

    @Override
    public Slb add(Slb slb) throws Exception {
        slbModelValidator.validate(slb);
        virtualServerModelValidator.validateVirtualServers(slb.getVirtualServers());
        autoFiller.autofill(slb);
        slbEntityManager.add(slb);
        for (VirtualServer virtualServer : slb.getVirtualServers()) {
            if (virtualServer.getSsl().booleanValue()) {
                virtualServerRepository.installCertificate(virtualServer);
            }
        }

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
        refreshVirtualServer(slb);
        slbEntityManager.update(slb);

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
    public void updateStatus(IdVersion[] slbs, ModelMode state) throws Exception {
        switch (state) {
            case MODEL_MODE_ONLINE:
                slbEntityManager.updateStatus(slbs);
                return;
            default:
                throw new NotImplementedException();
        }
    }

    @Override
    public void updateStatus(IdVersion[] slbs) throws Exception {
        updateStatus(slbs, ModelMode.MODEL_MODE_ONLINE);
    }

    @Override
    public Set<Long> port(Long[] slbId) throws Exception {
        return slbEntityManager.port(slbId);
    }

    private void refreshVirtualServer(Slb slb) throws Exception {
        slb.getVirtualServers().clear();
        Set<IdVersion> range = virtualServerCriteriaQuery.queryBySlbId(slb.getId());
        range.retainAll(virtualServerCriteriaQuery.queryAll(ModelMode.MODEL_MODE_MERGE_ONLINE));
        slb.getVirtualServers().addAll(virtualServerRepository.listAll(range.toArray(new IdVersion[range.size()])));
    }
}
