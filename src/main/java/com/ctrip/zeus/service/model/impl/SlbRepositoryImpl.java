package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.NginxServerDao;
import com.ctrip.zeus.dal.core.NginxServerDo;
import com.ctrip.zeus.dal.core.SlbDo;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.GroupSlb;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbServer;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.ArchiveService;
import com.ctrip.zeus.service.model.handler.GroupQuery;
import com.ctrip.zeus.service.model.handler.SlbQuery;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.model.handler.SlbSync;
import org.springframework.stereotype.Repository;
import org.unidal.dal.jdbc.DalException;

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
    private SlbSync slbSync;
    @Resource
    private SlbQuery slbQuery;
    @Resource
    private ArchiveService archiveService;
    @Resource
    private NginxServerDao nginxServerDao;

    @Override
    public List<Slb> list() throws Exception {
        List<Slb> list = new ArrayList<>();
        for (Slb slb : slbQuery.getAll()) {
            list.add(slb);
        }
        return list;
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
        return slbQuery.getByVirtualServer(virtualServerId);
    }

    @Override
    public List<Slb> listByGroupServerAndGroup(String groupServerIp, Long groupId) throws Exception {
        if (groupServerIp == null && (groupId == null || groupId.longValue() <= 0))
            return null;
        if (groupId == null || groupId.longValue() <= 0) {
            return slbQuery.getByGroupServer(groupServerIp);
        }
        if (groupServerIp == null) {
            return slbQuery.getByGroups(new Long[]{groupId});
        }
        return slbQuery.getByGroupServerAndGroup(groupServerIp, groupId);
    }

    @Override
    public List<Slb> listByGroups(Long[] groupIds) throws Exception {
        return slbQuery.getByGroups(groupIds);
    }

    @Override
    public List<GroupSlb> listGroupSlbsByGroups(Long[] groupIds) throws Exception {
        return slbQuery.getGroupSlbsByGroups(groupIds);
    }

    @Override
    public List<GroupSlb> listGroupSlbsByVirtualServer(Long virtualServerId) throws Exception {
        return slbQuery.getGroupSlbsByVirtualServer(virtualServerId);
    }

    @Override
    public List<GroupSlb> listGroupSlbsBySlb(Long slbId) throws Exception {
        return slbQuery.getGroupSlbsBySlb(slbId);
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

    @Override
    public List<String> listGroupServersBySlb(String slbName) throws Exception {
        return slbQuery.getGroupServersBySlb(slbName);
    }

    @Override
    public VirtualServer getVirtualServer(Long virtualServerId, Long slbId, String virtualServerName) throws Exception {
        if (virtualServerId == null
                && (slbId == null || virtualServerName == null)) {
            throw new ValidationException("Query cannot be performed due to lack of information.");
        }
        return slbQuery.getVirtualServer(virtualServerId, slbId, virtualServerName);
    }
}
