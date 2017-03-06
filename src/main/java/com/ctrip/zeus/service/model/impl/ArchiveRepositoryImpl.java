package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.TrafficPolicy;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.restful.message.view.ExtendedView;
import com.ctrip.zeus.service.model.Archive;
import com.ctrip.zeus.service.model.ArchiveRepository;
import com.ctrip.zeus.service.model.common.MetaType;
import com.ctrip.zeus.service.model.handler.impl.ContentReaders;
import com.ctrip.zeus.service.model.handler.impl.ContentWriters;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.ctrip.zeus.util.CompressUtils;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhoumy on 2016/5/17.
 */
@Repository("archiveRepository")
public class ArchiveRepositoryImpl implements ArchiveRepository {
    @Resource
    private GroupDao groupDao;
    @Resource
    private GroupHistoryDao groupHistoryDao;
    @Resource
    private ArchiveGroupDao archiveGroupDao;
    @Resource
    private ArchiveSlbDao archiveSlbDao;
    @Resource
    private ArchiveVsDao archiveVsDao;
    @Resource
    private ArchiveCommitDao archiveCommitDao;
    @Resource
    private ArchiveTrafficPolicyDao archiveTrafficPolicyDao;

    @Override
    public void archiveGroup(Group group) throws Exception {
        groupHistoryDao.insert(new GroupHistoryDo().setGroupId(group.getId()).setGroupName(group.getName()));
        archiveGroupDao.insert(new ArchiveGroupDo().setGroupId(group.getId()).setHash(0).setVersion(0)
                .setContent(ContentWriters.writeGroupContent(group)));
    }

    @Override
    public void archiveGroup(ExtendedView.ExtendedGroup group) throws Exception {
        groupHistoryDao.insert(new GroupHistoryDo().setGroupId(group.getId()).setGroupName(group.getName()));
        archiveGroupDao.insert(new ArchiveGroupDo().setGroupId(group.getId()).setHash(0).setVersion(0)
                .setContent(ContentWriters.write(group)));
    }

    @Override
    public void archiveSlb(Slb slb) throws Exception {
        archiveSlbDao.insert(new ArchiveSlbDo().setSlbId(slb.getId()).setHash(0).setVersion(0)
                .setContent(ContentWriters.writeSlbContent(slb)));
    }

    @Override
    public void archiveVs(VirtualServer vs) throws Exception {
        archiveVsDao.insert(new MetaVsArchiveDo().setVsId(vs.getId()).setHash(0).setVersion(0)
                .setContent(ContentWriters.writeVirtualServerContent(vs)));
    }

    @Override
    public void archivePolicy(TrafficPolicy trafficPolicy) throws Exception {
        archiveTrafficPolicyDao.insert(new ArchiveTrafficPolicyDo().setPolicyId(trafficPolicy.getId()).setPolicyName(trafficPolicy.getName()).setVersion(0)
                .setContent(CompressUtils.compressToGzippedBase64String(ContentWriters.write(trafficPolicy))));
    }

    @Override
    public String getGroupArchiveRaw(Long id, int version) throws Exception {
        ArchiveGroupDo archiveGroupDo = archiveGroupDao.findByGroupAndVersion(id, version, ArchiveGroupEntity.READSET_FULL);
        return archiveGroupDo == null ? null : archiveGroupDo.getContent();
    }

    @Override
    public Group getGroupArchive(Long id, int version) throws Exception {
        ArchiveGroupDo archiveGroupDo = archiveGroupDao.findByGroupAndVersion(id, version, ArchiveGroupEntity.READSET_FULL);
        return archiveGroupDo == null ? null : ContentReaders.readGroupContent(archiveGroupDo.getContent());
    }

    @Override
    public Group getGroupArchive(String name, int version) throws Exception {
        Long groupId;
        if (version == 0) {
            GroupHistoryDo d = groupHistoryDao.findByName(name, GroupHistoryEntity.READSET_FULL);
            if (d == null) return null;
            groupId = d.getGroupId();
        } else {
            GroupDo d = groupDao.findByName(name, GroupEntity.READSET_IDONLY);
            if (d == null) return null;
            groupId = d.getId();
        }

        ArchiveGroupDo archiveGroupDo = archiveGroupDao.findByGroupAndVersion(groupId, version, ArchiveGroupEntity.READSET_FULL);
        return archiveGroupDo == null ? null : ContentReaders.readGroupContent(archiveGroupDo.getContent());
    }

    @Override
    public Slb getSlbArchive(Long id, int version) throws Exception {
        ArchiveSlbDo archiveSlbDo = archiveSlbDao.findBySlbAndVersion(id, version, ArchiveSlbEntity.READSET_FULL);
        return archiveSlbDo == null ? null : ContentReaders.readSlbContent(archiveSlbDo.getContent());
    }

