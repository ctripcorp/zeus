package com.ctrip.zeus.service.status.impl;

import com.ctrip.zeus.dal.core.StatusAppServerDo;
import com.ctrip.zeus.dal.core.StatusServerDo;
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

    @Override
    public Set<String> findAllDownServers() {
        try {
            List<StatusServerDo> allDownServerList = statusServerService.listAllDown();
            Set<String> allDownIps = new HashSet<>();
            for (StatusServerDo d : allDownServerList) {
                allDownIps.add(d.getIp());
            }

            return allDownIps;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<String> findAllDownAppServers(String slbName) {
        try {
            Set<String> allDownAppServers = new HashSet<>();
            List<StatusAppServerDo> allDownAppServerList = statusAppServerService.listAllDownBySlbName(slbName);
            for (StatusAppServerDo d : allDownAppServerList) {
                allDownAppServers.add(d.getSlbName() + "_" + d.getVirtualServerName() + "_" + d.getAppName() + "_" + d.getIp());
            }
            return allDownAppServers;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
