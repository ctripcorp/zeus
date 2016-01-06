package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.handler.GroupSync;
import com.ctrip.zeus.service.model.handler.GroupValidator;
import com.ctrip.zeus.service.model.handler.VGroupValidator;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.status.StatusService;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.*;

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
    private AutoFiller autoFiller;
    @Resource
    private ArchiveService archiveService;
    @Resource
    private GroupValidator groupModelValidator;
    @Resource
    private VGroupValidator vGroupValidator;
    @Resource
    private StatusService statusService;
    @Resource
    private VirtualServerRepository virtualServerRepository;

    @Override
    public List<Group> list(Long[] ids) throws Exception {
        return list(ids, ModelMode.MODEL_MODE_MERGE);
    }

    @Override
    public List<Group> list(Long[] ids, ModelMode mode) throws Exception {
        List<Group> result = archiveService.getGroupsByMode(ids, mode);
        Set<Long> vsIds = new HashSet<>();
        for (Group group : result) {
            for (GroupVirtualServer groupVirtualServer : group.getGroupVirtualServers()) {
                vsIds.add(groupVirtualServer.getVirtualServer().getId());
            }
        }
        Map<Long, VirtualServer> map = Maps.uniqueIndex(
                virtualServerRepository.listAll(vsIds.toArray(new Long[vsIds.size()]), mode),
                new Function<VirtualServer, Long>() {
                    @Nullable
                    @Override
                    public Long apply(@Nullable VirtualServer virtualServer) {
                        return virtualServer.getId();
                    }
                });
        for (Group group : result) {
            for (GroupVirtualServer groupVirtualServer : group.getGroupVirtualServers()) {
                groupVirtualServer.setVirtualServer(map.get(groupVirtualServer.getVirtualServer().getId()));
            }
            autoFiller.autofillEmptyFields(group);
            hideVirtualValue(group);
        }
        return result;
    }

    @Override
    public Group getById(Long id) throws Exception {
        return getById(id, ModelMode.MODEL_MODE_MERGE);
    }

    @Override
    public Group getById(Long id, ModelMode mode) throws Exception {
        if (groupModelValidator.exists(id) || vGroupValidator.exists(id)) {
            Group result = archiveService.getGroupByMode(id, mode);
            autoFiller.autofill(result);
            hideVirtualValue(result);
            return result;
        }
        return null;
    }

    @Override
    public Group add(Group group) throws Exception {
        groupModelValidator.validate(group);
        autoFiller.autofill(group);
        hideVirtualValue(group);
        groupEntityManager.add(group, false);
        syncMemberStatus(group);
        return group;
    }

    @Override
    public Group addVGroup(Group group) throws Exception {
        vGroupValidator.validate(group);
        autoFiller.autofillVGroup(group);
        group.setVirtual(true);
        groupEntityManager.add(group, true);
        hideVirtualValue(group);
        return group;
    }

    @Override
    public Group update(Group group) throws Exception {
        if (!groupModelValidator.exists(group.getId()))
            throw new ValidationException("Group with id " + group.getId() + " does not exist.");
        groupModelValidator.validate(group);
        autoFiller.autofill(group);
        hideVirtualValue(group);
        groupEntityManager.update(group);
        syncMemberStatus(group);
        return group;
    }

    @Override
    public Group updateVGroup(Group group) throws Exception {
        if (!vGroupValidator.exists(group.getId()))
            throw new ValidationException("Group with id " + group.getId() + " does not exist.");
        vGroupValidator.validate(group);
        autoFiller.autofillVGroup(group);
        group.setVirtual(true);
        groupEntityManager.update(group);
        hideVirtualValue(group);
        return group;
    }

    @Override
    public int delete(Long groupId) throws Exception {
        groupModelValidator.removable(groupId);
        statusService.cleanGroupServerStatus(groupId);
        return groupEntityManager.delete(groupId);
    }

    @Override
    public int deleteVGroup(Long groupId) throws Exception {
        vGroupValidator.removable(groupId);
        return delete(groupId);
    }

    @Override
    public Set<Long> port(Long[] groupIds) throws Exception {
        return groupEntityManager.port(groupIds);
    }

    private void syncMemberStatus(Group group) throws Exception {
        List<GroupVirtualServer> virtualServers = group.getGroupVirtualServers();
        Long[] vsIds = new Long[virtualServers.size()];
        for (int i = 0; i < vsIds.length; i++) {
            vsIds[i] = virtualServers.get(i).getVirtualServer().getId();
        }
        List<GroupServer> groupServers = group.getGroupServers();
        String[] ips = new String[groupServers.size()];
        for (int i = 0; i < ips.length; i++) {
            ips[i] = groupServers.get(i).getIp();
        }
        statusService.groupServerStatusInit(group.getId(), vsIds, ips);
    }

    @Override
    public Group get(String groupName) throws Exception {
        return getById(groupCriteriaQuery.queryByName(groupName));
    }

    private void hideVirtualValue(Group group) {
        group.setVirtual(null);
    }

}
