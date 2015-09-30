package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbServer;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.ArchiveService;
import com.ctrip.zeus.service.model.GroupMemberRepository;
import com.ctrip.zeus.service.model.VirtualServerRepository;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.model.handler.SlbQuery;
import com.ctrip.zeus.service.model.handler.SlbSync;
import com.ctrip.zeus.service.model.handler.SlbValidator;
import com.ctrip.zeus.service.model.handler.VirtualServerValidator;
import com.ctrip.zeus.service.model.handler.impl.ContentWriters;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
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
    private GroupCriteriaQuery groupCriteriaQuery;
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

    @Override
    public List<Slb> list() throws Exception {
        Set<Long> slbIds = slbCriteriaQuery.queryAll();
        return archiveService.getLatestSlbs(slbIds.toArray(new Long[slbIds.size()]));
    }

    @Override
    public List<Slb> list(Long[] slbIds) throws Exception {
        return archiveService.getLatestSlbs(slbIds);
    }

    @Override
    public Slb getById(Long slbId) throws Exception {
        if (slbModelValidator.exists(slbId)) {
            return archiveService.getLatestSlb(slbId);
        }
        return null;
    }

    @Override
    public Slb get(String slbName) throws Exception {
        return getById(slbCriteriaQuery.queryByName(slbName));
    }

    @Override
    public Slb getBySlbServer(String slbServerIp) throws Exception {
        return getById(slbCriteriaQuery.queryBySlbServerIp(slbServerIp));
    }

    @Override
    public Slb getByVirtualServer(Long virtualServerId) throws Exception {
        VirtualServer vs = virtualServerRepository.getById(virtualServerId);
        return getById(vs.getSlbId());
    }

    @Override
    public List<Slb> listByGroupServer(String groupServerIp) throws Exception {
        Set<Long> groupIds = groupCriteriaQuery.queryByGroupServerIp(groupServerIp);
        if (groupIds.size() == 0)
            return new ArrayList<>();
        return listByGroups(groupIds.toArray(new Long[groupIds.size()]));
    }

    @Override
    public List<Slb> listByGroups(Long[] groupIds) throws Exception {
        Set<Long> slbIds = slbCriteriaQuery.queryByGroups(groupIds);
        return list(slbIds.toArray(new Long[slbIds.size()]));
    }

    @Override
    public Slb add(Slb slb) throws Exception {
        slbModelValidator.validate(slb);
        virtualServerModelValidator.validateVirtualServers(slb.getVirtualServers());
        autofill(slb);
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
        autofill(slb);
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

    @Override
    public Slb updateVersion(Long slbId) throws Exception {
        Slb slb = fresh(slbId);
        slbEntityManager.update(slb);
        return slb;
    }

    @Override
    public void autofill(Slb slb) throws Exception {
        slb.setNginxBin("/opt/app/nginx/sbin").setNginxConf("/opt/app/nginx/conf").setNginxWorkerProcesses(9)
                .setStatus(slb.getStatus() == null ? "Default" : slb.getStatus());
        for (VirtualServer virtualServer : slb.getVirtualServers()) {
            virtualServerRepository.autofill(virtualServer);
        }
    }

    @Override
    public List<Long> portSlbRel() throws Exception {
        Set<Long> slbIds = slbCriteriaQuery.queryAll();
        List<Slb> slbs = list(slbIds.toArray(new Long[slbIds.size()]));
        return slbEntityManager.port(slbs.toArray(new Slb[slbs.size()]));
    }

    @Override
    public void portSlbRel(Long slbId) throws Exception {
        Slb slb = getById(slbId);
        slbEntityManager.port(slb);
    }

    private Slb fresh(Long slbId) throws Exception {
        Slb slb = getById(slbId);
        autofill(slb);
        freshVirtualServers(slb);
        return slb;
    }

    private void freshVirtualServers(Slb slb) throws Exception {
        slb.getVirtualServers().clear();
        Set<Long> vsIds = virtualServerCriteriaQuery.queryBySlbId(slb.getId());
        for (VirtualServer virtualServer : virtualServerRepository.listAll(vsIds.toArray(new Long[vsIds.size()]))) {
            slb.getVirtualServers().add(virtualServer);
        }
    }

}
