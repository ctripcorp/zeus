package com.ctrip.zeus.service.status.impl;

import com.ctrip.zeus.dal.core.StatusAppServerDao;
import com.ctrip.zeus.dal.core.StatusAppServerDo;
import com.ctrip.zeus.dal.core.StatusAppServerEntity;
import com.ctrip.zeus.service.status.StatusAppServerService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/14/2015.
 */
@Component("statusAppServerService")
public class StatusAppServerServiceImpl implements StatusAppServerService {
    @Resource
    private StatusAppServerDao statusAppServerDao;

    @Override
    public List<StatusAppServerDo> list() throws Exception {
        return statusAppServerDao.findAll(StatusAppServerEntity.READSET_FULL);
    }

    @Override
    public List<StatusAppServerDo> listAllDownBySlbName(String slbName) throws Exception {
        return statusAppServerDao.findAllBySlbNameAndIsUp(slbName, false, StatusAppServerEntity.READSET_FULL);
    }

    @Override
    public List<StatusAppServerDo> listByAppName(String appName) throws Exception {
        return statusAppServerDao.findAllByApp(appName, StatusAppServerEntity.READSET_FULL);
    }

    @Override
    public List<StatusAppServerDo> listByServer(String ip) throws Exception {
        return statusAppServerDao.findAllByIp(ip, StatusAppServerEntity.READSET_FULL);
    }

    @Override
    public void updateStatusAppServer(StatusAppServerDo d) throws Exception {
        d.setCreatedTime(new Date());
        statusAppServerDao.insert(d);
    }
}
