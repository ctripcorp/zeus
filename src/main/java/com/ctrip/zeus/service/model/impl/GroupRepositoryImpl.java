package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupServer;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.handler.GroupQuery;
import com.ctrip.zeus.service.model.handler.GroupSync;
import com.ctrip.zeus.service.model.handler.GroupValidator;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    @Resource
    private GroupValidator groupModelValidator;

    @Override
    public List<Group> list() throws Exception {
        List<Group> list = new ArrayList<>();
        for (Group group : groupQuery.getAll()) {
            cascadeVsAndGs(group);
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
            cascadeVsAndGs(group);
            result.add(group);
        }
        return result;
    }

    @Override
    public List<Group> list(Long[] ids) throws Exception {
        List<Group> result = groupQuery.batchGet(ids);
        for (Group group : result) {
            cascadeVsAndGs(group);
        }
        return result;
    }

    @Override
    public Group getById(Long id) throws Exception {
        return cascadeVsAndGs(groupQuery.getById(id));
    }

    @Override
    public Group get(String groupName) throws Exception {
        return cascadeVsAndGs(groupQuery.get(groupName));
    }

    @Override
    public Group getByAppId(String appId) throws Exception {
        return cascadeVsAndGs(groupQuery.getByAppId(appId));
    }

    @Override
    public Group add(Group group) throws Exception {
        groupModelValidator.validate(group);
        Long groupId = groupSync.add(group);
        group.setId(groupId);
        syncVsAndGs(group);
        Group result = getById(groupId);
        archiveService.archiveGroup(result);
        return result;

    }

    @Override
    public Group update(Group group) throws Exception {
        groupModelValidator.validate(group);
        Long groupId = groupSync.update(group);
        group.setId(groupId);
        syncVsAndGs(group);
        Group result = getById(groupId);
        archiveService.archiveGroup(result);
        return result;
    }

    @Override
    public int delete(Long groupId) throws Exception {
        groupModelValidator.removable(groupId);
        cascadeRemoveByGroup(groupId);
        int count = groupSync.delete(groupId);
        return count;

    }

    @Override
    public List<Group> listGroupsByGroupServer(String groupServerIp) throws Exception {
        Long[] groupIds = groupMemberRepository.findGroupsByGroupServerIp(groupServerIp);
        List<Group> result = groupQuery.batchGet(groupIds);
        for (Group group : result) {
            cascadeVsAndGs(group);
        }
        return result;
    }

    private Group cascadeVsAndGs(Group group) throws Exception {
        for (GroupVirtualServer groupVirtualServer : virtualServerRepository.listGroupVsByGroups(new Long[]{group.getId()})) {
            group.addGroupVirtualServer(groupVirtualServer);
        }
        for (GroupServer server : groupMemberRepository.listGroupServersByGroup(group.getId())) {
            group.addGroupServer(server);
        }
        return group;
    }

    private void syncVsAndGs(Group group) throws Exception {
        Long groupId = group.getId();
        virtualServerRepository.updateGroupVirtualServers(groupId, group.getGroupVirtualServers());

        Set<String> originIps = new HashSet<>(groupMemberRepository.listGroupServerIpsByGroup(groupId));
        Set<String> inputIps = new HashSet<>();
        for (GroupServer groupServer : group.getGroupServers()) {
            inputIps.add(groupServer.getIp());
            if (originIps.contains(groupServer.getIp()))
                groupMemberRepository.updateGroupServer(groupId, groupServer);
            else
                groupMemberRepository.addGroupServer(groupId, groupServer);
        }
        originIps.removeAll(inputIps);
        for (String originIp : originIps) {
            groupMemberRepository.removeGroupServer(groupId, originIp);
        }
    }

    private void cascadeRemoveByGroup(Long groupId) throws Exception {
        virtualServerRepository.batchDeleteGroupVirtualServers(groupId);
        groupMemberRepository.removeGroupServer(groupId, null);
    }
}
