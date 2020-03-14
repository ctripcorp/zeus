package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dao.entity.*;
import com.ctrip.zeus.dao.mapper.SlbArchiveGroupMapper;
import com.ctrip.zeus.dao.mapper.SlbGroupMapper;
import com.ctrip.zeus.dao.mapper.SlbGroupStatusRMapper;
import com.ctrip.zeus.model.model.Group;
import com.ctrip.zeus.service.model.VersionUtils;
import com.ctrip.zeus.service.model.handler.GroupSync;
import com.ctrip.zeus.service.model.handler.MultiRelMaintainer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by zhoumy on 2015/9/23.
 */
@Component("groupEntityManager")
public class GroupEntityManager implements GroupSync {
    @Resource
    private SlbGroupMapper slbGroupMapper;

    @Resource
    private SlbArchiveGroupMapper slbArchiveGroupMapper;

    @Resource
    private SlbGroupStatusRMapper slbGroupStatusRMapper;

    @Resource
    private MultiRelMaintainer slbGroupGsRelMaintainer;

    @Resource
    private MultiRelMaintainer slbGroupVsRelMaintainer;

    @Override
    public void add(Group group) throws Exception {
        group.setVersion(1);
        SlbGroup d = parse(group.getId(), group);
        if (d.getAppId() == null) d.setAppId("000000");
        if (group.getId() == null || group.getId().equals(0L)) {
            slbGroupMapper.insert(d);
            group.setId(d.getId());
        } else {
            slbGroupMapper.insertIncludeId(d);
        }

        slbArchiveGroupMapper.insert(SlbArchiveGroup.builder().
                groupId(group.getId()).
                version(group.getVersion()).
                content(ContentWriters.writeGroupContent(group)).
                hash(VersionUtils.getHash(group.getId(), group.getVersion()))
                .build());
        slbGroupStatusRMapper.upsert(SlbGroupStatusR.builder().
                groupId(group.getId()).
                onlineVersion(0).
                offlineVersion(group.getVersion()).build());

        slbGroupVsRelMaintainer.insert(group);
        slbGroupGsRelMaintainer.insert(group);
    }

    @Override
    public void add(Group group, boolean isVirtual) throws Exception {
        add(group);
    }

    @Override
    public void update(Group group) throws Exception {
        group.setVersion(group.getVersion() + 1);
        SlbGroup d = parse(group.getId(), group);
        if (d.getAppId() == null) d.setAppId("000000");

        slbGroupMapper.updateByExampleSelective(d, new SlbGroupExample().createCriteria().andIdEqualTo(d.getId()).example());
        slbArchiveGroupMapper.insert(SlbArchiveGroup.
                builder().
                groupId(group.getId()).
                version(group.getVersion()).
                content(ContentWriters.writeGroupContent(group)).
                hash(VersionUtils.getHash(group.getId(), group.getVersion())).build());

        slbGroupVsRelMaintainer.refreshOffline(group);
        slbGroupGsRelMaintainer.refreshOffline(group);
        slbGroupStatusRMapper.insertOrUpdate(SlbGroupStatusR.builder().groupId(group.getId()).offlineVersion(group.getVersion()).build());
    }

    @Override
    public void updateStatus(List<Group> groups) throws Exception {
        if (groups.size() == 0) return;
        SlbGroupStatusR[] dos = new SlbGroupStatusR[groups.size()];
        for (int i = 0; i < dos.length; i++) {
            dos[i] = SlbGroupStatusR.builder().groupId(groups.get(i).getId()).onlineVersion(groups.get(i).getVersion()).build();
        }

        Group[] array = groups.toArray(new Group[groups.size()]);
        slbGroupVsRelMaintainer.refreshOnline(array);
        slbGroupGsRelMaintainer.refreshOnline(array);

        slbGroupStatusRMapper.updateOnlineVersionByGroup(Arrays.asList(dos));
    }

    @Override
    public int delete(Long groupId) throws Exception {
        slbGroupVsRelMaintainer.clear(groupId);
        slbGroupGsRelMaintainer.clear(groupId);
        slbGroupStatusRMapper.deleteByExample(new SlbGroupStatusRExample().createCriteria().andGroupIdEqualTo(groupId).example());
        int count = slbGroupMapper.deleteByPrimaryKey(groupId);
        slbArchiveGroupMapper.deleteByExample(new SlbArchiveGroupExample().createCriteria().andGroupIdEqualTo(groupId).example());
        return count;
    }

    private SlbGroup parse(Long groupId, Group e) {
        return SlbGroup.builder().
                id(groupId).
                appId(e.getAppId()).
                name(e.getName()).
                ssl(e.isSsl()).
                version(e.getVersion()).
                createdTime(new Date()).
                build();
    }
}