package com.ctrip.zeus.service.status.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.service.status.StatusAppServerService;
import com.ctrip.zeus.service.status.StatusServerService;
import com.ctrip.zeus.service.status.StatusService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
    private AppSlbDao appSlbDao;

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

    }


    @Override
    public void upMember(String appName, String ip) throws Exception {
            //ToDo:
            List<AppSlbDo> list = appSlbDao.findAllByApp(appName, AppSlbEntity.READSET_FULL);
            for (AppSlbDo d : list) {
                statusAppServerService.updateStatusAppServer(new StatusAppServerDo().setSlbName(d.getSlbName())
                .setVirtualServerName(d.getSlbVirtualServerName())
                .setAppName(appName).setIp(ip).setUp(true));
            }
    }

    //ToDo:
    @Override
    public void downMember(String appName, String ip) throws Exception {

            List<AppSlbDo> list = appSlbDao.findAllByApp(appName, AppSlbEntity.READSET_FULL);
            for (AppSlbDo d : list) {
                statusAppServerService.updateStatusAppServer(new StatusAppServerDo().setSlbName(d.getSlbName())
                        .setVirtualServerName(d.getSlbVirtualServerName())
                        .setAppName(appName).setIp(ip).setUp(false));
            }

    }
}
