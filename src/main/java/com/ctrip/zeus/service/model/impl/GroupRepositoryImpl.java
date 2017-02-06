package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.ArchiveGroupDao;
import com.ctrip.zeus.dal.core.ArchiveGroupDo;
import com.ctrip.zeus.dal.core.ArchiveGroupEntity;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.common.ValidationContext;
import com.ctrip.zeus.service.model.handler.GroupSync;
import com.ctrip.zeus.service.model.handler.GroupValidator;
import com.ctrip.zeus.service.model.handler.VGroupValidator;
import com.ctrip.zeus.service.model.handler.impl.ContentReaders;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.service.status.StatusService;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Repository;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
    private GroupValidator groupModelValidator;
    @Resource
    private ValidationFacade validationFacade;
    @Resource
    private VGroupValidator vGroupValidator;
    @Resource
    private StatusService statusService;
    @Resource
    private VirtualServerRepository virtualServerRepository;
    @Resource
    private ArchiveGroupDao archiveGroupDao;

    @Override
    public List<Group> list(Long[] ids) throws Exception {
        Set<IdVersion> keys = groupCriteriaQuery.queryByIdsAndMode(ids, SelectionMode.OFFLINE_FIRST);
        return list(keys.toArray(new IdVersion[keys.size()]));
    }

    @Override
    public List<Group> list(IdVersion[] keys) throws Exception {
        return list(keys, new RepositoryContext(false, SelectionMode.OFFLINE_FIRST));
    }

    @Override
    public List<Group> list(IdVersion[] keys, RepositoryContext repositoryContext) throws Exception {
        List<Group> result = new ArrayList<>();
        Integer[] hashes = new Integer[keys.length];
        String[] values = new String[keys.length];
        for (int i = 0; i < hashes.length; i++) {
            hashes[i] = keys[i].hashCode();
            values[i] = keys[i].toString();
        }
        for (ArchiveGroupDo d : archiveGroupDao.findAllByIdVersion(hashes, values, ArchiveGroupEntity.READSET_FULL)) {
            try {
                Group group = ContentReaders.readGroupContent(d.getContent());
                group.setCreatedTime(d.getDataChangeLastTime());
                result.add(group);
            } catch (Exception e) {
            }
        }

        for (Group group : result) {
            autoFiller.autofill(group);
            hideVirtualValue(group);
        }

        if (!repositoryContext.isLite()) {
            Set<Long> vsIds = new HashSet<>();
            for (Group group : result) {
                for (GroupVirtualServer groupVirtualServer : group.getGroupVirtualServers()) {
                    vsIds.add(groupVirtualServer.getVirtualServer().getId());
                }
            }
            Map<Long, VirtualServer> map = buildVsMapping(vsIds.toArray(new Long[vsIds.size()]), repositoryContext.getSelectionMode());
            for (Group group : result) {
                for (GroupVirtualServer groupVirtualServer : group.getGroupVirtualServers()) {
                    groupVirtualServer.setVirtualServer(map.get(groupVirtualServer.getVirtualServer().getId()));
                }
            }
        }
        return result;
    }

    @Override
    public Group getById(Long id) throws Exception {
        IdVersion[] key = groupCriteriaQuery.queryByIdAndMode(id, SelectionMode.OFFLINE_FIRST);
        if (key.length == 0)
            return null;
        return getByKey(key[0]);
    }

    @Override
    public Group getByKey(IdVersion key) throws Exception {
        return getByKey(key, new RepositoryContext(false, SelectionMode.OFFLINE_FIRST));
    }

    @Override
    public Group getByKey(IdVersion key, RepositoryContext repositoryContext) throws Exception {
        if (!groupModelValidator.exists(key.getId()) && !vGroupValidator.exists(key.getId())) {
            return null;
        }

        ArchiveGroupDo d = archiveGroupDao.findByGroupAndVersion(key.getId(), key.getVersion(), ArchiveGroupEntity.READSET_FULL);
        if (d == null) return null;

        Group result = ContentReaders.readGroupContent(d.getContent());
        autoFiller.autofill(result);
        hideVirtualValue(result);

        if (!repositoryContext.isLite()) {
            Set<Long> vsIds = new HashSet<>();
            for (GroupVirtualServer e : result.getGroupVirtualServers()) {
                vsIds.add(e.getVirtualServer().getId());
            }
            Map<Long, VirtualServer> map = buildVsMapping(vsIds.toArray(new Long[vsIds.size()]), repositoryContext.getSelectionMode());

            for (GroupVirtualServer e : result.getGroupVirtualServers()) {
                e.setVirtualServer(map.get(e.getVirtualServer().getId()));
            }
        }

        result.setCreatedTime(d.getDataChangeLastTime());
        return result;
    }

    @Override
    public Group add(Group group, boolean escapedPathValidation) throws Exception {
        group.setId(0L);
        ValidationContext context = new ValidationContext();
        validationFacade.validateGroup(group, context);
        if (escapedPathValidation) {
            //TODO filter by error type
        } else {
            if (context.getErrorGroups().contains(group.getId())) {
                throw new ValidationException(context.getGroupErrorReason(group.getId()));
            }
        }
        autoFiller.autofill(group);
        hideVirtualValue(group);
        groupEntityManager.add(group, false);
        syncMemberStatus(group);
        return getByKey(new IdVersion(group.getId(), group.getVersion()));
    }

    @Override
    public Group add(Group group) throws Exception {
        return add(group, false);
    }

    @Override
    public Group addVGroup(Group group) throws Exception {
        return addVGroup(group, false);
    }

    @Override
    public Group addVGroup(Group group, boolean escapedPathValidation) throws Exception {
        group.setId(null);
        vGroupValidator.validate(group, escapedPathValidation);
        autoFiller.autofillVGroup(group);
        group.setVirtual(true);
        groupEntityManager.add(group, true);
        hideVirtualValue(group);
        return getByKey(new IdVersion(group.getId(), group.getVersion()));
    }

    @Override
    public Group update(Group group) throws Exception {
        return update(group, false);
    }

    @Override
    public Group update(Group group, boolean escapedPathValidation) throws Exception {
        if (!groupModelValidator.exists(group.getId()))
            throw new ValidationException("Group with id " + group.getId() + " does not exist.");
        ValidationContext context = new ValidationContext();
        validationFacade.validateGroup(group, context);
        if (escapedPathValidation) {
            //TODO filter by error type
        } else {
            if (context.getErrorGroups().contains(group.getId())) {
                throw new ValidationException(context.getGroupErrorReason(group.getId()));
            }
        }
        autoFiller.autofill(group);
        hideVirtualValue(group);
        groupEntityManager.update(group);
        syncMemberStatus(group);
        return getByKey(new IdVersion(group.getId(), group.getVersion()));
    }

    @Override
    public Group updateVGroup(Group group) throws Exception {
        return updateVGroup(group, false);
    }

    @Override
    public Group updateVGroup(Group group, boolean escapedPathValidation) throws Exception {
        if (!vGroupValidator.exists(group.getId()))
            throw new ValidationException("Group with id " + group.getId() + " does not exist.");
        vGroupValidator.validate(group, escapedPathValidation);
        autoFiller.autofillVGroup(group);
        group.setVirtual(true);
        groupEntityManager.update(group);
        hideVirtualValue(group);
        return getByKey(new IdVersion(group.getId(), group.getVersion()));
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
    public void updateStatus(IdVersion[] groups, SelectionMode state) throws Exception {
        switch (state) {
            case ONLINE_EXCLUSIVE:
                List<Group> result = new ArrayList<>();
                for (int i = 0; i < groups.length; i++) {
                    if (groups[i].getVersion() == 0) {
                        result.add(new Group().setId(groups[i].getId()).setVersion(groups[i].getVersion()));
                    }
                }
                result.addAll(list(groups));
                groupEntityManager.updateStatus(result);
                return;
            default:
                throw new NotImplementedException();
        }
    }

    @Override
    public void updateStatus(IdVersion[] groups) throws Exception {
        updateStatus(groups, SelectionMode.ONLINE_EXCLUSIVE);
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

    private Map<Long, VirtualServer> buildVsMapping(Long[] vsIds, SelectionMode selectionMode) throws Exception {
        Set<IdVersion> vsKeys = virtualServerCriteriaQuery.queryByIdsAndMode(vsIds, selectionMode);
        Map<Long, VirtualServer> map = Maps.uniqueIndex(
                virtualServerRepository.listAll(vsKeys.toArray(new IdVersion[vsKeys.size()])),
                new Function<VirtualServer, Long>() {
                    @Nullable
                    @Override
                    public Long apply(@Nullable VirtualServer virtualServer) {
                        return virtualServer.getId();
                    }
                });
        return map;
    }

}
