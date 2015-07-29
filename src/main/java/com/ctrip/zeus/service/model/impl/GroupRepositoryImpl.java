package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.GroupDo;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.handler.GroupQuery;
import com.ctrip.zeus.service.model.handler.GroupSync;
import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
@Repository("groupRepository")
public class GroupRepositoryImpl implements GroupRepository {
    @Resource
    private GroupSync groupSync;
    @Resource
    private GroupQuery groupQuery;
    @Resource
    private VirtualServerRepository virtualServerRepository;
    @Resource
    private GroupMemberRepository groupMemberRepository;
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
    public List<Group> list(Long slbId, String virtualServerName) throws Exception {
        if (virtualServerName == null)
            return groupQuery.getBySlb(slbId);

        List<Group> result = new ArrayList<>();
        VirtualServer vs = virtualServerRepository.getBySlbAndName(slbId, virtualServerName);
        Long[] groupIds = virtualServerRepository.findGroupsByVirtualServer(vs.getId());
        for (Group group : groupQuery.batchGet(groupIds)) {
            result.add(group);
        }
        return result;
    }

    @Override
    public List<Group> list(Long[] ids) throws Exception {
        return groupQuery.batchGet(ids);
    }

    @Override
    public Group getById(Long id) throws Exception {
        return groupQuery.getById(id);
    }

    @Override
    public Group get(String groupName) throws Exception {
        return groupQuery.get(groupName);
    }

    @Override
    public Group getByAppId(String appId) throws Exception {
        return groupQuery.getByAppId(appId);
    }

    @Override
    public Group add(Group group) throws Exception {
        GroupDo d = groupSync.add(group);
        archiveService.archiveGroup(groupQuery.getById(d.getId()));
        return C.toGroup(d);

    }

    @Override
    public Group update(Group group) throws Exception {
        if (group == null)
            return null;
        GroupDo d = groupSync.update(group);
        group = groupQuery.getById(d.getId());
        archiveService.archiveGroup(group);
        return group;
    }

    @Override
    public int delete(Long groupId) throws Exception {
        int count = groupSync.delete(groupId);
        return count;

    }

    @Override
    public List<String> listGroupsByGroupServer(String groupServerIp) throws Exception {
        Long[] groupIds = groupMemberRepository.findGroupsByGroupServerIp(groupServerIp);
        List<String> result = new ArrayList<>();
        for (Group group : groupQuery.batchGet(groupIds)) {
            result.add(group.getName());
        }
        return result;
    }
}
