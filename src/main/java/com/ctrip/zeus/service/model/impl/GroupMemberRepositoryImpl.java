package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupServer;
import com.ctrip.zeus.service.model.GroupMemberRepository;
import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhoumy on 2015/7/27.
 */
@Component("groupMemberRepository")
public class GroupMemberRepositoryImpl implements GroupMemberRepository {
    @Resource
    private GroupServerDao groupServerDao;
    @Resource
    private RGroupGsDao rGroupGsDao;

    @Override
    public List<GroupServer> listGroupServersByGroup(Long groupId) throws Exception {
        List<GroupServer> result = new ArrayList<>();
        for (GroupServerDo groupServerDo : groupServerDao.findAllByGroup(groupId, GroupServerEntity.READSET_FULL)) {
            result.add(C.toGroupServer(groupServerDo));
        }
        return result;
    }

    @Override
    public void addGroupServer(Long groupId, GroupServer groupServer) throws Exception {
        autofill(groupServer);
        groupServerDao.insert(C.toGroupServerDo(groupServer).setGroupId(groupId));
        rGroupGsDao.insert(new RelGroupGsDo().setGroupId(groupId).setIp(groupServer.getIp()));
    }

    @Override
    public void updateGroupServer(Long groupId, GroupServer groupServer) throws Exception {
        autofill(groupServer);
        groupServerDao.updateByGroupAndIp(C.toGroupServerDo(groupServer).setGroupId(groupId), GroupServerEntity.UPDATESET_FULL);
    }

    @Override
    public void removeGroupServer(Long groupId, String ip) throws Exception {
        if (ip != null) {
            groupServerDao.deleteByGroupAndIp(new GroupServerDo().setGroupId(groupId).setIp(ip));
            rGroupGsDao.deleteByGroupAndIp(new RelGroupGsDo().setGroupId(groupId).setIp(ip));
        } else {
            groupServerDao.deleteByGroup(new GroupServerDo().setGroupId(groupId));
            rGroupGsDao.deleteAllByGroup(new RelGroupGsDo().setGroupId(groupId));
        }
    }

    @Override
    public void port(Group[] groups) throws Exception {
        List<RelGroupGsDo> batch = new ArrayList<>();
        for (Group group : groups) {
            for (GroupServer groupServer : group.getGroupServers()) {
                batch.add(new RelGroupGsDo().setGroupId(group.getId()).setIp(groupServer.getIp()));
            }
        }
        rGroupGsDao.insert(batch.toArray(new RelGroupGsDo[batch.size()]));
    }

    @Override
    public void autofill(GroupServer groupServer) {
        groupServer.setWeight(groupServer.getWeight() == null ? 5 : groupServer.getWeight())
                .setFailTimeout(groupServer.getFailTimeout() == null ? 30 : groupServer.getFailTimeout())
                .setFailTimeout(groupServer.getMaxFails() == null ? 0 : groupServer.getMaxFails());
    }
}
