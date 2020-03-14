package com.ctrip.zeus.service.build.impl;

import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.model.nginx.ConfFile;
import com.ctrip.zeus.model.nginx.NginxConfEntry;
import com.ctrip.zeus.model.nginx.Upstreams;
import com.ctrip.zeus.model.nginx.Vhosts;
import com.ctrip.zeus.restful.message.view.ExtendedView;
import com.ctrip.zeus.service.build.ConfSnapshotBuildService;
import com.ctrip.zeus.service.build.NginxConfBuilder;
import com.ctrip.zeus.service.model.snapshot.ModelEntities;
import com.ctrip.zeus.service.model.snapshot.ModelSnapshotEntity;
import com.ctrip.zeus.service.tools.local.LocalInfoService;
import com.ctrip.zeus.service.verify.verifier.PropertyValueUtils;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;



@Component("confSnapshotBuildService")
public class ConfSnapshotBuildServiceImpl implements ConfSnapshotBuildService {

    @Resource
    private NginxConfBuilder nginxConfBuilder;
    @Resource
    private LocalInfoService localInfoService;

    @VisibleForTesting
    public void setLocalInfoService(LocalInfoService localInfoService) {
        this.localInfoService = localInfoService;
    }

    public String buildNginxConf(ModelSnapshotEntity snapshot) throws Exception {
        ModelEntities modelEntities = snapshot.getModels();
        ExtendedView.ExtendedSlb nextOnlineSlb = modelEntities.getSlbs().get(snapshot.getTargetSlbId());
        List<Rule> defaultRules = modelEntities.getDefaultRules();
        return nginxConfBuilder.generateNginxConf(nextOnlineSlb.getInstance(), defaultRules, snapshot);
    }

    /*
     * @Description
     * Snapshot must not be null
     * @return
     **/
    public NginxConfEntry buildFullConfEntry(ModelSnapshotEntity snapshot) throws Exception {
        NginxConfEntry fullEntry = new NginxConfEntry().setUpstreams(new Upstreams()).setVhosts(new Vhosts());
        ModelEntities modelEntities = snapshot.getModels();

        ExtendedView.ExtendedSlb slb = modelEntities.getSlbs().get(snapshot.getTargetSlbId());

        Set<Long> lookupVsIds = new HashSet<>(snapshot.getModels().getVses().keySet());
//        for (ExtendedView.ExtendedVs extendedVs1 : snapshot.getModels().getVses().values()) {
//            lookupVsIds.add(extendedVs1.getId());
//        }

        for (ExtendedView.ExtendedVs extendedVs : modelEntities.getVses().values()) {
            if (extendedVs == null || modelEntities.getRemoveVsIds().contains(extendedVs.getId())) {
                continue;
            }
            ConfFile serverConfFile = buildServerConfFile(extendedVs.getInstance(), snapshot);

            fullEntry.getVhosts().addConfFile(serverConfFile);

            List<ConfFile> upstreamConfFiles = buildUpstreamConfFiles(slb.getInstance(), extendedVs.getInstance(), snapshot, lookupVsIds);
            for (ConfFile upStreamConf : upstreamConfFiles) {
                fullEntry.getUpstreams().addConfFile(upStreamConf);
            }
        }
        return fullEntry;
    }

    @Override
    public NginxConfEntry buildIncrementalEntry(ModelSnapshotEntity snapshot) throws Exception {
        if (snapshot != null) {
            ModelEntities modelEntities = snapshot.getModels();
            Slb nextOnlineSlb = modelEntities.getSlbs().get(snapshot.getTargetSlbId()).getInstance();

            Set<Long> lookupVsIds = snapshot.getModels().getAllNxOnlineVsIds();
            NginxConfEntry incrementalEntry = new NginxConfEntry().setVhosts(new Vhosts()).setUpstreams(new Upstreams());

            for (Long vsId : modelEntities.getIncrementalVses()) {
                if (modelEntities.getRemoveVsIds() != null && modelEntities.getRemoveVsIds().contains(vsId)) {
                    continue;
                }
                VirtualServer vs = modelEntities.getVses().get(vsId).getInstance();
                ConfFile serverConf = buildServerConfFile(vs, snapshot);
                List<ConfFile> upstreamConfs = buildUpstreamConfFiles(nextOnlineSlb, vs, snapshot, lookupVsIds);

                incrementalEntry.getVhosts().addConfFile(serverConf);
                for (ConfFile upstreamConf : upstreamConfs) {
                    incrementalEntry.getUpstreams().addConfFile(upstreamConf);
                }
            }
            return incrementalEntry;
        }
        return null;
    }

