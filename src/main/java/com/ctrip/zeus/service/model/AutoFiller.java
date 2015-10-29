package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by zhoumy on 2015/10/29.
 */
@Service("autoFiller")
public class AutoFiller {
    @Resource
    private VirtualServerRepository virtualServerRepository;

    public void autofill(Group group) throws Exception {
        for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
            VirtualServer tvs = gvs.getVirtualServer();
            VirtualServer vs = virtualServerRepository.getById(gvs.getVirtualServer().getId());
            tvs.setName(vs.getName()).setSlbId(vs.getSlbId()).setPort(vs.getPort()).setSsl(vs.getSsl());
            tvs.getDomains().clear();
            for (Domain domain : vs.getDomains()) {
                tvs.getDomains().add(domain);
            }
            gvs.setPriority(gvs.getPriority() == null ? 1000 : gvs.getPriority());
        }
        HealthCheck hc = group.getHealthCheck();
        if (hc != null) {
            hc.setIntervals(hc.getIntervals() == null ? 5000 : hc.getIntervals())
                    .setFails(hc.getFails() == null ? 5 : hc.getFails())
                    .setPasses(hc.getPasses() == null ? 1 : hc.getPasses());
        }
        LoadBalancingMethod lbm = group.getLoadBalancingMethod();
        if (lbm == null)
            lbm = new LoadBalancingMethod();
        lbm.setType("roundrobin").setValue(lbm.getValue() == null ? "Default" : lbm.getValue());
        for (GroupServer groupServer : group.getGroupServers()) {
            groupServer.setWeight(groupServer.getWeight() == null ? 5 : groupServer.getWeight())
                    .setFailTimeout(groupServer.getFailTimeout() == null ? 30 : groupServer.getFailTimeout())
                    .setFailTimeout(groupServer.getMaxFails() == null ? 0 : groupServer.getMaxFails());
        }
    }

    public void autofill(Slb slb) throws Exception {
        slb.setNginxBin("/opt/app/nginx/sbin").setNginxConf("/opt/app/nginx/conf").setNginxWorkerProcesses(9)
                .setStatus(slb.getStatus() == null ? "Default" : slb.getStatus());
        for (VirtualServer virtualServer : slb.getVirtualServers()) {
            autofill(virtualServer);
        }
    }

    public void autofill(VirtualServer virtualServer) {
        virtualServer.setSsl(virtualServer.getSsl() == null ? false : virtualServer.getSsl());
    }
}
