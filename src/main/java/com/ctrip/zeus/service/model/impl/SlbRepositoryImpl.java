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
import com.ctrip.zeus.service.model.handler.SlbQuery;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.model.handler.SlbSync;
import com.ctrip.zeus.service.model.handler.SlbValidator;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private VirtualServerRepository virtualServerRepository;
    @Resource
    private GroupMemberRepository groupMemberRepository;
    @Resource
    private ArchiveService archiveService;
    @Resource
    private SlbValidator slbModelValidator;

    @Override
    public List<Slb> list() throws Exception {
        List<Slb> result = slbQuery.getAll();
        for (Slb slb : result) {
            cascadeVs(slb);
        }
        return result;
    }

    @Override
    public Slb getById(Long slbId) throws Exception {
        return cascadeVs(slbQuery.getById(slbId));
    }

    @Override
    public Slb get(String slbName) throws Exception {
        return cascadeVs(slbQuery.get(slbName));
    }

    @Override
    public Slb getBySlbServer(String slbServerIp) throws Exception {
        return cascadeVs(slbQuery.getBySlbServer(slbServerIp));
    }

    @Override
    public Slb getByVirtualServer(Long virtualServerId) throws Exception {
        VirtualServer vs = virtualServerRepository.getById(virtualServerId);
        return cascadeVs(slbQuery.getById(vs.getSlbId()));
    }

    @Override
    public List<Slb> listByGroupServerAndGroup(String groupServerIp, Long groupId) throws Exception {
        if (groupServerIp == null && groupId == null)
            throw new ValidationException("At least one parameter must not be null.");
        Long[] groupIds = new Long[0];
        if (groupServerIp != null)
            groupIds = groupMemberRepository.findGroupsByGroupServerIp(groupServerIp);
        if (groupId == null)
            return slbQuery.getByGroups(groupIds);

        if (groupId != null && groupIds.length == 0)
            return slbQuery.getByGroups(new Long[]{groupId});
        boolean existed = false;
        for (Long id : groupIds) {
            if (id.equals(groupId)) {
                existed = true;
                break;
            }
        }
        List<Slb> result = new ArrayList<>();
        if (existed)
            result = slbQuery.getByGroups(new Long[]{groupId});
        for (Slb slb : result) {
            cascadeVs(slb);
        }
        return result;
    }

    @Override
    public List<Slb> listByGroups(Long[] groupIds) throws Exception {
        List<Slb> result = slbQuery.getByGroups(groupIds);
        for (Slb slb : result) {
            cascadeVs(slb);
        }
        return result;
    }

    @Override
    public Slb add(Slb slb) throws Exception {
        slbModelValidator.validate(slb);
        slbModelValidator.validateVirtualServer(slb.getVirtualServers().toArray(
                new VirtualServer[slb.getVirtualServers().size()]));
        Long slbId = slbSync.add(slb);
        slb.setId(slbId);
        addVs(slb);
        Slb result = getById(slbId);
        archiveService.archiveSlb(result);

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
        Slb result = getById(slbId);
        archiveService.archiveSlb(result);
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

    private Slb cascadeVs(Slb slb) throws Exception {
        for (VirtualServer virtualServer : virtualServerRepository.listVirtualServerBySlb(slb.getId())) {
            slb.addVirtualServer(virtualServer);
        }
        return slb;
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
