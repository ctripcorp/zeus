package com.ctrip.zeus.service.build.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.GroupSlb;
import com.ctrip.zeus.service.activate.ActiveConfService;
import com.ctrip.zeus.service.build.BuildInfoService;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.util.AssertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalNotFoundException;

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
        try {
            d = buildInfoDao.findBySlbId(slbId, BuildInfoEntity.READSET_FULL);
        } catch (DalNotFoundException e) {
            d = new BuildInfoDo();
            d.setSlbId(slbId).setCreatedTime(new Date()).setDataChangeLastTime(new Date()).setPendingTicket(1).setCurrentTicket(0);
            buildInfoDao.insert(d);
            return 1;
        }

        if (d==null)
        {
            d = new BuildInfoDo();
            d.setSlbId(slbId).setCreatedTime(new Date()).setDataChangeLastTime(new Date()).setPendingTicket(1).setCurrentTicket(0);
            buildInfoDao.insert(d);

            logger.debug("Ticket created. Ticket Num: " + d.getPendingTicket() + "Slb Id: " + slbId);
            return 1;
        }

        int pending = d.getPendingTicket();
        d.setPendingTicket(pending + 1).setDataChangeLastTime(new Date());
        buildInfoDao.updateBySlbId(d, BuildInfoEntity.UPDATESET_FULL);

        logger.debug("Get Ticket success. Ticket Num: " + d.getPendingTicket() + "Slb Id: " + slbId);

        return d.getPendingTicket();
    }

    @Override
    public boolean updateTicket(Long slbId, int ticket) throws Exception
    {
        BuildInfoDo d = buildInfoDao.findBySlbId(slbId, BuildInfoEntity.READSET_FULL);

        if (ticket>d.getCurrentTicket())
        {
            d.setCurrentTicket(ticket);
            buildInfoDao.updateByPK(d, BuildInfoEntity.UPDATESET_FULL);

            logger.debug("Update ticket success. Ticket Num: "+ticket+"Slb ID: "+ slbId);

            return true;
        }else
        {
            return  false;
        }

    }

    @Override
    public Set<Long> getAllNeededSlb(List<Long> slbIds,List<Long> groupIds) throws Exception {
        Set<Long> buildSlbIds = new HashSet<>();
        for (Long s : slbIds)
        {
            if (slbRepository.getById(s)==null)
            {
                logger.warn("slb ["+s+"] is not exist！remove it from activate slb  list!");
            }else {
                buildSlbIds.add(s);
            }
        }


        List<GroupSlb> list = slbRepository.listGroupSlbsByGroups(groupIds.toArray(new Long[]{}));


        if (groupIds.size()>0)
        {
            AssertUtils.isNull(list,"[BuildInfoService getAllNeededSlb]get appslb by appnames failed! Please check the configuration of groupIds: "+groupIds.toString());
        }

        if (list!=null&&list.size()>0)
        {
            for (GroupSlb groupSlb : list) {
                buildSlbIds.add(Long.parseLong(groupSlb.getSlbId().toString()));
            }
        }
        return buildSlbIds;
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
}
