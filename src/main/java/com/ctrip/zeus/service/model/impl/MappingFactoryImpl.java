package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.service.model.MappingFactory;
import com.ctrip.zeus.service.model.ModelStatusMapping;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

/**
 * Created by zhoumy on 2016/1/19.
 */
@Component("mappingFactory")
public class MappingFactoryImpl implements MappingFactory {
    @Resource
    private ArchiveGroupDao archiveGroupDao;
    @Resource
    private ArchiveVsDao archiveVsDao;
    @Resource
    private RGroupStatusDao rGroupStatusDao;
    @Resource
    private RVsStatusDao rVsStatusDao;

    @Override
    public ModelStatusMapping<Group> getByVsIds(Long[] vsIds) throws Exception {
        ModelStatusMapping<Group> result = new ModelStatusMapping<>();
        Set<Long> groupIds = new HashSet<>();
        Map<String, Group> mapping = new HashMap<>();
        for (ArchiveGroupDo d : archiveGroupDao.findAllByVsIds(vsIds, ArchiveGroupEntity.READSET_FULL)) {
            groupIds.add(d.getGroupId());
            Group g = DefaultSaxParser.parseEntity(Group.class, d.getContent());
            for (GroupVirtualServer e : g.getGroupVirtualServers()) {
                e.setVirtualServer(new VirtualServer().setId(e.getVirtualServer().getId()));
            }
            mapping.put(d.getGroupId() + "," + d.getVersion(), g);
        }
        for (RelGroupStatusDo d : rGroupStatusDao.findByGroups(groupIds.toArray(new Long[groupIds.size()]), RGroupStatusEntity.READSET_FULL)) {
            if (d.getOnlineVersion() == 0 || d.getOnlineVersion() != d.getOfflineVersion()) {
                result.addOffline(d.getGroupId(), mapping.get(d.getGroupId() + "," + d.getOfflineVersion()));
            }
            if (d.getOnlineVersion() != 0) {
                result.addOnline(d.getGroupId(), mapping.get(d.getGroupId() + "," + d.getOnlineVersion()));
            }
        }
        return result;
    }

    @Override
    public ModelStatusMapping<VirtualServer> getBySlbIds(Long slbId) throws Exception {
        ModelStatusMapping<VirtualServer> result = new ModelStatusMapping<>();
        Set<Long> vsIds = new HashSet<>();
        Map<String, VirtualServer> mapping = new HashMap<>();
        ;
        for (MetaVsArchiveDo d : archiveVsDao.findAllBySlbId(slbId, ArchiveVsEntity.READSET_FULL)) {
            vsIds.add(d.getVsId());
            VirtualServer vs = DefaultSaxParser.parseEntity(VirtualServer.class, d.getContent());
            mapping.put(vs.getId() + "," + d.getVersion(), DefaultSaxParser.parseEntity(VirtualServer.class, d.getContent()));
        }
        for (RelVsStatusDo d : rVsStatusDao.findByVses(vsIds.toArray(new Long[vsIds.size()]), RVsStatusEntity.READSET_FULL)) {
            if (d.getOnlineVersion() == 0 || d.getOnlineVersion() != d.getOfflineVersion()) {
                result.addOffline(d.getVsId(), mapping.get(d.getVsId() + "," + d.getOfflineVersion()));
            }
            if (d.getOnlineVersion() != 0) {
                result.addOnline(d.getVsId(), mapping.get(d.getVsId() + "," + d.getOnlineVersion()));
            }
        }
        return result;
    }
}
