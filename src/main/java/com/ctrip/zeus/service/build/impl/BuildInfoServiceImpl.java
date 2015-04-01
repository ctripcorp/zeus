package com.ctrip.zeus.service.build.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.AppSlb;
import com.ctrip.zeus.service.build.BuildInfoService;
import com.ctrip.zeus.service.model.SlbRepository;
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
        int pending = d.getPendingTicket();
        d.setPendingTicket(pending + 1).setLastModified(new Date());
        buildInfoDao.updateByName(d, BuildInfoEntity.UPDATESET_FULL);
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
                slbname.remove(s);
            }
        }
        buildNames.addAll(slbname);


        List<AppSlb> list = slbClusterRepository.listAppSlbsByApps(appname.toArray(new String[]{}));

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
