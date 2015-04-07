package com.ctrip.zeus.service.build.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.AppSlb;
import com.ctrip.zeus.service.Activate.ActiveConfService;
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
    private SlbRepository slbClusterRepository;
    @Resource
    private ActiveConfService activeConfService;


    private Logger logger= LoggerFactory.getLogger(BuildInfoServiceImpl.class);

    @Override
    public int getTicket(String name) throws Exception
    {
        BuildInfoDo d = null;
        try {
            d = buildInfoDao.findByName(name, BuildInfoEntity.READSET_FULL);
        } catch (DalNotFoundException e) {
            d = new BuildInfoDo();
            d.setName(name).setCreatedTime(new Date()).setLastModified(new Date()).setPendingTicket(1).setCurrentTicket(0);
            buildInfoDao.insert(d);
            return 1;
        }

        if (d==null)
        {
            d = new BuildInfoDo();
            d.setName(name).setCreatedTime(new Date()).setLastModified(new Date()).setPendingTicket(1).setCurrentTicket(0);
            buildInfoDao.insert(d);

            logger.debug("Ticket created. Ticket Num: " + d.getPendingTicket() + "Slb Name: " + name);
            return 1;
        }

        int pending = d.getPendingTicket();
        d.setPendingTicket(pending + 1).setLastModified(new Date());
        buildInfoDao.updateByName(d, BuildInfoEntity.UPDATESET_FULL);

        logger.debug("Get Ticket success. Ticket Num: " + d.getPendingTicket() + "Slb Name: " + name);

        return d.getPendingTicket();
    }

    @Override
    public boolean updateTicket(String name, int ticket) throws Exception
    {
        BuildInfoDo d = buildInfoDao.findByName(name, BuildInfoEntity.READSET_FULL);

        if (ticket>d.getCurrentTicket())
        {
            d.setCurrentTicket(ticket);
            buildInfoDao.updateByPK(d, BuildInfoEntity.UPDATESET_FULL);

            logger.debug("Update ticket success. Ticket Num: "+ticket+"Slb Name: "+ name);

            return true;
        }else
        {
            return  false;
        }

    }

    @Override
    public Set<String> getAllNeededSlb(List<String> slbname,List<String> appname) throws Exception {
        Set<String> buildNames = new HashSet<>();
        for (String s:slbname)
        {
            if (slbClusterRepository.get(s)==null)
            {
                logger.warn("slb ["+s+"] is not exist！remove it from activate slb names list!");
                slbname.remove(s);
            }else if (activeConfService.getConfSlbActiveContentBySlbNames(s)==null)
            {
                logger.warn("slb ["+s+"] is not activated！remove it from activate slb names list!");
                slbname.remove(s);
            }
        }
        buildNames.addAll(slbname);


        List<AppSlb> list = slbClusterRepository.listAppSlbsByApps(appname.toArray(new String[]{}));


        if (appname.size()>0)
        {
            AssertUtils.isNull(list,"[BuildInfoService getAllNeededSlb]get appslb by appnames failed! Please check the configuration of appnames: "+appname.toString());
        }

        if (list!=null&&list.size()>0)
        {
            for (AppSlb appSlb : list) {
                buildNames.add(appSlb.getSlbName());
            }
        }


        return buildNames;
    }

    @Override
    public int getCurrentTicket(String slbname) throws Exception {
        BuildInfoDo d = buildInfoDao.findByName(slbname, BuildInfoEntity.READSET_FULL);
        return d.getCurrentTicket();
    }
}
