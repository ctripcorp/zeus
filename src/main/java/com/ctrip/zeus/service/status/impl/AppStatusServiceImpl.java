package com.ctrip.zeus.service.status.impl;

import com.ctrip.zeus.client.LocalClient;
import com.ctrip.zeus.client.NginxClient;
import com.ctrip.zeus.client.StatusClient;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.nginx.entity.S;
import com.ctrip.zeus.nginx.entity.UpstreamStatus;
import com.ctrip.zeus.service.model.AppRepository;
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
@Service("appStatusService")
public class AppStatusServiceImpl implements AppStatusService {
    private static DynamicIntProperty nginxStatusPort = DynamicPropertyFactory.getInstance().getIntProperty("slb.nginx.status-port", 10001);
    private static DynamicIntProperty adminServerPort = DynamicPropertyFactory.getInstance().getIntProperty("server.port", 8099);

    @Resource
    SlbRepository slbRepository;

    @Resource
    AppRepository appRepository;

    @Resource
    StatusService statusService;

    private String currentSlbName = null;

    @Override
    public List<AppStatus> getAllAppStatus() throws Exception {
        List<AppStatus> result = new ArrayList<>();
        List<Slb> slbList = slbRepository.list();
        for (Slb slb : slbList) {
            result.addAll(getAllAppStatus(slb.getName()));
        }
        return result;
    }

    @Override
    public List<AppStatus> getAllAppStatus(String slbName) throws Exception {
        List<AppStatus> result = new ArrayList<>();

        List<AppSlb> appSlbs = slbRepository.listAppSlbsBySlb(slbName);
        for (AppSlb appSlb : appSlbs) {
            AppStatus appStatus = getAppStatus(appSlb.getAppName(), appSlb.getSlbName());
            result.add(appStatus);
        }
        return result;
    }

    @Override
    public List<AppStatus> getAppStatus(String appName) throws Exception {
        List<AppStatus> result = new ArrayList<>();
        List<Slb> slbList = slbRepository.listByApps(new String[]{appName});
        for (Slb slb : slbList) {
            String slbName = slb.getName();
            result.add(getAppStatus(appName,slbName));
        }
        return result;
    }

    @Override
    public AppStatus getAppStatus(String appName, String slbName) throws Exception {
        if (!isCurrentSlb(slbName))
        {
            Slb slb = slbRepository.get(slbName);
            StatusClient client = StatusClient.getClient("http://"+slb.getSlbServers().get(0).getIp()+":"+adminServerPort.get());
            return client.getAppStatus(appName,slbName);
        }
        AppStatus status = new AppStatus();
        status.setAppName(appName);
        status.setSlbName(slbName);

        List<AppServer> appServerList = appRepository.getAppServersByApp(appName);
        for (AppServer appServer : appServerList) {
            AppServerStatus serverStatus = getAppServerStatus(appName, slbName, appServer.getIp(), appServer.getPort());
            status.addAppServerStatus(serverStatus);
        }
        return status;
    }

    @Override
    public AppServerStatus getAppServerStatus(String appName, String slbName, String ip, Integer port) throws Exception {
        if (!isCurrentSlb(slbName))
        {
            Slb slb = slbRepository.get(slbName);
            StatusClient client = StatusClient.getClient("http://"+slb.getSlbServers().get(0).getIp()+":"+adminServerPort.get());
            return client.getAppServerStatus(appName,slbName,ip+":"+port);
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
    private boolean isCurrentSlb(String slbName) throws Exception {
        if (currentSlbName == null)
        {
            String ip = com.ctrip.zeus.util.S.getIp();
            Slb slb = slbRepository.getBySlbServer(ip);
            if (slb != null )
            {
                currentSlbName = slb.getName();
            }
        }
        return slbName.equals(currentSlbName);
    }
}
