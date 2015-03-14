package com.ctrip.zeus.service.status.impl;

import com.ctrip.zeus.dal.core.StatusServerDao;
import com.ctrip.zeus.dal.core.StatusServerDo;
import com.ctrip.zeus.dal.core.StatusServerEntity;
import com.ctrip.zeus.service.status.StatusServerService;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
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
    public List<com.ctrip.zeus.dal.core.StatusServerDo> list() throws DalException {
        return statusServerDao.findAll(StatusServerEntity.READSET_FULL);
    }

    @Override
    public List<com.ctrip.zeus.dal.core.StatusServerDo> listByServer(String ip) throws DalException {
        return statusServerDao.findAllByIp(ip, StatusServerEntity.READSET_FULL);
    }

    @Override
    public void updateStatusAppServer(StatusServerDo d) throws DalException {
        statusServerDao.insert(d);

    }
}
