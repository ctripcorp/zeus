package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dao.entity.*;
import com.ctrip.zeus.dao.mapper.*;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.restful.message.view.ExtendedView;
import com.ctrip.zeus.service.model.ArchiveRepository;
import com.ctrip.zeus.service.model.handler.impl.ContentReaders;
import com.ctrip.zeus.service.model.handler.impl.ContentWriters;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.ctrip.zeus.util.CompressUtils;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * Created by zhoumy on 2016/5/17.
 */
@Repository("archiveRepository")
public class ArchiveRepositoryImpl implements ArchiveRepository {

    @Resource
    private SlbGroupMapper slbGroupMapper;

    @Resource
    private SlbGroupHistoryMapper slbGroupHistoryMapper;

    @Resource
    private SlbArchiveGroupMapper slbArchiveGroupMapper;

    @Resource
    private SlbArchiveSlbMapper slbArchiveSlbMapper;

    @Resource
    private SlbArchiveVsMapper slbArchiveVsMapper;

    @Resource
    private SlbArchiveDrMapper slbArchiveDrMapper;

    @Resource
    private SlbArchiveTrafficPolicyMapper slbArchiveTrafficPolicyMapper;

    @Override
    public void archiveGroup(Group group) throws Exception {
        slbGroupHistoryMapper.insert(SlbGroupHistory.builder().groupId(group.getId()).groupName(group.getName()).build());
        slbArchiveGroupMapper.insert(SlbArchiveGroup.builder().groupId(group.getId()).hash(0).version(0).
                content(ContentWriters.writeGroupContent(group)).build());
    }

    @Override
    public void archiveGroup(ExtendedView.ExtendedGroup group) throws Exception {
        slbGroupHistoryMapper.insert(SlbGroupHistory.builder().groupId(group.getId()).groupName(group.getName()).build());
        slbArchiveGroupMapper.insert(SlbArchiveGroup.builder().groupId(group.getId()).hash(0).version(0).content(ContentWriters.write(group)).build());
    }

    @Override
    public void archiveSlb(Slb slb) throws Exception {
        slbArchiveSlbMapper.insert(SlbArchiveSlb.builder().slbId(slb.getId()).hash(0).version(0).content(ContentWriters.writeSlbContent(slb)).build());
    }

    @Override
    public void archiveVs(VirtualServer vs) throws Exception {
        slbArchiveVsMapper.insert(SlbArchiveVs.builder().vsId(vs.getId()).hash(0).version(0).content(ContentWriters.writeVirtualServerContent(vs)).build());
    }

    @Override
    public void archivePolicy(TrafficPolicy trafficPolicy) throws Exception {
        slbArchiveTrafficPolicyMapper.insert(SlbArchiveTrafficPolicy.builder().
                policyId(trafficPolicy.getId()).policyName(trafficPolicy.getName()).
                version(0).
                content(CompressUtils.compressToGzippedBase64String(ContentWriters.write(trafficPolicy))).
                build());
    }

    @Override
    public void archiveDr(Dr dr) throws Exception {
        slbArchiveDrMapper.insert(SlbArchiveDr.builder()
                .drId(dr.getId())
                .version(0)
                .content(ContentWriters.writeDrContent(dr))
                .build());
    }

    @Override
    public void archiveDr(ExtendedView.ExtendedDr dr) throws Exception {
        slbArchiveDrMapper.insert(SlbArchiveDr.builder()
                .drId(dr.getId())
                .version(0)
                .content(ContentWriters.write(dr))
                .build());
    }

    @Override
    public String getGroupArchiveRaw(Long id, int version) throws Exception {
        SlbArchiveGroup slbArchiveGroup = slbArchiveGroupMapper.selectOneByExampleWithBLOBs(new SlbArchiveGroupExample().createCriteria().andGroupIdEqualTo(id).andVersionEqualTo(version).example());
        return slbArchiveGroup == null ? null : slbArchiveGroup.getContent();
    }

    @Override
    public Group getGroupArchive(Long id, int version) throws Exception {
        SlbArchiveGroup slbArchiveGroup = slbArchiveGroupMapper.selectOneByExampleWithBLOBs(new SlbArchiveGroupExample().createCriteria().andGroupIdEqualTo(id).andVersionEqualTo(version).example());
        return slbArchiveGroup == null ? null : ContentReaders.readGroupContent(slbArchiveGroup.getContent());
    }

    @Override
    public Group getGroupArchive(String name, int version) throws Exception {
        Long groupId;
        if (version == 0) {
            SlbGroupHistory d = slbGroupHistoryMapper.selectOneByExampleSelective(new SlbGroupHistoryExample().createCriteria().andGroupNameEqualTo(name).example(), SlbGroupHistory.Column.groupId);
            if (d == null) return null;
            groupId = d.getGroupId();
        } else {
            SlbGroup d = slbGroupMapper.selectOneByExampleSelective(new SlbGroupExample().createCriteria().andNameEqualTo(name).example(), SlbGroup.Column.id);
            if (d == null) return null;
            groupId = d.getId();
        }

        SlbArchiveGroup slbArchiveGroup = slbArchiveGroupMapper.selectOneByExampleWithBLOBs(new SlbArchiveGroupExample().createCriteria().andGroupIdEqualTo(groupId).andVersionEqualTo(version).example());
        return slbArchiveGroup == null ? null : ContentReaders.readGroupContent(slbArchiveGroup.getContent());
    }

    @Override
    public Slb getSlbArchive(Long id, int version) throws Exception {
        SlbArchiveSlb slb = slbArchiveSlbMapper.selectOneByExampleWithBLOBs(new SlbArchiveSlbExample().createCriteria().andSlbIdEqualTo(id).andVersionEqualTo(version).example());
        return slb == null ? null : ContentReaders.readSlbContent(slb.getContent());
    }

    @Override
    public VirtualServer getVsArchive(Long id, int version) throws Exception {
        SlbArchiveVs slbArchiveVs = slbArchiveVsMapper.selectOneByExampleWithBLOBs(new SlbArchiveVsExample().createCriteria().andVsIdEqualTo(id).andVersionEqualTo(version).example());
        return slbArchiveVs == null ? null : ContentReaders.readVirtualServerContent(slbArchiveVs.getContent());
    }

    @Override
    public TrafficPolicy getPolicyArchive(Long id, String name) throws Exception {
        SlbArchiveTrafficPolicy d = null;
        if (id != null && id > 0L) {
            d = slbArchiveTrafficPolicyMapper.selectByPrimaryKey(id);
        }
        if (d == null && name != null) {
            d = slbArchiveTrafficPolicyMapper.selectOneByExample(new SlbArchiveTrafficPolicyExample().createCriteria().andPolicyNameEqualTo(name).example());
        }
        return d == null ? null : ObjectJsonParser.parse(CompressUtils.decompressGzippedBase64String(d.getContent()), TrafficPolicy.class);
    }

    @Override
    public Dr getDrArchive(Long id, int version) throws Exception {
        SlbArchiveDr archiveDr = slbArchiveDrMapper.selectOneByExampleWithBLOBs(new SlbArchiveDrExample().createCriteria().andDrIdEqualTo(id).andVersionEqualTo(version).example());
        return archiveDr == null ? null : ContentReaders.readDrContent(archiveDr.getContent());
    }
}
