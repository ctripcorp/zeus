package com.ctrip.zeus.service.activate.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.service.activate.ServerGroupService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fanqq on 2015/8/4.
 */
@Component("serverGroupService")
public class ServerGroupServiceImpl implements ServerGroupService {
    @Resource
    SnapServerGroupDao snapServerGroupDao;

    @Override
    public List<Long> findAllByIp(String ip) throws Exception {
        List<SnapServerGroupDo> serverGroupDos = snapServerGroupDao.findAllByIp(ip, SnapServerGroupEntity.READSET_FULL);
        List<Long> result = new ArrayList<>();
        for (SnapServerGroupDo snapServerGroupDo : serverGroupDos){
            result.add(snapServerGroupDo.getGroupId());
        }
        return result;
    }

    @Override
    public void insertServerGroup(String ip, Long groupId) throws Exception {
        SnapServerGroupDo tmp = new SnapServerGroupDo();
        tmp.setIp(ip).setGroupId(groupId);
        snapServerGroupDao.insert(tmp);
    }

    @Override
    public void deleteByGroupId(Long groupId) throws Exception {
        snapServerGroupDao.deleteByGroupId(new SnapServerGroupDo().setGroupId(groupId));
    }

}
