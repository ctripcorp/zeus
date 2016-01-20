package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.handler.GroupSync;
import com.ctrip.zeus.service.model.handler.GroupValidator;
import com.ctrip.zeus.service.model.handler.VGroupValidator;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
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
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
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
        Set<IdVersion> keys = groupCriteriaQuery.queryByIdsAndMode(ids, ModelMode.MODEL_MODE_MERGE_OFFLINE);
        return list(keys.toArray(new IdVersion[keys.size()]));
    }

    @Override
    public List<Group> list(IdVersion[] keys) throws Exception {
        List<Group> result = archiveService.listGroups(keys);
        Set<Long> vsIds = new HashSet<>();
        for (Group group : result) {
            for (GroupVirtualServer groupVirtualServer : group.getGroupVirtualServers()) {
                vsIds.add(groupVirtualServer.getVirtualServer().getId());
            }
        }
        Set<IdVersion> vsKeys = virtualServerCriteriaQuery.queryByIdsAndMode(vsIds.toArray(new Long[vsIds.size()]), ModelMode.MODEL_MODE_MERGE_ONLINE);
        Map<Long, VirtualServer> map = Maps.uniqueIndex(
                virtualServerRepository.listAll(vsKeys.toArray(new IdVersion[vsKeys.size()])),
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
        IdVersion[] key = groupCriteriaQuery.queryByIdAndMode(id, ModelMode.MODEL_MODE_MERGE_OFFLINE);
        if (key.length == 0)
            return null;
        return getByKey(key[0]);
    }

    @Override
    public Group getByKey(IdVersion key) throws Exception {
        if (groupModelValidator.exists(key.getId()) || vGroupValidator.exists(key.getId())) {
            Group result = archiveService.getGroup(key.getId(), key.getVersion());
            for (GroupVirtualServer groupVirtualServer : result.getGroupVirtualServers()) {
                IdVersion[] vsKey = virtualServerCriteriaQuery.queryByIdAndMode(groupVirtualServer.getVirtualServer().getId(), ModelMode.MODEL_MODE_MERGE_ONLINE);
                groupVirtualServer.setVirtualServer(virtualServerRepository.getByKey(vsKey[0]));
            }
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
    public void updateStatus(Group[] groups) throws Exception {
        groupEntityManager.updateStatus(groups);
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
