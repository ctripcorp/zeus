package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.GroupDao;
import com.ctrip.zeus.dal.core.GroupDo;
import com.ctrip.zeus.dal.core.GroupEntity;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.handler.GroupSync;
import com.ctrip.zeus.service.model.handler.GroupValidator;
import com.ctrip.zeus.service.model.handler.VirtualServerValidator;
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
    private VirtualServerValidator virtualServerModelValidator;

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
    public Group add(Group group) throws Exception {
        autofill(group);
        groupModelValidator.validate(group);
        groupEntityManager.add(group);
        syncVsAndGs(group);
        return group;
    }

    @Override
    public Group update(Group group) throws Exception {
        autofill(group);
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
        Long[] groupIds = groupMemberRepository.findGroupsByGroupServerIp(groupServerIp);
        return list(groupIds);
    }

    // this would be called iff virtual/group servers are modified
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
