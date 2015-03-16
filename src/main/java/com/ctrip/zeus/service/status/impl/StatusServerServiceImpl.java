package com.ctrip.zeus.service.status.impl;

import com.ctrip.zeus.dal.core.StatusServerDao;
import com.ctrip.zeus.dal.core.StatusServerDo;
import com.ctrip.zeus.dal.core.StatusServerEntity;
import com.ctrip.zeus.service.status.StatusServerService;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;

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
    public List<StatusServerDo> list() throws DalException {
        return statusServerDao.findAll(StatusServerEntity.READSET_FULL);
    }

    @Override
    public List<StatusServerDo> listAllDown() throws DalException {
        return statusServerDao.findAllByIsUp(false, StatusServerEntity.READSET_FULL);
    }

    @Override
    public List<StatusServerDo> listByServer(String ip) throws DalException {
        return statusServerDao.findAllByIp(ip, StatusServerEntity.READSET_FULL);
    }

    @Override
    public void updateStatusServer(StatusServerDo d) throws DalException {
        d.setCreatedTime(new Date());
        statusServerDao.insert(d);

    }
}
