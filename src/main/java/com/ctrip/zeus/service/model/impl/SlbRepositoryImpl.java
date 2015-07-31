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


    @Override
    public List<Slb> list() throws Exception {
        return slbQuery.getAll();
    }

    @Override
    public Slb getById(Long slbId) throws Exception {
        return slbQuery.getById(slbId);
    }

    @Override
    public Slb get(String slbName) throws Exception {
        return slbQuery.get(slbName);
    }

    @Override
    public Slb getBySlbServer(String slbServerIp) throws Exception {
        return slbQuery.getBySlbServer(slbServerIp);
    }

    @Override
    public Slb getByVirtualServer(Long virtualServerId) throws Exception {
        VirtualServer vs = virtualServerRepository.getById(virtualServerId);
        return slbQuery.getById(vs.getSlbId());
    }

    @Override
    public List<Slb> listByGroupServerAndGroup(String groupServerIp, Long groupId) throws Exception {
        if (groupServerIp == null && groupId == null)
            throw new ValidationException("At least one parameter must not be null.");
        Long[] groupIds = null;
        if (groupServerIp != null) {
            groupIds = groupMemberRepository.findGroupsByGroupServerIp(groupServerIp);
        }
        if (groupId != null) {
            boolean existed = false;
            for (Long id : groupIds) {
                if (id.equals(groupId)) {
                    existed = true;
                    break;
                }
            }
            if (existed)
                groupIds = new Long[]{groupId};
            else
                return new ArrayList<>();
        }
        return slbQuery.getByGroups(groupIds);
    }

    @Override
    public List<Slb> listByGroups(Long[] groupIds) throws Exception {
        return slbQuery.getByGroups(groupIds);
    }

    @Override
    public Slb add(Slb slb) throws Exception {
        slbSync.add(slb);
        slb = slbQuery.getById(slb.getId());
        archiveService.archiveSlb(slb);

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
        slbSync.update(slb);
        Slb d = slbQuery.getById(slb.getId());
        archiveService.archiveSlb(d);
        for (SlbServer slbServer : slb.getSlbServers()) {
            nginxServerDao.insert(new NginxServerDo()
                    .setIp(slbServer.getIp())
                    .setSlbId(slb.getId())
                    .setVersion(0)
                    .setCreatedTime(new Date()));
        }
        return d;
    }

    @Override
    public int delete(Long slbId) throws Exception {
        int count = slbSync.delete(slbId);
        return count;
    }
}
