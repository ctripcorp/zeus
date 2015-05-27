package com.ctrip.zeus.service.status.impl;

import com.ctrip.zeus.client.LocalClient;
import com.ctrip.zeus.client.StatusClient;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.nginx.entity.S;
import com.ctrip.zeus.nginx.entity.UpstreamStatus;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.status.AppStatusService;
import com.ctrip.zeus.service.status.StatusService;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: mag
 * Date: 4/1/2015
 * Time: 2:26 PM
 */
@Service("groupStatusService")
public class GroupStatusServiceImpl implements AppStatusService {
    private static DynamicIntProperty nginxStatusPort = DynamicPropertyFactory.getInstance().getIntProperty("slb.nginx.status-port", 10001);
    private static DynamicIntProperty adminServerPort = DynamicPropertyFactory.getInstance().getIntProperty("server.port", 8099);

    @Resource
    SlbRepository slbRepository;

    @Resource
    GroupRepository groupRepository;

    @Resource
    StatusService statusService;

    private Long currentSlbName = null;

    @Override
    public List<GroupStatus> getAllGroupStatus() throws Exception {
        List<GroupStatus> result = new ArrayList<>();
        List<Slb> slbList = slbRepository.list();
        for (Slb slb : slbList) {
            result.addAll(getAllGroupStatus(slb.getId()));
        }
        return result;
    }

    @Override
    public List<GroupStatus> getAllGroupStatus(Long slbId) throws Exception {
        List<GroupStatus> result = new ArrayList<>();

        List<GroupSlb> groupSlbs = slbRepository.listGroupSlbsBySlb(slbId);
        for (GroupSlb groupSlb : groupSlbs) {
            GroupStatus appStatus = getGroupStatus(groupSlb.getGroupId(), groupSlb.getSlbId());
            result.add(appStatus);
        }
        return result;
    }

    @Override
    public List<GroupStatus> getGroupStatus(Long groupId) throws Exception {
        List<GroupStatus> result = new ArrayList<>();
        List<Slb> slbList = slbRepository.listByGroups(new String[]{groupId});
        for (Slb slb : slbList) {
            result.add(getGroupStatus(groupId, slb.getId()));
        }
        return result;
    }

    @Override
    public GroupStatus getGroupStatus(Long groupId, Long slbId) throws Exception {
        if (!isCurrentSlb(slbId))
        {
            Slb slb = slbRepository.get(slbId);
            StatusClient client = StatusClient.getClient("http://"+slb.getSlbServers().get(0).getIp()+":"+adminServerPort.get());
            return client.getAppStatus(appName,slbName);
        }
        AppStatus status = new AppStatus();
        status.setAppName(appName);
        status.setSlbName(slbName);

        List<AppServer> appServerList = groupRepository.getAppServersByApp(appName);
        for (AppServer appServer : appServerList) {
            AppServerStatus serverStatus = getAppServerStatus(appName, slbName, appServer.getIp(), appServer.getPort());
            status.addAppServerStatus(serverStatus);
        }
        return status;
    }

    @Override
    public GroupServerStatus getGroupServerStatus(Long groupId, Long slbId, String ip, Integer port) throws Exception {
        if (!isCurrentSlb(slbName))
        {
            Slb slb = slbRepository.get(slbName);
            StatusClient client = StatusClient.getClient("http://"+slb.getSlbServers().get(0).getIp()+":"+adminServerPort.get());
            return client.getAppServerStatus(appName, slbName, ip + ":" + port);
        }

        AppServerStatus appServerStatus = new AppServerStatus();
        appServerStatus.setIp(ip);
        appServerStatus.setPort(port);

        boolean memberUp = statusService.getAppServerStatus(slbName,appName,ip);
        boolean serverUp = statusService.getServerStatus(ip);
        boolean backendUp = getUpstreamStatus(appName,ip);

        appServerStatus.setServer(serverUp);
        appServerStatus.setMember(memberUp);
        appServerStatus.setUp(backendUp);

        return appServerStatus;
    }

    //TODO: should include port to get accurate upstream
    private boolean getUpstreamStatus(String appName , String ip) throws IOException {
        UpstreamStatus upstreamStatus = LocalClient.getInstance().getUpstreamStatus();
        List<S> servers = upstreamStatus.getServers().getServer();
        String upstreamNameEndWith = "_"+appName;
        for (S server : servers) {
            if (!server.getUpstream().endsWith(upstreamNameEndWith))
            {
                continue;
            }
            String ipPort = server.getName();
            String[] ipPorts = ipPort.split(":");
            if (ipPorts.length == 2){
                if (ipPorts[0].equals(ip)){
                    return "up".equalsIgnoreCase(server.getStatus());
                }
            }
        }
        return false;
    }
    private boolean isCurrentSlb(Long slbId) throws Exception {
        if (currentSlbName < 0 ||currentSlbName == null)
        {
            String ip = com.ctrip.zeus.util.S.getIp();
            Slb slb = slbRepository.getBySlbServer(ip);
            if (slb != null )
            {
                currentSlbName = slb.getId();
            }
        }
        return slbId.equals(currentSlbName);
    }
}
