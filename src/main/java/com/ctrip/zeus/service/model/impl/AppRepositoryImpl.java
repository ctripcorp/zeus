package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.GroupDo;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupServer;
import com.ctrip.zeus.service.model.handler.GroupQuery;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.handler.GroupSync;
import com.ctrip.zeus.service.model.ArchiveService;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
@Repository("groupRepository")
public class AppRepositoryImpl implements GroupRepository {
    @Resource
    private GroupSync groupSync;
    @Resource
    private GroupQuery groupQuery;
    @Resource
    private ArchiveService archiveService;

    @Override
    public List<Group> list() throws Exception {
        List<Group> list = new ArrayList<>();
        for (Group group : groupQuery.getAll()) {
            list.add(group);
        }
        return list;

    }

    @Override
    public List<Group> list(String slbName, String virtualServerName) throws Exception {
        List<Group> list = new ArrayList<>();
        for (Group group : groupQuery.getBySlbAndVirtualServer(slbName, virtualServerName)) {
            list.add(group);
        }
        return list;
    }

    @Override
    public List<Group> listLimit(long fromId, int maxCount) throws Exception {
        List<Group> list = new ArrayList<>();
        for (Group group : groupQuery.getLimit(fromId, maxCount)) {
            list.add(group);
        }
        return list;
    }

    @Override
    public Group get(String groupName) throws Exception {
        return groupQuery.get(groupName);
    }

    @Override
    public Group getByAppId(String groupId) throws Exception {
        return groupQuery.getByAppId(groupId);
    }

    @Override
    public long add(Group group) throws Exception {
        GroupDo d = groupSync.add(group);
        archiveService.archiveGroup(groupQuery.getById(d.getId()));
        return d.getKeyId();

    }

    @Override
    public void update(Group group) throws Exception {
        if (group == null)
            return;
        GroupDo d = groupSync.update(group);
        group = groupQuery.getById(d.getId());
        archiveService.archiveGroup(group);
    }

    @Override
    public int delete(String groupName) throws Exception {
        int count = groupSync.delete(groupName);
        archiveService.deleteGroupArchive(groupName);
        return count;

    }

    @Override
    public List<String> listGroupsByGroupServer(String groupServerIp) throws Exception {
        return groupQuery.getByGroupServer(groupServerIp);
    }

    @Override
    public List<String> listGroupServersByGroup(String groupName) throws Exception {
        return groupQuery.getGroupServersByGroup(groupName);
    }

    @Override
    public List<GroupServer> getGroupServersByGroup(String groupName) throws Exception {
        return groupQuery.listGroupServersByGroup(groupName);
    }
}
