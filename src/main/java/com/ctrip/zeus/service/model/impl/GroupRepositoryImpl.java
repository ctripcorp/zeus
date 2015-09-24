package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.GroupDao;
import com.ctrip.zeus.dal.core.GroupDo;
import com.ctrip.zeus.dal.core.GroupEntity;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupServer;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.handler.GroupSync;
import com.ctrip.zeus.service.model.handler.GroupValidator;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.support.C;
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
    private GroupSync groupEntityManager;
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
    @Resource
    private GroupDao groupDao;

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
        throw new ValidationException("Group does not exist.");
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
        groupEntityManager.add(group);
        syncVsAndGs(group);
        return group;
    }

    @Override
    public Group update(Group group) throws Exception {
        groupModelValidator.validate(group);
        groupEntityManager.update(group);
        syncVsAndGs(group);
        return group;
    }

    @Override
    public List<Group> updateVersion(Long[] groupIds) throws Exception {
        List<Group> result = new ArrayList<>();
        for (Long groupId : groupIds) {
            Group g = fresh(groupId);
            if (g != null)
                groupEntityManager.update(g);
            result.add(g);
        }
        return result;
    }

    @Override
    public int delete(Long groupId) throws Exception {
        groupModelValidator.removable(groupId);
        cascadeRemoveByGroup(groupId);
        return groupEntityManager.delete(groupId);
    }

    @Override
    public List<Group> listGroupsByGroupServer(String groupServerIp) throws Exception {
        Long[] groupIds = groupMemberRepository.findGroupsByGroupServerIp(groupServerIp);
        return list(groupIds);
    }

    private Group fresh(Long groupId) throws Exception {
        GroupDo d = groupDao.findById(groupId, GroupEntity.READSET_FULL);
        if (d == null)
            return null;
        Group group = C.toGroup(d);
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
