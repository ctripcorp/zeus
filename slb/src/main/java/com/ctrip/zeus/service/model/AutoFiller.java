package com.ctrip.zeus.service.model;

import com.ctrip.zeus.domain.GroupType;
import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.tag.PropertyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhoumy on 2015/10/29.
 */
@Service("autoFiller")
public class AutoFiller {

    @Resource
    PropertyService propertyService;
    @Resource
    GroupCriteriaQuery groupCriteriaQuery;
    @Autowired
    GroupRepository groupRepository;

    private static final String RegexRootPath = " /";
    private static final String RegexRootPath2 = " ^/";
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public void autoFillRelatedGroup(Group group) {
        String relatedAppId = null;
        try {
            Property p = propertyService.getProperty("relatedAppId", group.getId(), "group");
            if (p != null) {
                relatedAppId = p.getValue();
            }
            if (relatedAppId == null) return;
        } catch (Exception e) {
            logger.error("Get Property Failed.relatedAppId,group," + group.getId(), e);
            return;
        }

        Map<Long, GroupVirtualServer> gvsMap = new HashMap<>();
        for (GroupVirtualServer tmp : group.getGroupVirtualServers()) {
            if (tmp.getPriority() == null) {
                gvsMap.put(tmp.getVirtualServer().getId(), tmp);
            }
        }
        if (gvsMap.size() == 0) return;

        List<Group> groups = null;
        try {
            Set<Long> groupIds = groupCriteriaQuery.queryByAppId(relatedAppId);
            groupIds.remove(group.getId());
            groups = groupRepository.list((groupIds.toArray(new Long[groupIds.size()])));
        } catch (Exception e) {
            logger.error("Get Groups Failed.RelatedAppId:" + relatedAppId, e);
            return;
        }
        if (groups == null || groups.size() == 0) {
            return;
        }
        for (Group g : groups) {
            for (GroupVirtualServer gvs : g.getGroupVirtualServers()) {
                if (!gvsMap.containsKey(gvs.getVirtualServer().getId())) continue;
                GroupVirtualServer relatedGvs = gvsMap.get(gvs.getVirtualServer().getId());
                if (relatedGvs.getPriority() == null || relatedGvs.getPriority() > gvs.getPriority()) {
                    relatedGvs.setPriority(gvs.getPriority());
                }
            }
        }
        for (GroupVirtualServer gvs : gvsMap.values()) {
            if (gvs.getPriority() != null) {
                gvs.setPriority(gvs.getPriority() - ModelConstValues.PRIORITY_STEP_FOR_RELATED_GROUP);
            }
        }
    }

    public void autofill(Group group) {
        if (group.getType() == null || group.getType().isEmpty()) {
            group.setType((group.isVirtual() ? GroupType.VGROUP : GroupType.GROUP).toString());
        }
        group.setSsl(group.getSsl() == null ? false : group.getSsl());
        for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
            gvs.setVirtualServer(new VirtualServer().setId(gvs.getVirtualServer().getId()));

            if (gvs.getPriority() == null) {
                if (gvs.getPath() == null || gvs.getPath().isEmpty() || gvs.getPath().endsWith(RegexRootPath) || gvs.getPath().endsWith(RegexRootPath2))
                    gvs.setPriority(-1000);
                else
                    gvs.setPriority(gvs.getRewrite() == null ? 1000 : -1000);
            }
        }
        HealthCheck hc = group.getHealthCheck();
        if (hc != null) {
            hc.setIntervals(hc.getIntervals() == null ? 10000 : hc.getIntervals());
            hc.setFails(hc.getFails() == null ? 5 : hc.getFails());
            hc.setPasses(hc.getPasses() == null ? 1 : hc.getPasses());
            hc.setTimeout(hc.getTimeout() == null ? 3000 : hc.getTimeout());
        }
        LoadBalancingMethod lbm = group.getLoadBalancingMethod();
        if (lbm == null) {
            lbm = new LoadBalancingMethod();
            group.setLoadBalancingMethod(lbm);
        }
        lbm.setType("roundrobin").setValue(lbm.getValue() == null ? "Default" : lbm.getValue());
        for (GroupServer groupServer : group.getGroupServers()) {
            groupServer.setWeight(groupServer.getWeight() == null ? 5 : groupServer.getWeight())
                    .setFailTimeout(groupServer.getFailTimeout() == null ? 30 : groupServer.getFailTimeout())
                    .setMaxFails(groupServer.getMaxFails() == null ? 0 : groupServer.getMaxFails());
        }
    }

    public void autofillVGroup(Group group) throws Exception {
        group.setType(GroupType.VGROUP.toString());
        for (GroupVirtualServer e : group.getGroupVirtualServers()) {
            e.setVirtualServer(new VirtualServer().setId(e.getVirtualServer().getId()));

            if (e.getPriority() == null) {
                if (e.getPath() == null || e.getPath().isEmpty() || e.getPath().endsWith(RegexRootPath))
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
