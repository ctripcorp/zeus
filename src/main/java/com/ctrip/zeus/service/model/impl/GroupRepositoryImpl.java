package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.handler.GroupSync;
import com.ctrip.zeus.service.model.handler.GroupValidator;
import com.ctrip.zeus.service.model.handler.VGroupValidator;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import org.springframework.stereotype.Repository;

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

    @Override
    public List<Group> list(Long[] ids) throws Exception {
        List<Group> result = archiveService.getLatestGroups(ids);
        for (Group group : result) {
            autoFiller.autofill(group);
        }
        return result;
    }

    @Override
    public Group getById(Long id) throws Exception {
        if (groupModelValidator.exists(id)) {
            Group result = archiveService.getLatestGroup(id);
            autoFiller.autofill(result);
            return result;
        }
        return null;
    }

    @Override
    public Group add(Group group) throws Exception {
        groupModelValidator.validate(group);
        autoFiller.autofill(group);
        groupEntityManager.add(group, false);
        return group;
    }

    @Override
    public Group addVGroup(Group group) throws Exception {
        vGroupValidator.validate(group);
        autoFiller.autofillVGroup(group);
        groupEntityManager.add(group, true);
        return group;
    }

    @Override
    public Group update(Group group) throws Exception {
        if (!groupModelValidator.exists(group.getId()))
            throw new ValidationException("Group with id " + group.getId() + " does not exist.");
        groupModelValidator.validate(group);
        autoFiller.autofill(group);
        groupEntityManager.update(group);
        return group;
    }

    @Override
    public Group updateVGroup(Group group) throws Exception {
        if (!vGroupValidator.exists(group.getId()))
            throw new ValidationException("Group with id " + group.getId() + " does not exist.");
        vGroupValidator.validate(group);
        autoFiller.autofillVGroup(group);
        groupEntityManager.update(group);
        return group;
    }

    // this would be called iff virtual servers are modified
    @Override
    public List<Group> updateVersion(Long[] groupIds) throws Exception {
        List<Group> result = new ArrayList<>();
        for (Long groupId : groupIds) {
            Group g = getById(groupId);
            autoFiller.autofill(g);
            groupEntityManager.update(g);
            result.add(g);
        }
        return result;
    }

    @Override
    public int delete(Long groupId) throws Exception {
        groupModelValidator.removable(groupId);
        return groupEntityManager.delete(groupId);
    }

    @Override
    public int deleteVGroup(Long groupId) throws Exception {
        vGroupValidator.removable(groupId);
        return delete(groupId);
    }

    @Override
    public List<Group> listGroupsByGroupServer(String groupServerIp) throws Exception {
        Set<Long> groupIds = groupCriteriaQuery.queryByGroupServerIp(groupServerIp);
        return list(groupIds.toArray(new Long[groupIds.size()]));
    }

    @Override
    public List<Long> portGroupRel() throws Exception {
        Set<Long> groupIds = groupCriteriaQuery.queryAll();
        List<Group> groups = list(groupIds.toArray(new Long[groupIds.size()]));
        Group[] batch = groups.toArray(new Group[groups.size()]);
        return groupEntityManager.port(batch);
    }

    @Override
    public void portGroupRel(Long groupId) throws Exception {
        Group group = getById(groupId);
        groupEntityManager.port(group);
    }

    @Override
    public Group get(String groupName) throws Exception {
        return getById(groupCriteriaQuery.queryByName(groupName));
    }

    @Override
    public List<Group> list(Long slbId) throws Exception {
        Set<Long> groupIds = groupCriteriaQuery.queryBySlbId(slbId);
        return list(groupIds.toArray(new Long[groupIds.size()]));
    }
}
