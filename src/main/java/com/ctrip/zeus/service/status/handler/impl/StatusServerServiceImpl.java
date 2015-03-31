package com.ctrip.zeus.service.status.handler.impl;

import com.ctrip.zeus.dal.core.StatusServerDao;
import com.ctrip.zeus.dal.core.StatusServerDo;
import com.ctrip.zeus.dal.core.StatusServerEntity;
import com.ctrip.zeus.service.status.handler.StatusServerService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/14/2015.
 */
@Component("statusServerService")
public class StatusServerServiceImpl implements StatusServerService {

    @Resource
    private StatusServerDao statusServerDao;

    @Override
    public List<StatusServerDo> list() throws Exception {
        return statusServerDao.findAll(StatusServerEntity.READSET_FULL);
    }

    @Override
    public List<StatusServerDo> listAllDown() throws Exception {
        return statusServerDao.findAllByIsUp(false, StatusServerEntity.READSET_FULL);
    }

    @Override
    public List<StatusServerDo> listByIp(String ip) throws Exception {
        return statusServerDao.findAllByIp(ip, StatusServerEntity.READSET_FULL);
    }

    @Override
    public void updateStatusServer(StatusServerDo d) throws Exception {
        d.setLastModified(new Date());
        statusServerDao.insert(d);
    }
}
