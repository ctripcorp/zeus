package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.handler.GroupSync;
import com.ctrip.zeus.service.model.handler.GroupValidator;
import com.ctrip.zeus.service.model.handler.impl.ArraysUniquePicker;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
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
    private VirtualServerRepository virtualServerRepository;
    @Resource
    private GroupMemberRepository groupMemberRepository;
    @Resource
    private ArchiveService archiveService;
    @Resource
    private GroupValidator groupModelValidator;

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
    public Group add(Group group) throws Exception {
        groupModelValidator.validate(group);
        autofill(group);
        groupEntityManager.add(group);
        syncVsAndGs(group);
        return group;
    }

    @Override
    public Group update(Group group) throws Exception {
        if (!groupModelValidator.exists(group.getId()))
            throw new ValidationException("Group with id " + group.getId() + "does not exist.");
        groupModelValidator.validate(group);
        autofill(group);
        groupEntityManager.update(group);
        syncVsAndGs(group);
        return group;
    }

    // this would be called iff virtual/group servers are modified
    @Override
    public List<Group> updateVersion(Long[] groupIds) throws Exception {
        List<Group> result = new ArrayList<>();
        for (Long groupId : groupIds) {
            Group g = fresh(groupId);
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
    public void autofill(Group group) throws Exception {
        for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
            VirtualServer tvs = gvs.getVirtualServer();
            VirtualServer vs = virtualServerRepository.getById(gvs.getVirtualServer().getId());
            tvs.setName(vs.getName()).setSlbId(vs.getSlbId()).setPort(vs.getPort()).setSsl(vs.getSsl());
            tvs.getDomains().clear();
            for (Domain domain : vs.getDomains()) {
                tvs.getDomains().add(domain);
            }
        }
        HealthCheck hc = group.getHealthCheck();
        if (hc != null) {
            hc.setIntervals(hc.getIntervals() == null ? 5000 : hc.getIntervals())
                    .setFails(hc.getFails() == null ? 5 : hc.getFails())
                    .setPasses(hc.getPasses() == null ? 1 : hc.getPasses());
        }
        LoadBalancingMethod lbm = group.getLoadBalancingMethod();
        if (lbm == null)
            lbm = new LoadBalancingMethod();
        lbm.setType("roundrobin").setValue(lbm.getValue() == null ? "Default" : lbm.getValue());
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
        groupMemberRepository.port(batch);
        return groupEntityManager.port(batch);
    }

    @Override
    public void portGroupRel(Long groupId) throws Exception {
        Group group = getById(groupId);
        groupMemberRepository.port(new Group[]{group});
        groupEntityManager.port(group);
    }

    private Group fresh(Long groupId) throws Exception {
        Group group = getById(groupId);
        autofill(group);
        group.getGroupServers().clear();
        for (GroupServer server : groupMemberRepository.listGroupServersByGroup(group.getId())) {
            group.addGroupServer(server);
        }
        return group;
    }

    private void syncVsAndGs(Group group) throws Exception {
        Long groupId = group.getId();

        Map<String, GroupServer> originGses = Maps.uniqueIndex(
                groupMemberRepository.listGroupServersByGroup(groupId),
                new Function<GroupServer, String>() {
                    @Override
                    public String apply(GroupServer input) {
                        return input.getIp() + ":" + input.getPort();
                    }
                });
        Map<String, GroupServer> newGses = Maps.uniqueIndex(
                group.getGroupServers(),
                new Function<GroupServer, String>() {
                    @Override
                    public String apply(GroupServer input) {
                        return input.getIp() + ":" + input.getPort();
                    }
                });
        String[] origServers = new String[originGses.size()];
        String[] newServers = new String[newGses.size()];

        Iterator<String> iter = originGses.keySet().iterator();
        for (int i = 0; i < origServers.length; i++) {
            origServers[i] = iter.next();
        }
        iter = newGses.keySet().iterator();
        for (int i = 0; i < newServers.length; i++) {
            newServers[i] = iter.next();
        }

        List<String> removing = new ArrayList<>();
        List<String> adding = new ArrayList<>();
        List<String> updating = new ArrayList<>();
        ArraysUniquePicker.pick(origServers, newServers, removing, adding, updating);

        for (String id : removing) {
            groupMemberRepository.removeGroupServer(groupId, originGses.get(id).getIp());
        }
        for (String id : adding) {
            groupMemberRepository.addGroupServer(groupId, newGses.get(id));
        }
        for (String id : updating) {
            groupMemberRepository.updateGroupServer(groupId, newGses.get(id));
        }
    }

    private void cascadeRemoveByGroup(Long groupId) throws Exception {
        groupMemberRepository.removeGroupServer(groupId, null);
    }
}
