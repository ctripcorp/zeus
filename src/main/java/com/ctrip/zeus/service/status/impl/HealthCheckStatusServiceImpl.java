package com.ctrip.zeus.service.status.impl;

import com.ctrip.zeus.client.LocalClient;
import com.ctrip.zeus.dal.core.StatusHealthCheckDao;
import com.ctrip.zeus.dal.core.StatusHealthCheckDo;
import com.ctrip.zeus.nginx.entity.Item;
import com.ctrip.zeus.nginx.entity.UpstreamStatus;
import com.ctrip.zeus.service.status.HealthCheckStatusService;
import com.ctrip.zeus.util.S;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Created by fanqq on 2015/11/11.
 */
@Service("healthCheckStatusService")
public class HealthCheckStatusServiceImpl implements HealthCheckStatusService {
    @Resource
    private StatusHealthCheckDao statusHealthCheckDao;
    @Override
    public void freshHealthCheckStatus() throws Exception {
        String hostIp = S.getIp();
        statusHealthCheckDao.deleteBySlbServerIp(new StatusHealthCheckDo().setSlbServerIp(hostIp));
        UpstreamStatus upstreamStatus = LocalClient.getInstance().getUpstreamStatus();
        List<Item> servers = upstreamStatus.getServers().getServer();
        for (Item item : servers){
            statusHealthCheckDao.insert(new StatusHealthCheckDo()
                    .setSlbServerIp(hostIp)
                    .setFall(item.getFall())
                    .setMemberIpPort(item.getName())
                    .setRise(item.getRise())
                    .setUpstreamName(item.getUpstream())
                    .setType(item.getType()));
        }
    }

    @Override
    public Map<String, Boolean> getHealthCheckStatusBySlbId(Long slbId) throws Exception {
        return null;
    }
}
