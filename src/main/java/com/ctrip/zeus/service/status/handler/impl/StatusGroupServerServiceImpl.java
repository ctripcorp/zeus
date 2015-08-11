package com.ctrip.zeus.service.status.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.service.status.handler.StatusGroupServerService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/14/2015.
 */
@Component("statusGroupServerService")
public class StatusGroupServerServiceImpl implements StatusGroupServerService {
    @Resource
    private StatusGroupServerDao statusGroupServerDao;

    @Override
    public List<StatusGroupServerDo> list() throws Exception {
        return statusGroupServerDao.findAll(StatusGroupServerEntity.READSET_FULL);
    }

    @Override
    public List<StatusGroupServerDo> listAllDownBySlbId(Long slbId) throws Exception {
        return statusGroupServerDao.findAllBySlbIdAndIsUp(slbId, false, StatusGroupServerEntity.READSET_FULL);
    }

    @Override
    public List<StatusGroupServerDo> listAllUpBySlbId(Long slbId) throws Exception {
        return statusGroupServerDao.findAllBySlbIdAndIsUp(slbId, true, StatusGroupServerEntity.READSET_FULL);
    }

    @Override
    public List<StatusGroupServerDo> listByGroupId(Long groupId) throws Exception {
        return statusGroupServerDao.findAllByGroupId(groupId, StatusGroupServerEntity.READSET_FULL);
    }

    @Override
    public List<StatusGroupServerDo> listByServer(String ip) throws Exception {
        return statusGroupServerDao.findAllByIp(ip, StatusGroupServerEntity.READSET_FULL);
    }

    @Override
    public List<StatusGroupServerDo> listBySlbIdAndGroupIdAndIp(Long slbId,Long groupId,String ip) throws Exception {
        return statusGroupServerDao.findAllBySlbIdAndGroupIdAndIp(slbId, groupId, ip, StatusGroupServerEntity.READSET_FULL);
    }

    @Override
    public List<StatusGroupServerDo> listBySlbIdAndGroupId(Long slbId,Long groupId)throws Exception {
        return statusGroupServerDao.findAllByGroupIdAndSlbId(slbId, groupId, StatusGroupServerEntity.READSET_FULL);
    }

    @Override
    public void deleteBySlbIdAndGroupIdAndVsId(Long slbId,Long groupId,Long vsId) throws Exception {
        statusGroupServerDao.deleteByGroupIdAndSlbIdAndVirtualServerId(new StatusGroupServerDo()
                .setGroupId(groupId).setSlbId(slbId).setSlbVirtualServerId(vsId));
    }

    @Override
    public void updateStatusGroupServer(StatusGroupServerDo d) throws Exception {
        d.setCreatedTime(new Date());
        statusGroupServerDao.insert(d);
    }

    @Override
    public void deleteByGroupIdAndSlbIdAndIp(Long slbId,Long groupId, String ip) throws Exception {
        statusGroupServerDao.deleteByGroupIdAndSlbIdAndIp(new StatusGroupServerDo().setSlbId(slbId).setGroupId(groupId).setIp(ip));
    }

}
