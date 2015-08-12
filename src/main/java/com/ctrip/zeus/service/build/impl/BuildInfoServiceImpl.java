package com.ctrip.zeus.service.build.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.service.activate.ActiveConfService;
import com.ctrip.zeus.service.build.BuildInfoService;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.util.AssertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    @Resource
    private ActiveConfService activeConfService;


    private Logger logger= LoggerFactory.getLogger(BuildInfoServiceImpl.class);

    @Override
    public int getTicket(Long slbId) throws Exception
    {
        BuildInfoDo d = null;
        d = buildInfoDao.findBySlbId(slbId, BuildInfoEntity.READSET_FULL);
        if (d==null)
        {
            d = new BuildInfoDo();
            d.setSlbId(slbId).setCreatedTime(new Date()).setDataChangeLastTime(new Date()).setPendingTicket(1).setCurrentTicket(0);
            buildInfoDao.insert(d);

            logger.debug("Ticket created. Ticket Num: " + d.getPendingTicket() + "Slb Id: " + slbId);
            return 1;
        }

        int current = d.getCurrentTicket();
        d.setPendingTicket(current + 1).setDataChangeLastTime(new Date());
        buildInfoDao.updateBySlbId(d, BuildInfoEntity.UPDATESET_FULL);

        logger.debug("Get Ticket success. Ticket Num: " + d.getPendingTicket() + "Slb Id: " + slbId);

        return d.getPendingTicket();
    }

    @Override
    public boolean updateTicket(Long slbId, int version) throws Exception
    {
        BuildInfoDo d = buildInfoDao.findBySlbId(slbId, BuildInfoEntity.READSET_FULL);

        if (version>d.getCurrentTicket())
        {
            d.setCurrentTicket(version);
            buildInfoDao.updateByPK(d, BuildInfoEntity.UPDATESET_FULL);

            logger.debug("Update ticket success. Ticket Num: "+version+"Slb ID: "+ slbId);

            return true;
        }else
        {
            return  false;
        }

    }


    @Override
    public int getCurrentTicket(Long slbId) throws Exception {
        BuildInfoDo d = buildInfoDao.findBySlbId(slbId, BuildInfoEntity.READSET_FULL);
        return d.getCurrentTicket();
    }

    @Override
    public int getPaddingTicket(Long slbId)throws Exception{
        BuildInfoDo d = buildInfoDao.findBySlbId(slbId, BuildInfoEntity.READSET_FULL);
        return d.getPendingTicket();
    }

    @Override
    public int resetPaddingTicket(Long slbId) throws Exception {
        BuildInfoDo d = buildInfoDao.findBySlbId(slbId, BuildInfoEntity.READSET_FULL);
        if (d!=null){
            buildInfoDao.updateByPK(d.setPendingTicket(d.getCurrentTicket()), BuildInfoEntity.UPDATESET_FULL);
            return d.getPendingTicket();
        }else {
            return 0;
        }
    }
}
