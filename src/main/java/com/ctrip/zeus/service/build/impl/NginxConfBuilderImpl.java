package com.ctrip.zeus.service.build.impl;

import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.nginx.entity.ConfFile;
import com.ctrip.zeus.service.build.NginxConfBuilder;
import com.ctrip.zeus.service.build.conf.NginxConf;
import com.ctrip.zeus.service.build.conf.ServerConf;
import com.ctrip.zeus.service.build.conf.UpstreamsConf;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by fanqq on 2015/3/30.
 */
@Service("nginxConfigBuilder")
public class NginxConfBuilderImpl implements NginxConfBuilder {

    @Resource
    NginxConf nginxConf;
    @Resource
    ServerConf serverConf;
    @Resource
    UpstreamsConf upstreamsConf;

    @Override
    public String generateNginxConf(Slb slb) throws Exception {
        return nginxConf.generate(slb);
    }

    @Override
    public String generateServerConf(Slb slb, VirtualServer vs, List<TrafficPolicy> policies, List<Group> groups) throws Exception {
        final Map<String, Object> objectOnVsReferrer = new HashMap<>();
        policies = policies == null ? new ArrayList<TrafficPolicy>() : policies;
        groups = groups == null ? new ArrayList<Group>() : groups;

        Long vsId = vs.getId();
        for (TrafficPolicy p : policies) {
            for (PolicyVirtualServer pvs : p.getPolicyVirtualServers()) {
                if (pvs.getVirtualServer().getId().equals(vsId)) {
                    objectOnVsReferrer.put("pvs-" + p.getId(), pvs);
                }
            }
        }
        for (Group g : groups) {
            for (GroupVirtualServer gvs : g.getGroupVirtualServers()) {
                if (gvs.getVirtualServer().getId().equals(vsId)) {
                    objectOnVsReferrer.put("gvs-" + g.getId(), gvs);
                }
            }
        }
        Collections.sort(policies, new Comparator<TrafficPolicy>() {
            @Override
            public int compare(TrafficPolicy o1, TrafficPolicy o2) {
                int result = ((PolicyVirtualServer) objectOnVsReferrer.get("pvs-" + o2.getId())).getPriority() -
                        ((PolicyVirtualServer) objectOnVsReferrer.get("pvs-" + o1.getId())).getPriority();
                return result == 0 ? o2.getId().compareTo(o1.getId()) : result;
            }
        });
        Collections.sort(groups, new Comparator<Group>() {
            @Override
            public int compare(Group o1, Group o2) {
                int result = ((GroupVirtualServer) objectOnVsReferrer.get("gvs-" + o2.getId())).getPriority() -
                        ((GroupVirtualServer) objectOnVsReferrer.get("gvs-" + o1.getId())).getPriority();
                return result == 0 ? o2.getId().compareTo(o1.getId()) : result;
            }
        });
        return serverConf.generate(slb, vs, policies, groups, objectOnVsReferrer);
    }

    @Override
    public List<ConfFile> generateUpstreamsConf(Set<Long> vsLookup, VirtualServer vs, List<Group> groups,
                                                Set<String> allDownServers, Set<String> allUpGroupServers,
                                                Set<String> visited) throws Exception {
        return upstreamsConf.generate(vsLookup, vs, groups, allDownServers, allUpGroupServers, visited);
    }
}
