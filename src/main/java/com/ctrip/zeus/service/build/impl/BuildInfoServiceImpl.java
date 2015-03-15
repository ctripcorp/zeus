package com.ctrip.zeus.service.build.impl;

import com.ctrip.zeus.dal.core.BuildInfoDao;
import com.ctrip.zeus.dal.core.BuildInfoDo;
import com.ctrip.zeus.dal.core.BuildInfoEntity;
import com.ctrip.zeus.service.build.BuildInfoService;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
@Component("buildInfoService")
public class BuildInfoServiceImpl implements BuildInfoService {
    @Resource
    private BuildInfoDao buildInfoDao;

    @Override
    public int getTicket(String name) throws DalException {
        BuildInfoDo d = null;
        try {
            d = buildInfoDao.findByName(name, BuildInfoEntity.READSET_FULL);
        } catch (DalNotFoundException e) {
            d = new BuildInfoDo();
            d.setName(name).setCreatedTime(new Date()).setLastModified(new Date()).setPendingTicket(1).setCurrentTicket(0);
            buildInfoDao.insert(d);
            return 1;
        }
        int pending = d.getPendingTicket();
        d.setPendingTicket(pending + 1).setLastModified(new Date());
        buildInfoDao.updateByName(d, BuildInfoEntity.UPDATESET_FULL);
        return d.getPendingTicket();
    }

    @Override
    public void updateTicket(String name, int ticket) throws DalException {
        BuildInfoDo d = buildInfoDao.findByName(name, BuildInfoEntity.READSET_FULL);
        d.setCurrentTicket(ticket);
        buildInfoDao.updateByPK(d, BuildInfoEntity.UPDATESET_FULL);
    }
}
