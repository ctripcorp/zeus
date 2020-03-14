package com.ctrip.zeus.service.build.impl;

import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.model.nginx.ConfFile;
import com.ctrip.zeus.service.build.NginxConfBuilder;
import com.ctrip.zeus.service.build.conf.NginxConf;
import com.ctrip.zeus.service.build.conf.ServerConf;
import com.ctrip.zeus.service.build.conf.UpstreamsConf;
import com.ctrip.zeus.service.model.snapshot.ModelSnapshotEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by fanqq on 2015/3/30.
 */
@Service("nginxConfigBuilder")
public class NginxConfBuilderImpl implements NginxConfBuilder {

    @Autowired
    NginxConf nginxConf;
    @Resource
    ServerConf serverConf;
    @Resource
    UpstreamsConf upstreamsConf;

    @Override
    public String generateNginxConf(Slb slb, List<Rule> defaultRules, ModelSnapshotEntity snapshot) throws Exception {
        return nginxConf.generate(slb, defaultRules, snapshot);
    }

    @Override
    public String generateServerConf(Slb slb, VirtualServer vs, List<TrafficPolicy> policies, List<Group> groups,
                                     Map<Long, Map<Long, Integer>> drDesSlbByGroupIds, Map<Long, Dr> drByGroupIds,
                                     Map<Long, String> canaryIpMap, List<Rule> defaultRules, ModelSnapshotEntity snapshot) throws Exception {
        if (vs.getDomains().size() == 0) {
            return "";
        }

        final Map<String, Object> objectOnVsReferrer = new HashMap<>();
        policies = policies == null ? new ArrayList<>() : policies;
        groups = groups == null ? new ArrayList<>() : groups;

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
        policies.sort((o1, o2) -> {
            int result = ((PolicyVirtualServer) objectOnVsReferrer.get("pvs-" + o2.getId())).getPriority() -
                    ((PolicyVirtualServer) objectOnVsReferrer.get("pvs-" + o1.getId())).getPriority();
            return result == 0 ? o2.getId().compareTo(o1.getId()) : result;
        });
        groups.sort((o1, o2) -> {
            int result = ((GroupVirtualServer) objectOnVsReferrer.get("gvs-" + o2.getId())).getPriority() -
                    ((GroupVirtualServer) objectOnVsReferrer.get("gvs-" + o1.getId())).getPriority();
            return result == 0 ? o2.getId().compareTo(o1.getId()) : result;
        });
        return serverConf.generate(slb, vs, policies, groups, drDesSlbByGroupIds, drByGroupIds, objectOnVsReferrer, canaryIpMap, defaultRules, snapshot);
    }

    @Override
    public List<ConfFile> generateUpstreamsConf(Set<Long> vsLookup, VirtualServer vs, List<Group> groups,
                                                Set<String> allDownServers, Set<String> allUpGroupServers,
                                                Set<String> visited, List<Rule> defaultRules, Slb nxOnlineSlb) throws Exception {
        return upstreamsConf.generate(vsLookup, vs, groups, allDownServers, allUpGroupServers, visited,defaultRules,nxOnlineSlb);
    }
}
