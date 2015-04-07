package com.ctrip.zeus.service.status.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.AppSlb;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.status.handler.StatusAppServerService;
import com.ctrip.zeus.service.status.handler.StatusServerService;
import com.ctrip.zeus.service.status.StatusService;
import com.ctrip.zeus.util.AssertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author:xingchaowang
 * @date: 3/16/2015.
 */
@Service("statusService")
public class StatusServiceImpl implements StatusService {
    @Resource
    private StatusServerService statusServerService;
    @Resource
    private StatusAppServerService statusAppServerService;
    @Resource
    private SlbRepository slbClusterRepository;

    private Logger logger = LoggerFactory.getLogger(StatusServiceImpl.class);

    @Override
    public Set<String> findAllDownServers() throws Exception {
            List<StatusServerDo> allDownServerList = statusServerService.listAllDown();
            Set<String> allDownIps = new HashSet<>();
            for (StatusServerDo d : allDownServerList) {
                allDownIps.add(d.getIp());
            }

            return allDownIps;

    }

    @Override
    public Set<String> findAllDownAppServersBySlbName(String slbName) throws Exception {
            Set<String> allDownAppServers = new HashSet<>();
            List<StatusAppServerDo> allDownAppServerList = statusAppServerService.listAllDownBySlbName(slbName);
            for (StatusAppServerDo d : allDownAppServerList) {
                allDownAppServers.add(d.getSlbName() + "_" + d.getVirtualServerName() + "_" + d.getAppName() + "_" + d.getIp());
            }
            return allDownAppServers;
    }

    @Override
    public void upServer(String ip) throws Exception {

        serverStatusOperation(ip,true);
    }

    @Override
    public void downServer(String ip) throws Exception {

        serverStatusOperation(ip,false);
    }

    private void serverStatusOperation(String ip , boolean status) throws Exception {

        statusServerService.updateStatusServer(new StatusServerDo().setIp(ip).setUp(status));

        logger.info("server status up ; server ip :"+ip+",Status: "+(status?"UP":"Down"));
    }


    @Override
    public void upMember(String appName, String ip) throws Exception {

        List<AppSlb> appslblist = slbClusterRepository.listAppSlbsByApps(new String[]{appName});
        if (appslblist==null||appslblist.size()==0)
        {
            logger.warn("[down member]: Can not find appslb by appName! AppName: "+appName);
            AssertUtils.isNull(appslblist,"[up member]: Can not find appslb by appName! Please Check the Configuration or param again!");
            return;
        }

        dataAdjust(appslblist,appName);

        for (AppSlb d : appslblist)
        {
            statusAppServerService.updateStatusAppServer(new StatusAppServerDo().setSlbName(d.getSlbName())
                    .setVirtualServerName(d.getVirtualServer().getName()).setAppName(appName).setIp(ip).setUp(true));
            logger.info("[up Member]: AppSlb:"+d.toString());
        }
    }

    @Override
    public void downMember(String appName, String ip) throws Exception {

        List<AppSlb> appslblist = slbClusterRepository.listAppSlbsByApps(new String[]{appName});
        if (appslblist==null||appslblist.size()==0)
        {
            logger.warn("[down member]: Can not find appslb by appName! AppName: "+appName);
            AssertUtils.isNull(appslblist,"[down member]: Can not find appslb by appName! Please Check the Configuration or param again!");
            return;
        }

        dataAdjust(appslblist,appName);

        for (AppSlb d : appslblist)
        {
            statusAppServerService.updateStatusAppServer(new StatusAppServerDo().setSlbName(d.getSlbName())
                    .setVirtualServerName(d.getVirtualServer().getName()).setAppName(appName).setIp(ip).setUp(false));
            logger.info("[down Member]: AppSlb:"+d.toString());
        }

    }

    @Override
    public boolean getAppServerStatus(String slbname, String appName, String vsip) throws Exception {

        List<StatusAppServerDo> list = statusAppServerService.listBySlbNameAndAppNameAndIp(slbname,appName,vsip);
        if (list!=null&&list.size()>0)
        {
            return list.get(0).isUp();
        }
        return true;
    }

    @Override
    public boolean getServerStatus(String vsip) throws Exception {
        List<StatusServerDo> list = statusServerService.listByIp(vsip);
        if (list!=null&&list.size()>0)
        {
            return list.get(0).isUp();
        }
        return true;
    }

    private boolean dataAdjust(List<AppSlb> list ,String appName)throws Exception{
        Set<String> appvs =new HashSet<>();
        Set<String> slbnames = new HashSet<>();
        for (AppSlb p : list)
        {
            appvs.add(p.getAppName()+p.getSlbName()+p.getVirtualServer().getName());
            slbnames.add(p.getSlbName());
        }

        List<StatusAppServerDo> statuslist = new ArrayList<>();
        for (String slbname:slbnames){
            List<StatusAppServerDo> tmplist = statusAppServerService.listBySlbNameAndAppName(slbname,appName);
            if (tmplist!=null)
            {
                statuslist.addAll(tmplist);
            }
        }

        for (StatusAppServerDo d:statuslist)
        {
            if(!appvs.contains(d.getAppName()+d.getSlbName()+d.getVirtualServerName()))
            {
                statusAppServerService.deleteBySlbNameAndAppNameAndVsName(d.getSlbName(),d.getAppName(),d.getVirtualServerName());
                logger.info("[status adjust]remove StatusAppServer :"+d.toString());
            }
        }

        return true;
    }
}
