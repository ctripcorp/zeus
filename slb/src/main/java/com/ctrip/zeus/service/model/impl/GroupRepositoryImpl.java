package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dao.entity.SlbArchiveGroup;
import com.ctrip.zeus.dao.entity.SlbArchiveGroupExample;
import com.ctrip.zeus.dao.entity.SlbGroupStatusR;
import com.ctrip.zeus.dao.entity.SlbGroupStatusRExample;
import com.ctrip.zeus.dao.mapper.SlbArchiveGroupMapper;
import com.ctrip.zeus.dao.mapper.SlbGroupStatusRMapper;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.service.SmartArchiveGroupMapper;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.common.ErrorType;
import com.ctrip.zeus.service.model.common.MetaType;
import com.ctrip.zeus.service.model.common.ValidationContext;
import com.ctrip.zeus.service.model.handler.GroupSync;
import com.ctrip.zeus.service.model.handler.impl.ContentReaders;
import com.ctrip.zeus.service.model.validation.GroupValidator;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.service.status.StatusService;
import com.ctrip.zeus.tag.ItemTypes;
import com.ctrip.zeus.tag.PropertyBox;
import com.ctrip.zeus.tag.PropertyNames;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Repository;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
    private StatusService statusService;
    @Resource
    private VirtualServerRepository virtualServerRepository;
    @Resource
    private SlbArchiveGroupMapper slbArchiveGroupMapper;
    @Resource
    private PropertyBox propertyBox;
    @Resource
    private EntityFactory entityFactory;
    @Resource
    private SlbGroupStatusRMapper slbGroupStatusRMapper;
    @Resource
    private SmartArchiveGroupMapper smartArchiveGroupMapper;

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
        if (keys == null || keys.length == 0) return result;

        Integer[] hashes = new Integer[keys.length];
        String[] values = new String[keys.length];
        for (int i = 0; i < hashes.length; i++) {
            hashes[i] = keys[i].hashCode();
            values[i] = keys[i].toString();
        }
        for (SlbArchiveGroup d : smartArchiveGroupMapper.findAllByIdVersion(Arrays.asList(hashes), Arrays.asList(values))) {
            try {
                Group group = ContentReaders.readGroupContent(d.getContent());
                group.setCreatedTime(d.getDatachangeLasttime());
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
        SlbArchiveGroup d = slbArchiveGroupMapper.selectOneByExampleWithBLOBs(new SlbArchiveGroupExample().createCriteria().andGroupIdEqualTo(key.getId()).andVersionEqualTo(key.getVersion()).example());
        if (d == null) return null;

        Group result = ContentReaders.readGroupContent(d.getContent());
        result.setCreatedTime(d.getDatachangeLasttime());
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

        result.setCreatedTime(d.getDatachangeLasttime());
        return result;
    }

    @Override
    public Group add(Group group, boolean escapedPathValidation) throws Exception {
        group.setId(0L);
        ValidationContext context = new ValidationContext();
        validationFacade.validateGroup(group, context);
        if (escapedPathValidation) {
            if (context.getErrorGroups().contains(group.getId())) {
                Set<String> errorTypes = context.getGroupErrorType(group.getId());
                errorTypes.remove(ErrorType.PATH_VALIDATION);
                if (errorTypes.size() > 0) {
                    throw new ValidationException(context.getGroupErrorReason(group.getId()));
                }
            }
        } else {
            if (!context.shouldProceed()) {
                escapeValidateErrors(context, group);
            }
            if (context.getErrorGroups().contains(group.getId())) {
                throw new ValidationException(context.getGroupErrorReason(group.getId()));
            }
        }
        autoFiller.autofill(group);
        hideVirtualValue(group);
        groupEntityManager.add(group, false);
        propertyBox.set(PropertyNames.GROUP_TYPE, group.getType(), ItemTypes.GROUP, group.getId());
        syncMemberStatus(group);
        return getByKey(new IdVersion(group.getId(), group.getVersion()));
    }

    @Override
    public Group add(Group group, Set<Long> gids) throws Exception {
        group.setId(0L);
        ValidationContext context = new ValidationContext();
        validationFacade.validateGroup(group, context);

        if (!context.shouldProceed()) {
            escapeValidateErrors(context, group);
            if (gids != null && gids.size() > 0) {
                for (Long gid : gids) {
                    context.ignoreGroupErrors(0L, ErrorType.PATH_VALIDATION, MetaType.GROUP, gid);
                    context.ignoreGroupErrors(gid, ErrorType.PATH_VALIDATION, MetaType.GROUP, 0L);
                }
            }
        }
        if (context.getErrorGroups().contains(group.getId())) {
            throw new ValidationException(context.getGroupErrorReason(group.getId()));
        }

        autoFiller.autofill(group);
        hideVirtualValue(group);
        groupEntityManager.add(group, false);
        propertyBox.set(PropertyNames.GROUP_TYPE, group.getType(), ItemTypes.GROUP, group.getId());
        if(group.getRuleSet()!=null && group.getRuleSet().size()>0) {
            // clear rule set while creating group
            group.getRuleSet().clear();
        }
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
        group.setId(0L);
        ValidationContext context = new ValidationContext();
        group.setVirtual(true);
        validationFacade.validateGroup(group, context);
        if (escapedPathValidation) {
            if (context.getErrorGroups().contains(group.getId())) {
                Set<String> errorTypes = context.getGroupErrorType(group.getId());
                errorTypes.remove(ErrorType.PATH_VALIDATION);
                if (errorTypes.size() > 0) {
                    throw new ValidationException(context.getGroupErrorReason(group.getId()));
                }
            }
        } else {
            if (context.getErrorGroups().contains(group.getId())) {
                throw new ValidationException(context.getGroupErrorReason(group.getId()));
            }
        }
        autoFiller.autofillVGroup(group);
        groupEntityManager.add(group, true);
        propertyBox.set(PropertyNames.GROUP_TYPE, group.getType(), ItemTypes.GROUP, group.getId());
        hideVirtualValue(group);
        return getByKey(new IdVersion(group.getId(), group.getVersion()));
    }

    @Override
    public Group updateGroupRules(Group group) throws Exception {
        return updateGroup(group, true, true, false);
    }

    @Override
    public Group update(Group group) throws Exception {
        return update(group, false);
    }

    @Override
    public Group update(Group group, boolean escapedPathValidation) throws Exception {
        return update(group, escapedPathValidation, false);
    }

    /**
     * TODO Used in fix deactivatePolicy bug ( not a perfect solution).
     *
     * @param group
     * @param escapedPathValidation
     * @param escapedDependencyValidation
     * @return
     * @throws Exception
     */
    @Override
    public Group update(Group group, boolean escapedPathValidation, boolean escapedDependencyValidation) throws Exception {
        return updateGroup(group, escapedPathValidation, escapedDependencyValidation, true);
    }

    @Override
    public Group updateVGroup(Group group) throws Exception {
        return updateVGroup(group, false);
    }

    @Override
    public Group updateVGroup(Group group, boolean escapedPathValidation) throws Exception {
        groupModelValidator.checkRestrictionForUpdate(group);
        ValidationContext context = new ValidationContext();
        group.setVirtual(true);
        validationFacade.validateGroup(group, context);
        if (escapedPathValidation) {
            if (context.getErrorGroups().contains(group.getId())) {
                Set<String> errorTypes = context.getGroupErrorType(group.getId());
                errorTypes.remove(ErrorType.PATH_VALIDATION);
                if (errorTypes.size() > 0) {
                    throw new ValidationException(context.getGroupErrorReason(group.getId()));
                }
            }
        } else {
            if (!context.shouldProceed()) {
                validationFacade.validateSkipErrorsOfWhiteList("group", context);
                escapeValidateErrors(context, group);
            }
            if (context.getErrorGroups().contains(group.getId())) {
                throw new ValidationException(context.getGroupErrorReason(group.getId()));
            }
        }
        autoFiller.autofillVGroup(group);
        groupEntityManager.update(group);
        propertyBox.set(PropertyNames.GROUP_TYPE, group.getType(), ItemTypes.GROUP, group.getId());
        hideVirtualValue(group);
        return getByKey(new IdVersion(group.getId(), group.getVersion()));
    }

    private void escapeValidateErrors(ValidationContext context, Group group) throws Exception {
        validationFacade.validateSkipErrorsOfRelatedGroup(group, context);
    }

    @Override
    public int delete(Long groupId) throws Exception {
        groupModelValidator.removable(groupId);
        statusService.cleanGroupServerStatus(groupId);
        return groupEntityManager.delete(groupId);
    }

    @Override
    public int deleteVGroup(Long groupId) throws Exception {
        groupModelValidator.removable(groupId);
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

    @Override
    public void updateCanaryStatus(IdVersion[] groups) throws Exception {
        for (IdVersion idv : groups) {
            slbGroupStatusRMapper.updateByExampleSelective(SlbGroupStatusR.builder().groupId(idv.getId()).canaryVersion(idv.getVersion()).build(),
                    SlbGroupStatusRExample.newAndCreateCriteria().andGroupIdEqualTo(idv.getId()).example());
        }
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

    private Group updateGroup(Group group, boolean escapedPathValidation, boolean escapedDependencyValidation, boolean escapeRuleSet) throws Exception {
        groupModelValidator.checkRestrictionForUpdate(group);
        autoFiller.autoFillRelatedGroup(group);
        ValidationContext context = new ValidationContext();
        if (!escapedPathValidation && !validationFacade.validateRelatedGroup(group, null, null, null, null)) {
            throw new ValidationException("Related Group Validate Failed.");
        }
        validationFacade.validateGroup(group, context);
        if (escapedPathValidation) {
            if (context.getErrorGroups().contains(group.getId())) {
                Set<String> errorTypes = context.getGroupErrorType(group.getId());
                errorTypes.remove(ErrorType.PATH_VALIDATION);
                if (escapedDependencyValidation) {
                    errorTypes.remove(ErrorType.DEPENDENCY_VALIDATION);
                }
                if (errorTypes.size() > 0) {
                    throw new ValidationException(context.getGroupErrorReason(group.getId()));
                }
            }
        } else {
            if (!context.shouldProceed()) {
                validationFacade.validateSkipErrorsOfWhiteList("group", context);
                escapeValidateErrors(context, group);
            }
            if (context.getErrorGroups().contains(group.getId())) {
                throw new ValidationException(context.getGroupErrorReason(group.getId()));
            }
        }
        autoFiller.autofill(group);
        hideVirtualValue(group);
        if (escapeRuleSet) {
            syncGroupRules(group);
        }
        groupEntityManager.update(group);
        propertyBox.set(PropertyNames.GROUP_TYPE, group.getType(), ItemTypes.GROUP, group.getId());
        syncMemberStatus(group);
        return getByKey(new IdVersion(group.getId(), group.getVersion()));
    }

    private void hideVirtualValue(Group group) {
        group.setVirtual(null);
    }

    private void syncGroupRules(Group group) throws Exception {
        Long groupId = group.getId();
        ModelStatusMapping<Group> groupMap = entityFactory.getGroupsByIds(new Long[]{groupId});
        Group offline = groupMap.getOfflineMapping().get(groupId);
        if (offline == null) {
            throw new ValidationException("Group does not has offline version. GroupId:" + groupId);
        }
        List<Rule> offlineRules = offline.getRuleSet();
        group.getRuleSet().clear();
        group.getRuleSet().addAll(offlineRules);
    }

    private Map<Long, VirtualServer> buildVsMapping(Long[] vsIds, SelectionMode selectionMode) throws Exception {
        Set<IdVersion> vsKeys = virtualServerCriteriaQuery.queryByIdsAndMode(vsIds, selectionMode);
        Map<Long, VirtualServer> map = Maps.uniqueIndex(
                virtualServerRepository.listAll(vsKeys.toArray(new IdVersion[vsKeys.size()])),
                new Function<VirtualServer, Long>() {
                    @Override
                    public Long apply(VirtualServer virtualServer) {
                        return virtualServer != null ? virtualServer.getId() : null;
                    }
                });
        return map;
    }

}
