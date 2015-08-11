package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.*;
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
    private GroupSlbDao groupSlbDao;
    @Resource
    private GroupServerDao groupServerDao;

    @Override
    public List<String> listGroupServersBySlb(Long slbId) throws Exception {
        List<String> result = new ArrayList<>();
        List<GroupSlbDo> groupSlbDos = groupSlbDao.findAllBySlb(slbId, GroupSlbEntity.READSET_FULL);
        if (groupSlbDos.size() == 0)
            return result;
        List<GroupServerDo> groupServerDos = new ArrayList<>();
        for (GroupSlbDo groupSlbDo : groupSlbDos) {
            groupServerDos.addAll(groupServerDao.findAllByGroup(groupSlbDo.getGroupId(), GroupServerEntity.READSET_FULL));
        }
        if (groupServerDos.size() == 0)
            return result;
        for (GroupServerDo groupServerDo : groupServerDos) {
            if (result.contains(groupServerDo.getIp()))
                continue;
            result.add(groupServerDo.getIp());
        }
        return result;
    }

    @Override
    public List<String> listGroupServerIpsByGroup(Long groupId) throws Exception {
        List<String> result = new ArrayList<>();
        for (GroupServerDo groupServerDo : groupServerDao.findAllByGroup(groupId, GroupServerEntity.READSET_FULL)) {
            result.add(groupServerDo.getIp());
        }
        return result;
    }

    @Override
    public List<GroupServer> listGroupServersByGroup(Long groupId) throws Exception {
        List<GroupServer> result = new ArrayList<>();
        for (GroupServerDo groupServerDo : groupServerDao.findAllByGroup(groupId, GroupServerEntity.READSET_FULL)) {
            result.add(C.toGroupServer(groupServerDo));
        }
        return result;
    }

    @Override
    public Long[] findGroupsByGroupServerIp(String groupServerIp) throws Exception {
        List<GroupServerDo> l = groupServerDao.findAllByIp(groupServerIp, GroupServerEntity.READSET_FULL);
        Long[] result = new Long[l.size()];
        for (int i = 0; i < l.size(); i++) {
            result[i] = l.get(i).getGroupId();
        }
        return result;
    }

    @Override
    public void addGroupServer(Long groupId, GroupServer groupServer) throws Exception {
        groupServerDao.insert(C.toGroupServerDo(groupServer).setGroupId(groupId));
    }

    @Override
    public void updateGroupServer(Long groupId, GroupServer groupServer) throws Exception {
        groupServerDao.updateByGroupAndIp(C.toGroupServerDo(groupServer).setGroupId(groupId), GroupServerEntity.UPDATESET_FULL);
    }

    @Override
    public void removeGroupServer(Long groupId, String ip) throws Exception {
        if (ip != null) {
            groupServerDao.deleteByGroupAndIp(new GroupServerDo().setGroupId(groupId).setIp(ip));
        } else {
            groupServerDao.deleteByGroup(new GroupServerDo().setGroupId(groupId));
        }
    }
}