    public ConfFile buildServerConfFile(VirtualServer vs, ModelSnapshotEntity snapshot) throws Exception {
        // all the input parameters has been checked not to be null
        ModelEntities modelEntities = snapshot.getModels();
        Slb nextOnlineSlb = modelEntities.getSlbs().get(localInfoService.getLocalSlbId()).getInstance();

        List<ExtendedView.ExtendedTrafficPolicy> extendedTrafficPolicies = modelEntities.getPolicyReferrerOfVses().get(vs.getId());
        extendedTrafficPolicies = extendedTrafficPolicies == null ? new ArrayList<>() : extendedTrafficPolicies;
        List<TrafficPolicy> policies = new ArrayList<>();
        extendedTrafficPolicies.forEach(policy -> policies.add(policy.getInstance()));
        List<ExtendedView.ExtendedGroup> extendedGroups = modelEntities.getGroupReferrerOfVses().get(vs.getId());
        extendedGroups = extendedGroups == null ? new ArrayList<>() : extendedGroups;
        List<Group> groups = new ArrayList<>();
        extendedGroups.forEach(extendedGroup -> groups.add(extendedGroup.getInstance()));
        Map<Long, ExtendedView.ExtendedDr> extendedDrMap = modelEntities.getGroupIdDrMap();
        Map<Long, Dr> drMap = new HashMap<>(extendedDrMap.size());
        for (Long key : extendedDrMap.keySet()) {
            drMap.put(key, extendedDrMap.get(key).getInstance());
        }
        String confContent = nginxConfBuilder.generateServerConf(
                nextOnlineSlb,
                vs,
                policies,
                groups,
                modelEntities.getVsIdSourceGroupIdTargetSlbIdWeightMap().get(vs.getId()),
                drMap,
                getCanaryIpMap(vs, snapshot),
                modelEntities.getDefaultRules(), snapshot);
        return new ConfFile().setName("" + vs.getId()).setContent(confContent);
    }

    private Map<Long, String> getCanaryIpMap(VirtualServer vs, ModelSnapshotEntity snapshot) {
        Map<Long, String> canaryIpMap = new HashMap<>();
        ModelEntities modelEntities = snapshot.getModels();
        List<ExtendedView.ExtendedGroup> groups = modelEntities.getGroupReferrerOfVses().get(vs.getId());

        if (groups != null && groups.size() > 0) {
            for (ExtendedView.ExtendedGroup extendedGroup : groups) {
                String canaryIp = PropertyValueUtils.findByName(extendedGroup.getProperties(), "canaryIp");
                if (canaryIp != null && !canaryIp.isEmpty()) {
                    canaryIpMap.put(extendedGroup.getId(), canaryIp);
                }
            }
        }

        return canaryIpMap;
    }

    private List<ConfFile> buildUpstreamConfFiles(Slb nextOnlineSlb, VirtualServer vs, ModelSnapshotEntity snapshot, Set<Long> lookupVsIds) throws Exception {
        // Generate all ConfFiles corresponding to every group of the passed vs
        // Called by fullUpdate and incrementalUpdate method
        // While in incrementalUpdate method, use UpstreamConfPicker to pick the ones you need to dyups
        ModelEntities modelEntities = snapshot.getModels();

        Set<String> fileTrack = new HashSet<>();

        List<ExtendedView.ExtendedGroup> extendedGroups = snapshot.getModels().getGroupReferrerOfVses().get(vs.getId());
        extendedGroups = extendedGroups == null ? new ArrayList<>() : extendedGroups;
        List<Group> groups = new ArrayList<>();
        extendedGroups.forEach(group -> groups.add(group.getInstance()));
        return nginxConfBuilder.generateUpstreamsConf(
                lookupVsIds,
                vs,
                groups,
                snapshot.getAllDownServers(),
                snapshot.getAllUpGroupServers(),
                fileTrack,
                modelEntities.getDefaultRules(),
                nextOnlineSlb);
    }
}
