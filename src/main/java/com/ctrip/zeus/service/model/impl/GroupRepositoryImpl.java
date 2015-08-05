package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.GroupDo;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupServer;
import com.ctrip.zeus.model.entity.GroupSlb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.model.handler.GroupQuery;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.handler.GroupSync;
import com.ctrip.zeus.service.model.ArchiveService;
import com.ctrip.zeus.service.model.handler.SlbQuery;
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
    private GroupSync groupSync;
    @Resource
    private GroupQuery groupQuery;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private ArchiveService archiveService;

    @Override
    public List<Group> list() throws Exception {
        return groupQuery.getAll();
    }

    @Override
    public List<Group> list(String slbName, String virtualServerName) throws Exception {
        List<Group> list = new ArrayList<>();
        Long slbId = slbRepository.get(slbName).getId();
        VirtualServer vs = slbRepository.getVirtualServer(null, slbId, virtualServerName);
        for (Group group : groupQuery.getByVirtualServer(vs.getId())) {
            list.add(group);
        }
        return list;
    }

    @Override
    public List<Group> list(Long slbId) throws Exception {
        Set<Long> groupIds = new HashSet<>();
        for (GroupSlb groupSlb :slbRepository.listGroupSlbsBySlb(slbId)) {
            groupIds.add(groupSlb.getGroupId());
        }
        return groupQuery.batchGetByIds(groupIds.toArray(new Long[groupIds.size()]));
    }

    @Override
    public List<Group> listLimit(Long fromId, int maxCount) throws Exception {
        return groupQuery.getLimit(fromId, maxCount);
    }

    @Override
    public List<Group> list(Long[] ids) throws Exception {
        return groupQuery.batchGetByIds(ids);
    }

    @Override
    public Group getById(Long id) throws Exception {
        return groupQuery.getById(id);
    }

    @Override
    public Group get(String groupName) throws Exception {
        return groupQuery.get(groupName);
    }

    @Override
    public Group getByAppId(String appId) throws Exception {
        return groupQuery.getByAppId(appId);
    }

    @Override
    public Group add(Group group) throws Exception {
        GroupDo d = groupSync.add(group);
        archiveService.archiveGroup(groupQuery.getById(d.getId()));
        return C.toGroup(d);

    }

    @Override
    public Group update(Group group) throws Exception {
        if (group == null)
            return null;
        GroupDo d = groupSync.update(group);
        group = groupQuery.getById(d.getId());
        archiveService.archiveGroup(group);
        return group;
    }

    @Override
    public int delete(Long groupId) throws Exception {
        int count = groupSync.delete(groupId);
        return count;

    }

    @Override
    public List<String> listGroupsByGroupServer(String groupServerIp) throws Exception {
        return groupQuery.getByGroupServer(groupServerIp);
    }

    @Override
    public List<String> listGroupServerIpsByGroup(Long groupId) throws Exception {
        return groupQuery.getGroupServerIpsByGroup(groupId);
    }

    @Override
    public List<GroupServer> listGroupServersByGroup(Long groupId) throws Exception {
        return groupQuery.getGroupServersByGroup(groupId);
    }
}
