package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupServer;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.handler.GroupQuery;
import com.ctrip.zeus.service.model.handler.GroupSync;
import com.ctrip.zeus.service.model.handler.GroupValidator;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
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
    private GroupCriteriaQuery groupCriteriaQuery;
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
        Set<Long> groupIds = groupCriteriaQuery.queryAll();
        return list(groupIds.toArray(new Long[groupIds.size()]));
    }

    @Override
    public List<Group> list(Long slbId) throws Exception {
        Set<Long> groupIds = groupCriteriaQuery.queryBySlbId(slbId);
        return list(groupIds.toArray(new Long[groupIds.size()]));
    }

    @Override
    public List<Group> list(Long[] ids) throws Exception {
        return archiveService.getLatestGroups(ids);
    }

    @Override
    public Group getById(Long id) throws Exception {
        if (groupModelValidator.exists(id)) {
            return archiveService.getLatestGroup(id);
        }
        return null;
    }

    @Override
    public Group get(String groupName) throws Exception {
        return getById(groupCriteriaQuery.queryByName(groupName));
    }

    @Override
    public List<Group> listByAppId(String appId) throws Exception {
        Set<Long> groupIds = groupCriteriaQuery.queryByAppId(appId);
        return archiveService.getLatestGroups(groupIds.toArray(new Long[groupIds.size()]));
    }

    @Override
    public Group add(Group group) throws Exception {
        groupModelValidator.validate(group);
        Long groupId = groupSync.add(group);
        group.setId(groupId);
        syncVsAndGs(group);
        return archive(groupId);
    }

    @Override
    public Group update(Group group) throws Exception {
        groupModelValidator.validate(group);
        Long groupId = groupSync.update(group);
        group.setId(groupId);
        syncVsAndGs(group);
        return archive(groupId);
    }

    @Override
    public List<Group> updateVersion(Long[] groupIds) throws Exception {
        List<Group> result = new ArrayList<>();
        groupSync.updateVersion(groupIds);
        for (Long groupId : groupIds) {
            Group group = archive(groupId);
            result.add(group);
        }
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
        return list(groupIds);
    }

    private Group archive(Long groupId) throws Exception {
        Group group = groupQuery.getById(groupId);
        for (GroupVirtualServer groupVirtualServer : virtualServerRepository.listGroupVsByGroups(new Long[]{group.getId()})) {
            group.addGroupVirtualServer(groupVirtualServer);
        }
        for (GroupServer server : groupMemberRepository.listGroupServersByGroup(group.getId())) {
            group.addGroupServer(server);
        }
        archiveService.archiveGroup(group);
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
