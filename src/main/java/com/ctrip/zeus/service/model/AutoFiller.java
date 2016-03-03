package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhoumy on 2015/10/29.
 */
@Service("autoFiller")
public class AutoFiller {
    @Resource
    private VirtualServerRepository virtualServerRepository;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    private static final String RegexRootPath = " /";

    public void autofill(Group group) throws Exception {
        Set<Long> vsIds = new HashSet<>();
        for (GroupVirtualServer e : group.getGroupVirtualServers()) {
            vsIds.add(e.getVirtualServer().getId());
        }
        Set<IdVersion> vsKeys = virtualServerCriteriaQuery.queryByIdsAndMode(vsIds.toArray(new Long[vsIds.size()]), SelectionMode.ONLINE_FIRST);
        Map<Long, VirtualServer> map = Maps.uniqueIndex(
                virtualServerRepository.listAll(vsKeys.toArray(new IdVersion[vsKeys.size()])),
                new Function<VirtualServer, Long>() {
                    @Nullable
                    @Override
                    public Long apply(@Nullable VirtualServer virtualServer) {
                        return virtualServer.getId();
                    }
                });

        for (GroupVirtualServer e : group.getGroupVirtualServers()) {
            e.setVirtualServer(map.get(e.getVirtualServer().getId()));
        }
        autofillEmptyFields(group);
    }

    public void autofillEmptyFields(Group group) {
        for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
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
                    .setTimeout(hc.getTimeout() == null ? 2000 : hc.getTimeout());
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
        Set<Long> vsIds = new HashSet<>();
        for (GroupVirtualServer e : group.getGroupVirtualServers()) {
            vsIds.add(e.getVirtualServer().getId());
        }
        Set<IdVersion> vsKeys = virtualServerCriteriaQuery.queryByIdsAndMode(vsIds.toArray(new Long[vsIds.size()]), SelectionMode.ONLINE_FIRST);
        Map<Long, VirtualServer> map = Maps.uniqueIndex(
                virtualServerRepository.listAll(vsKeys.toArray(new IdVersion[vsKeys.size()])),
                new Function<VirtualServer, Long>() {
                    @Nullable
                    @Override
                    public Long apply(@Nullable VirtualServer virtualServer) {
                        return virtualServer.getId();
                    }
                });

        for (GroupVirtualServer e : group.getGroupVirtualServers()) {
            e.setVirtualServer(map.get(e.getVirtualServer().getId()));
        }

        for (GroupVirtualServer e : group.getGroupVirtualServers()) {
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
        for (VirtualServer virtualServer : slb.getVirtualServers()) {
            autofill(virtualServer);
        }
    }

    public void autofill(VirtualServer virtualServer) {
        virtualServer.setSsl(virtualServer.getSsl() == null ? false : virtualServer.getSsl());
    }
}
