package com.ctrip.zeus.service.build.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.service.build.BuildInfoService;
import com.ctrip.zeus.service.model.SlbRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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

    @Resource
    private SlbRepository slbRepository;


    private Logger logger = LoggerFactory.getLogger(BuildInfoServiceImpl.class);

    @Override
    public int getTicket(Long slbId) throws Exception {
        BuildInfoDo d = null;
        d = buildInfoDao.findBySlbId(slbId, BuildInfoEntity.READSET_FULL);
        if (d == null) {
            d = new BuildInfoDo();
            d.setSlbId(slbId).setCreatedTime(new Date()).setDataChangeLastTime(new Date()).setPendingTicket(1).setCurrentTicket(1);
            buildInfoDao.insert(d);
            return 1;
        }

        int current = d.getCurrentTicket();
        d.setPendingTicket(current + 1).setCurrentTicket(current + 1).setDataChangeLastTime(new Date());
        buildInfoDao.updateBySlbId(d, BuildInfoEntity.UPDATESET_FULL);

        return d.getCurrentTicket();
    }

    @Override
    public boolean updateTicket(Long slbId, int version) throws Exception {
        BuildInfoDo d = buildInfoDao.findBySlbId(slbId, BuildInfoEntity.READSET_FULL);

        if (version > d.getCurrentTicket()) {
            d.setCurrentTicket(version);
            buildInfoDao.updateByPK(d, BuildInfoEntity.UPDATESET_FULL);
            return true;
        } else {
            return false;
        }

    }


    @Override
    public int getCurrentTicket(Long slbId) throws Exception {
        BuildInfoDo d = buildInfoDao.findBySlbId(slbId, BuildInfoEntity.READSET_FULL);
        return d.getCurrentTicket();
    }

    @Override
    public int getPaddingTicket(Long slbId) throws Exception {
        BuildInfoDo d = buildInfoDao.findBySlbId(slbId, BuildInfoEntity.READSET_FULL);
        return d.getPendingTicket();
    }
}
