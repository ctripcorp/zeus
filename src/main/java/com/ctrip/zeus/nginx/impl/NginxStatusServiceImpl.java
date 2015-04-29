package com.ctrip.zeus.nginx.impl;

import com.ctrip.zeus.client.LocalClient;
import com.ctrip.zeus.nginx.NginxStatus;
import com.ctrip.zeus.nginx.NginxStatusService;
import com.ctrip.zeus.nginx.entity.TrafficStatus;
import com.ctrip.zeus.nginx.entity.UpstreamStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/19/2015.
 */
@Service("nginxStatusService")
public class NginxStatusServiceImpl implements NginxStatusService {
    @Override
    public NginxStatus getNginxStatus(String slbName) throws Exception {
        UpstreamStatus upstreamStatus = LocalClient.getInstance().getUpstreamStatus();
        return new DefaultNginxStatus(upstreamStatus);
    }

    @Override
    public TrafficStatus getTrafficStatus(String hostname) {
        List<TrafficStatus> trafficStatuses = LocalClient.getInstance().getTrafficStatus();
        for (TrafficStatus ts : trafficStatuses) {
            if (ts.getHostName().equalsIgnoreCase(hostname))
                return ts;
        }
        return null;
    }
}