    @Override
    public VirtualServer getVsArchive(Long id, int version) throws Exception {
        MetaVsArchiveDo archiveVsDo = archiveVsDao.findByVsAndVersion(id, version, ArchiveVsEntity.READSET_FULL);
        return archiveVsDo == null ? null : ContentReaders.readVirtualServerContent(archiveVsDo.getContent());
    }

    @Override
    public TrafficPolicy getPolicyArchive(Long id, String name) throws Exception {
        ArchiveTrafficPolicyDo d = null;
        if (id != null && id > 0L) {
            d = archiveTrafficPolicyDao.findByPolicyId(id, ArchiveTrafficPolicyEntity.READSET_FULL);
        }
        if (d == null && name != null) {
            d = archiveTrafficPolicyDao.findByPolicyName(name, ArchiveTrafficPolicyEntity.READSET_FULL);
        }
        return d == null ? null : ObjectJsonParser.parse(CompressUtils.decompressGzippedBase64String(d.getContent()), TrafficPolicy.class);
    }

    @Override
    public List<Archive<Group>> getAllGroupArchives(Long id) throws Exception {
        List<ArchiveGroupDo> archives = archiveGroupDao.findAllByGroup(id, ArchiveGroupEntity.READSET_CONTENT_EXCLUDED);
        Long[] archiveIds = new Long[archives.size()];
        for (int i = 0; i < archives.size(); i++) {
            archiveIds[i] = archives.get(i).getId();
        }

        Map<Long, ArchiveCommitDo> commitByArchiveId = new HashMap<>();
        for (ArchiveCommitDo d : archiveCommitDao.findAllByArchiveAndType(archiveIds, MetaType.GROUP.getId(), ArchiveCommitEntity.READSET_FULL)) {
            commitByArchiveId.put(d.getArchiveId(), d);
        }
        List<Archive<Group>> result = new ArrayList<>(archives.size());
        for (ArchiveGroupDo e : archives) {
            Archive<Group> r = new Archive<Group>().setId(e.getGroupId()).setVersion(e.getVersion()).setCreatedTime(e.getDataChangeLastTime());
            result.add(r);

            ArchiveCommitDo c = commitByArchiveId.get(e.getId());
            if (c != null) {
                r.setAuthor(c.getAuthor()).setCommitMessage(c.getMessage());
            }
        }
        return result;
    }

    @Override
    public List<Archive<Slb>> getAllSlbArchives(Long id) throws Exception {
        List<ArchiveSlbDo> archives = archiveSlbDao.findAllBySlb(id, ArchiveSlbEntity.READSET_CONTENT_EXCLUDED);
        Long[] archiveIds = new Long[archives.size()];
        for (int i = 0; i < archives.size(); i++) {
            archiveIds[i] = archives.get(i).getId();
        }

        Map<Long, ArchiveCommitDo> commitByArchiveId = new HashMap<>();
        for (ArchiveCommitDo d : archiveCommitDao.findAllByArchiveAndType(archiveIds, MetaType.SLB.getId(), ArchiveCommitEntity.READSET_FULL)) {
            commitByArchiveId.put(d.getArchiveId(), d);
        }
        List<Archive<Slb>> result = new ArrayList<>(archives.size());
        for (ArchiveSlbDo e : archives) {
            Archive<Slb> r = new Archive<Slb>().setId(e.getSlbId()).setVersion(e.getVersion()).setCreatedTime(e.getDataChangeLastTime());
            result.add(r);

            ArchiveCommitDo c = commitByArchiveId.get(e.getId());
            if (c != null) {
                r.setAuthor(c.getAuthor()).setCommitMessage(c.getMessage());
            }
        }
        return result;
    }

    @Override
    public List<Archive<VirtualServer>> getAllVsArchives(Long id) throws Exception {
        List<MetaVsArchiveDo> archives = archiveVsDao.findByVs(id, ArchiveVsEntity.READSET_CONTENT_EXCLUDED);
        Long[] archiveIds = new Long[archives.size()];
        for (int i = 0; i < archives.size(); i++) {
            archiveIds[i] = archives.get(i).getId();
        }

        Map<Long, ArchiveCommitDo> commitByArchiveId = new HashMap<>();
        for (ArchiveCommitDo d : archiveCommitDao.findAllByArchiveAndType(archiveIds, MetaType.VS.getId(), ArchiveCommitEntity.READSET_FULL)) {
            commitByArchiveId.put(d.getArchiveId(), d);
        }
        List<Archive<VirtualServer>> result = new ArrayList<>(archives.size());
        for (MetaVsArchiveDo e : archives) {
            Archive<VirtualServer> r = new Archive<VirtualServer>().setId(e.getVsId()).setVersion(e.getVersion()).setCreatedTime(e.getDateTimeLastChange());
            result.add(r);

            ArchiveCommitDo c = commitByArchiveId.get(e.getId());
            if (c != null) {
                r.setAuthor(c.getAuthor()).setCommitMessage(c.getMessage());
            }
        }
        return result;
    }
}
