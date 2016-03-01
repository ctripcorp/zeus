package com.ctrip.zeus.service.status.impl;

import com.ctrip.zeus.client.LocalClient;
import com.ctrip.zeus.dal.core.StatusHealthCheckDao;
import com.ctrip.zeus.dal.core.StatusHealthCheckDo;
import com.ctrip.zeus.dal.core.StatusHealthCheckEntity;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbServer;
import com.ctrip.zeus.nginx.entity.Item;
import com.ctrip.zeus.nginx.entity.UpstreamStatus;
import com.ctrip.zeus.service.activate.ActivateService;
import com.ctrip.zeus.service.status.HealthCheckStatusService;
import com.ctrip.zeus.util.S;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by fanqq on 2015/11/11.
 */
@Service("healthCheckStatusService")
public class HealthCheckStatusServiceImpl implements HealthCheckStatusService {
    @Resource
    private StatusHealthCheckDao statusHealthCheckDao;
    @Resource
    private ActivateService activateService;
    private static int count = 0;
    private static DynamicIntProperty invalidInterval = DynamicPropertyFactory.getInstance().getIntProperty("health.check.status.invalid.interval", 300000);

    @Override
    public void freshHealthCheckStatus() throws Exception {
        String hostIp = S.getIp();
        UpstreamStatus upstreamStatus = LocalClient.getInstance().getUpstreamStatus();

        if (upstreamStatus == null || upstreamStatus.getServers() == null || upstreamStatus.getServers().getServer() == null) {
            return;
        }

        List<Item> servers = upstreamStatus.getServers().getServer();

        StatusHealthCheckDo[] dos = new StatusHealthCheckDo[servers.size()];

        for (int i = 0; i < servers.size(); i++) {
            Item item = servers.get(i);
            dos[i] = new StatusHealthCheckDo()
                    .setSlbServerIp(hostIp)
                    .setFall(item.getFall())
                    .setMemberIpPort(item.getName())
                    .setRise(item.getRise())
                    .setUpstreamName(item.getUpstream())
                    .setType(item.getType())
                    .setStatus(item.getStatus());
        }
        if (dos.length > 0) {
            statusHealthCheckDao.insert(dos);
        }
    }

    @Override
    public Map<String, Boolean> getHealthCheckStatusBySlbId(Long slbId) throws Exception {
        Map<String, Boolean> result = new HashMap<>();
        Slb slb = activateService.getActivatedSlb(slbId);
        List<SlbServer> servers = slb.getSlbServers();
        for (SlbServer slbServer : servers) {
            List<StatusHealthCheckDo> list = statusHealthCheckDao.findBySlbServerIp(slbServer.getIp(), StatusHealthCheckEntity.READSET_FULL);
            for (StatusHealthCheckDo statusHealthCheckDo : list) {
                long now = System.currentTimeMillis();
                long lastUpdate = statusHealthCheckDo.getDataChangeLastTime().getTime();
                if (now - lastUpdate > invalidInterval.get()) {
                    continue;
                }
                String upstream = statusHealthCheckDo.getUpstreamName();
                String ipPort = statusHealthCheckDo.getMemberIpPort();
                if (upstream == null || ipPort == null) {
                    continue;
                }
                String[] tmp = upstream.split("_");
                String[] iptmp = ipPort.split(":");
                String key = tmp[tmp.length - 1] + "_" + iptmp[0];
                if (result.get(key) == null || result.get(key)) {
                    result.put(key, statusHealthCheckDo.getStatus().equals("up"));
                }
            }
        }
        return result;
    }

    @Override
    public Map<String, Boolean> getHealthCheckStatusBySlbId(Long slbId, Set<Long> groupId) throws Exception {
        Map<String, Boolean> result = new HashMap<>();
        Slb slb = activateService.getActivatedSlb(slbId);
        List<SlbServer> servers = slb.getSlbServers();
        List<String> upstreamName = new ArrayList<>();
        for (Long gid : groupId) {
            upstreamName.add("backend_" + gid);
        }
        for (SlbServer slbServer : servers) {
            List<StatusHealthCheckDo> list = statusHealthCheckDao.findBySlbServerIpAndUpstreamName(slbServer.getIp(),
                    upstreamName.toArray(new String[]{}), StatusHealthCheckEntity.READSET_FULL);
            boolean flag = false;
            for (StatusHealthCheckDo statusHealthCheckDo : list) {
                long now = System.currentTimeMillis();
                long lastUpdate = statusHealthCheckDo.getDataChangeLastTime().getTime();
                if (now - lastUpdate > invalidInterval.get()) {
                    flag = true;
                    continue;
                }
                String upstream = statusHealthCheckDo.getUpstreamName();
                String ipPort = statusHealthCheckDo.getMemberIpPort();
                if (upstream == null || ipPort == null) {
                    flag = true;
                    continue;
                }
                String[] tmp = upstream.split("_");
                String[] iptmp = ipPort.split(":");
                String key = tmp[tmp.length - 1] + "_" + iptmp[0];
                if (result.get(key) == null || result.get(key)) {
                    result.put(key, statusHealthCheckDo.getStatus().equals("up"));
                }
            }
            if (!flag){
                break;
            }
        }
        return result;
    }

    @Override
    public Map<String, Boolean> getHealthCheckStatusBySlbServer(String serverIp) throws Exception {
        Map<String, Boolean> result = new HashMap<>();
        List<StatusHealthCheckDo> list = statusHealthCheckDao.findBySlbServerIp(serverIp, StatusHealthCheckEntity.READSET_FULL);
        for (StatusHealthCheckDo statusHealthCheckDo : list) {
            long now = System.currentTimeMillis();
            long lastUpdate = statusHealthCheckDo.getDataChangeLastTime().getTime();
            if (now - lastUpdate > invalidInterval.get()) {
                continue;
            }
            String upstream = statusHealthCheckDo.getUpstreamName();
            String ipPort = statusHealthCheckDo.getMemberIpPort();
            if (upstream == null || ipPort == null) {
                continue;
            }
            String[] tmp = upstream.split("_");
            String[] iptmp = ipPort.split(":");
            String key = tmp[tmp.length - 1] + iptmp[0];
            if (result.get(key) == null || result.get(key)) {
                result.put(key, statusHealthCheckDo.getStatus().equals("up"));
            }
        }
        return result;
    }
}
