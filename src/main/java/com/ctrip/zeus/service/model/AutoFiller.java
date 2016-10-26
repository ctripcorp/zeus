package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.*;
import org.springframework.stereotype.Service;

/**
 * Created by zhoumy on 2015/10/29.
 */
@Service("autoFiller")
public class AutoFiller {
    private static final String RegexRootPath = " /";

    public void autofill(Group group) {
        for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
            gvs.setVirtualServer(new VirtualServer().setId(gvs.getVirtualServer().getId()));

            if (gvs.getPriority() == null) {
                if (gvs.getPath().endsWith(RegexRootPath))
                    gvs.setPriority(Integer.MIN_VALUE);
                else
                    gvs.setPriority(gvs.getRewrite() == null ? 1000 : -1000);
            }
        }
        HealthCheck hc = group.getHealthCheck();
        if (hc != null) {
            hc.setIntervals(hc.getIntervals() == null ? 10000 : hc.getIntervals())
                    .setFails(hc.getFails() == null ? 5 : hc.getFails())
                    .setPasses(hc.getPasses() == null ? 1 : hc.getPasses())
                    .setTimeout(hc.getTimeout() == null ? 3000 : hc.getTimeout());
        }
        LoadBalancingMethod lbm = group.getLoadBalancingMethod();
        if (lbm == null)
            lbm = new LoadBalancingMethod();
        lbm.setType("roundrobin").setValue(lbm.getValue() == null ? "Default" : lbm.getValue());
        for (GroupServer groupServer : group.getGroupServers()) {
            groupServer.setWeight(groupServer.getWeight() == null ? 5 : groupServer.getWeight())
                    .setFailTimeout(groupServer.getFailTimeout() == null ? 30 : groupServer.getFailTimeout())
                    .setMaxFails(groupServer.getMaxFails() == null ? 0 : groupServer.getMaxFails());
        }
    }

    public void autofillVGroup(Group group) throws Exception {
        for (GroupVirtualServer e : group.getGroupVirtualServers()) {
            e.setVirtualServer(new VirtualServer().setId(e.getVirtualServer().getId()));

            if (e.getPriority() == null) {
                if (e.getPath().endsWith(RegexRootPath))
                    e.setPriority(Integer.MIN_VALUE);
                else
                    e.setPriority(e.getRewrite() == null ? 1000 : -1000);
            }
        }
        group.setHealthCheck(null);
        group.setLoadBalancingMethod(null);
        group.getGroupServers().clear();
    }

    public void autofill(Slb slb) throws Exception {
        slb.setNginxBin("/opt/app/nginx/sbin").setNginxConf("/opt/app/nginx/conf").setNginxWorkerProcesses(9)
                .setStatus(slb.getStatus() == null ? "Default" : slb.getStatus());
    }

    public void autofill(VirtualServer virtualServer) {
        virtualServer.setSsl(virtualServer.getSsl() == null ? false : virtualServer.getSsl());
    }
}
