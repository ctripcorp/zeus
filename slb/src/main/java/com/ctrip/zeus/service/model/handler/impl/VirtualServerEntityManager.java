package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dao.entity.*;
import com.ctrip.zeus.dao.mapper.SlbArchiveVsMapper;
import com.ctrip.zeus.dao.mapper.SlbVirtualServerMapper;
import com.ctrip.zeus.dao.mapper.SlbVsStatusRMapper;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.VirtualServer;
import com.ctrip.zeus.service.model.VersionUtils;
import com.ctrip.zeus.service.model.handler.VirtualServerSync;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by zhoumy on 2015/9/22.
 */
@Component("virtualServerEntityManager")
public class VirtualServerEntityManager implements VirtualServerSync {
    @Resource
    private SlbVirtualServerMapper slbVirtualServerMapper;
    @Resource
    private SlbVsDomainRelMaintainer slbVsDomainRelMaintainer;

    @Resource
    private SlbVsSlbRelMaintainer slbVsSlbRelMaintainer;

    @Resource
    private SlbArchiveVsMapper slbArchiveVsMapper;

    @Resource
    private SlbVsStatusRMapper slbVsStatusRMapper;

    public void setSlbVsDomainRelMaintainer(SlbVsDomainRelMaintainer slbVsDomainRelMaintainer) {
        this.slbVsDomainRelMaintainer = slbVsDomainRelMaintainer;
    }

    public void setSlbVsSlbRelMaintainer(SlbVsSlbRelMaintainer slbVsSlbRelMaintainer) {
        this.slbVsSlbRelMaintainer = slbVsSlbRelMaintainer;
    }

    @Override
    public void add(VirtualServer virtualServer) throws Exception {
        virtualServer.setVersion(1).setId(null);
        SlbVirtualServer d = toSlbVirtualServer(virtualServer);

        // Insert
        slbVirtualServerMapper.insert(d);
        Long vsId = d.getId();
        virtualServer.setId(vsId);

        // Relation
        slbArchiveVsMapper.insert(SlbArchiveVs.builder().vsId(vsId).version(virtualServer.getVersion())
                .content(ContentWriters.writeVirtualServerContent(virtualServer))
                .hash(VersionUtils.getHash(virtualServer.getId(), virtualServer.getVersion()))
                .build());
        slbVsStatusRMapper.insert(SlbVsStatusR.builder().vsId(vsId).offlineVersion(virtualServer.getVersion()).onlineVersion(0).build());
        slbVsSlbRelMaintainer.insert(virtualServer);
        slbVsDomainRelMaintainer.insert(virtualServer);
    }

    @Override
    public void update(VirtualServer virtualServer) throws Exception {
        SlbVsStatusR check = slbVsStatusRMapper.selectOneByExample(new SlbVsStatusRExample().createCriteria().andVsIdEqualTo(virtualServer.getId()).example());
        if (check.getOfflineVersion() > virtualServer.getVersion()) {
            throw new ValidationException("Newer virtual server version is detected.");
        }
        if (!check.getOfflineVersion().equals(virtualServer.getVersion())) {
            throw new ValidationException("Incompatible virtual server version.");
        }

        virtualServer.setVersion(virtualServer.getVersion() + 1);

        SlbVirtualServer d = toSlbVirtualServer(virtualServer);
        slbVirtualServerMapper.updateByPrimaryKey(d);

        slbArchiveVsMapper.insert(SlbArchiveVs.builder().vsId(virtualServer.getId()).version(virtualServer.getVersion())
                .content(ContentWriters.writeVirtualServerContent(virtualServer))
                .hash(VersionUtils.getHash(virtualServer.getId(), virtualServer.getVersion()))
                .build());

        slbVsSlbRelMaintainer.refreshOffline(virtualServer);
        slbVsDomainRelMaintainer.refreshOffline(virtualServer);
        check.setOfflineVersion(virtualServer.getVersion());
        slbVsStatusRMapper.upsertOfflineVersion(check);
    }

    @Override
    public void updateStatus(List<VirtualServer> virtualServers) throws Exception {
        if (virtualServers == null || virtualServers.size() == 0) return;

        SlbVsStatusR[] dos = new SlbVsStatusR[virtualServers.size()];
        for (int i = 0; i < dos.length; i++) {
            dos[i] = SlbVsStatusR.builder().vsId(virtualServers.get(i).getId()).onlineVersion(virtualServers.get(i).getVersion()).build();
        }
        VirtualServer[] array = virtualServers.toArray(new VirtualServer[virtualServers.size()]);

        slbVsSlbRelMaintainer.refreshOnline(array);
        slbVsDomainRelMaintainer.refreshOnline(array);

        slbVsStatusRMapper.batchUpdateByVsId(Arrays.asList(dos));
    }

    @Override
    public void delete(Long vsId) throws Exception {
        slbVsSlbRelMaintainer.clear(vsId);
        slbVsDomainRelMaintainer.clear(vsId);
        slbVsStatusRMapper.deleteByExample(new SlbVsStatusRExample().createCriteria().andVsIdEqualTo(vsId).example());
        slbVirtualServerMapper.deleteByPrimaryKey(vsId);
        slbArchiveVsMapper.deleteByExample(new SlbArchiveVsExample().createCriteria().andVsIdEqualTo(vsId).example());
    }

    private static SlbVirtualServer toSlbVirtualServer(VirtualServer e) {
        return SlbVirtualServer.
                builder().
                id(e.getId() == null ? 0L : e.getId()).
                name(e.getName()).
                port(e.getPort()).
                isSsl(e.isSsl()).
                version(e.getVersion()).
                createdTime(new Date()).
                slbId(0L).
                build();
    }

}
