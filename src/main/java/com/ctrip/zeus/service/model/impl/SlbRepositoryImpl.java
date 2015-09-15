package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.NginxServerDao;
import com.ctrip.zeus.dal.core.NginxServerDo;
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
    private SlbSync slbSync;
    @Resource
    private SlbQuery slbQuery;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private VirtualServerRepository virtualServerRepository;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    private GroupMemberRepository groupMemberRepository;
    @Resource
    private ArchiveService archiveService;
    @Resource
    private SlbValidator slbModelValidator;

    @Override
    public List<Slb> list() throws Exception {
        Set<Long> slbIds = slbCriteriaQuery.queryAll();
        return archiveService.getLatestSlbs(slbIds.toArray(new Long[slbIds.size()]));
    }

    @Override
    public Slb getById(Long slbId) throws Exception {
        return archiveService.getLatestSlb(slbId);
    }

    @Override
    public Slb get(String slbName) throws Exception {
        return getById(slbCriteriaQuery.queryByName(slbName));
    }

    @Override
    public Slb getBySlbServer(String slbServerIp) throws Exception {
        return getById(slbCriteriaQuery.queryBySlbServer(slbServerIp));
    }

    @Override
    public Slb getByVirtualServer(Long virtualServerId) throws Exception {
        VirtualServer vs = virtualServerRepository.getById(virtualServerId);
        return getById(vs.getSlbId());
    }

    @Override
    public List<Slb> listByGroupServer(String groupServerIp) throws Exception {
        if (groupServerIp == null)
            throw new ValidationException("group server ip must not be empty.");
        Long[] groupIds = groupMemberRepository.findGroupsByGroupServerIp(groupServerIp);
        if (groupIds.length == 0)
            return new ArrayList<>();
        return listByGroups(groupIds);
    }

    @Override
    public List<Slb> listByGroups(Long[] groupIds) throws Exception {
        Set<Long> slbIds = slbCriteriaQuery.queryByGroups(groupIds);
        return batchGet(slbIds.toArray(new Long[slbIds.size()]));
    }

    @Override
    public Slb add(Slb slb) throws Exception {
        slbModelValidator.validate(slb);
        slbModelValidator.validateVirtualServer(slb.getVirtualServers().toArray(
                new VirtualServer[slb.getVirtualServers().size()]));
        Long slbId = slbSync.add(slb);
        slb.setId(slbId);
        addVs(slb);
        Slb result = archive(slbId);

        for (SlbServer slbServer : result.getSlbServers()) {
            nginxServerDao.insert(new NginxServerDo()
                    .setIp(slbServer.getIp())
                    .setSlbId(result.getId())
                    .setVersion(0)
                    .setCreatedTime(new Date()));
        }
        return result;
    }

    @Override
    public Slb update(Slb slb) throws Exception {
        slbModelValidator.validate(slb);
        Long slbId = slbSync.update(slb);
        Slb result = archive(slbId);

        for (SlbServer slbServer : result.getSlbServers()) {
            nginxServerDao.insert(new NginxServerDo()
                    .setIp(slbServer.getIp())
                    .setSlbId(slb.getId())
                    .setVersion(0)
                    .setCreatedTime(new Date()));
        }
        return result;
    }

    @Override
    public int delete(Long slbId) throws Exception {
        slbModelValidator.removable(slbId);
        removeVsBySlb(slbId);
        int count = slbSync.delete(slbId);
        return count;
    }

    @Override
    public Slb updateVersion(Long slbId) throws Exception {
        slbSync.updateVersion(slbId);
        return archive(slbId);
    }

    private Slb archive(Long slbId) throws Exception {
        Slb slb = slbQuery.getById(slbId);
        Set<Long> vsIds = virtualServerCriteriaQuery.queryBySlbId(slb.getId());
        for (VirtualServer virtualServer : virtualServerRepository.listAll(vsIds.toArray(new Long[vsIds.size()]))) {
            slb.addVirtualServer(virtualServer);
        }
        archiveService.archiveSlb(slb);
        return slb;
    }

    private List<Slb> batchGet(Long[] slbIds) throws Exception {
        return archiveService.getLatestSlbs(slbIds);
    }

    private void addVs(Slb slb) throws Exception {
        for (VirtualServer virtualServer : slb.getVirtualServers()) {
            virtualServerRepository.addVirtualServer(slb.getId(), virtualServer);
        }
    }

    private void removeVsBySlb(Long slbId) throws Exception {
        virtualServerRepository.batchDeleteVirtualServers(slbId);
    }
}
