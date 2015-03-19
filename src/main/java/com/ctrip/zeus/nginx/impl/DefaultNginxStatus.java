package com.ctrip.zeus.nginx.impl;

import com.ctrip.zeus.nginx.NginxStatus;
import com.ctrip.zeus.nginx.entity.S;
import com.ctrip.zeus.nginx.entity.UpstreamStatus;

import java.util.HashSet;
import java.util.Set;

/**
 * @author:xingchaowang
 * @date: 3/19/2015.
 */
public class DefaultNginxStatus implements NginxStatus {
    private Set<String> ips = new HashSet<>();
    private Set<String> ipsUp = new HashSet<>();
    private Set<String> appIps = new HashSet<>();
    private Set<String> vhostAppIps = new HashSet<>();
    private Set<String> vhostAppIpsUp = new HashSet<>();
    private Set<String> appIpsUp = new HashSet<>();

    private UpstreamStatus upstreamStatus;

    public DefaultNginxStatus(UpstreamStatus upstreamStatus) {
        this.upstreamStatus = upstreamStatus;
        for (S s : upstreamStatus.getServers().getServer()) {
            String[] names = s.getUpstream().split("_");
            String appName = names[2];
            String vhostName = names[1];
            String ip = s.getName().split(":")[0];
            boolean isUp = s.getStatus().equals("up");

            ips.add(ip);
            appIps.add(appName + "_" + ip);
            vhostAppIps.add(vhostName + "_" + appName + "_" + ip);

            if (isUp) {
                vhostAppIpsUp.add(vhostName + "_" + appName + "_" + ip);
                appIpsUp.add(vhostName + "_" + appName + "_" + ip);
                ipsUp.add(ip);
            }
        }
    }

    @Override
    public boolean hasServer(String ip) {
        return ips.contains(ip);
    }

    @Override
    public boolean appHasServer(String vhostName, String appName, String ip) {
        return vhostAppIps.contains(vhostName + "_" + appName + "_" + ip);
    }

    @Override
    public boolean serverIsUp(String ip) {
        return ipsUp.contains(ip);
    }

    @Override
    public boolean appServerIsUp(String vhostName, String appName, String ip) {
        return vhostAppIpsUp.contains(vhostName + "_" + appName + "_" + ip);
    }

    @Override
    public boolean appServerIsUp(String appName, String ip) {
        return appIpsUp.contains(appName + "_" + ip);
    }
}
