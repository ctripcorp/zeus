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
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fanqq on 2015/11/11.
 */
@Service("healthCheckStatusService")
public class HealthCheckStatusServiceImpl implements HealthCheckStatusService {
    @Resource
    private StatusHealthCheckDao statusHealthCheckDao;
    @Resource
    private ActivateService activateService;

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
            Item item  = servers.get(i);
            dos[i] = new StatusHealthCheckDo()
                    .setSlbServerIp(hostIp)
                    .setFall(item.getFall())
                    .setMemberIpPort(item.getName())
                    .setRise(item.getRise())
                    .setUpstreamName(item.getUpstream())
                    .setType(item.getType())
                    .setStatus(item.getStatus());
        }
        if (dos.length > 0 ){
            statusHealthCheckDao.deleteBySlbServerIp(new StatusHealthCheckDo().setSlbServerIp(hostIp));
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
        }
        return result;
    }

    @Override
    public Map<String, Boolean> getHealthCheckStatusBySlbServer(String serverIp) throws Exception {
        Map<String, Boolean> result = new HashMap<>();
        List<StatusHealthCheckDo> list = statusHealthCheckDao.findBySlbServerIp(serverIp, StatusHealthCheckEntity.READSET_FULL);
        for (StatusHealthCheckDo statusHealthCheckDo : list) {
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
