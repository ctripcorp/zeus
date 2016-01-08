package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbServer;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.handler.SlbSync;
import com.ctrip.zeus.service.model.handler.SlbValidator;
import com.ctrip.zeus.service.model.handler.VirtualServerValidator;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import org.springframework.stereotype.Repository;

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
    private ArchiveService archiveService;
    @Resource
    private SlbValidator slbModelValidator;
    @Resource
    private VirtualServerValidator virtualServerModelValidator;
    @Resource
    private AutoFiller autoFiller;

    @Override
    public List<Slb> list(Long[] slbIds) throws Exception {
        Set<IdVersion> keys = slbCriteriaQuery.queryByIdsAndMode(slbIds, ModelMode.MODEL_MODE_MERGE);
        return list(keys.toArray(new IdVersion[keys.size()]));
    }

    @Override
    public List<Slb> list(IdVersion[] keys) throws Exception {
        List<Slb> result = archiveService.listSlbs(keys);
        for (Slb slb : result) {
            slb.getVirtualServers().clear();
            Set<IdVersion> range = virtualServerCriteriaQuery.queryBySlbId(slb.getId());
            range.retainAll(virtualServerCriteriaQuery.queryAll(ModelMode.MODEL_MODE_ONLINE));
            slb.getVirtualServers().addAll(virtualServerRepository.listAll(range.toArray(new IdVersion[range.size()])));
        }
        return result;
    }

    @Override
    public Slb getById(Long slbId) throws Exception {
        return getById(slbId, ModelMode.MODEL_MODE_MERGE);
    }

    @Override
    public Slb getById(Long slbId, ModelMode mode) throws Exception {
        if (slbModelValidator.exists(slbId)) {
            Slb result = archiveService.getLatestSlb(slbId);
            result.getVirtualServers().clear();
            Set<IdVersion> check = virtualServerCriteriaQuery.queryBySlbId(slbId);
            check.retainAll(virtualServerCriteriaQuery.queryAll(mode));
            Long[] vsIds = new Long[check.size()];
            int i = 0;
            for (IdVersion idVersion : check) {
                vsIds[i] = idVersion.getId();
                ++i;
            }
            result.getVirtualServers().addAll(virtualServerRepository.listAll(vsIds));
        }
        return null;
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
        freshVirtualServers(slb);
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

    private void freshVirtualServers(Slb slb) throws Exception {
        slb.getVirtualServers().clear();
        Set<Long> vsIds = null;// virtualServerCriteriaQuery.queryBySlbId(slb.getId());
        for (VirtualServer virtualServer : virtualServerRepository.listAll(vsIds.toArray(new Long[vsIds.size()]))) {
            slb.getVirtualServers().add(virtualServer);
        }
    }

}
