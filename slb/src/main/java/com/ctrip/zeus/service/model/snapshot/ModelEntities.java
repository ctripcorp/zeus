package com.ctrip.zeus.service.model.snapshot;

import com.ctrip.zeus.model.model.Rule;
import com.ctrip.zeus.restful.message.view.ExtendedView;

import java.util.*;

public class ModelEntities {
    //All Slb Infos
    private Map<Long, ExtendedView.ExtendedSlb> slbs = new HashMap<>();
    //Incremental Vses
    private Map<Long, ExtendedView.ExtendedVs> vses = new HashMap<>();
    // ALL NxOnline Vs Ids
    private Set<Long> allNxOnlineVsIds = new HashSet<>();
    //Rules
    private List<Rule> defaultRules = new ArrayList<>();
    //Incremental VsId
    private Set<Long> incrementalVses = new HashSet<>();
    //Remove VsId
    private Set<Long> removeVsIds = new HashSet<>();
    //All Groups Referrer Of Vses.
    private Map<Long, List<ExtendedView.ExtendedGroup>> groupReferrerOfVses = new HashMap<>();
    //All Policies Referrer Of Vses.
    private Map<Long, List<ExtendedView.ExtendedTrafficPolicy>> policyReferrerOfVses = new HashMap<>();

    //Dr Map, Mapping Map<vsId,Map<sourceGroupId,Map<targetSlbId,weight>>>
    private Map<Long, Map<Long, Map<Long, Integer>>> vsIdSourceGroupIdTargetSlbIdWeightMap = new HashMap<>();
    //Map<GroupId,Dr>
    private Map<Long, ExtendedView.ExtendedDr> groupIdDrMap = new HashMap<>();


    public Map<Long, ExtendedView.ExtendedSlb> getSlbs() {
        return slbs;
    }

    public ModelEntities setSlbs(Map<Long, ExtendedView.ExtendedSlb> slbs) {
        this.slbs = slbs;
        return this;
    }

    public Map<Long, ExtendedView.ExtendedVs> getVses() {
        return vses;
    }

    public ModelEntities setVses(Map<Long, ExtendedView.ExtendedVs> vses) {
        this.vses = vses;
        return this;
    }

    public Set<Long> getAllNxOnlineVsIds() {
        return allNxOnlineVsIds;
    }

    public void setAllNxOnlineVsIds(Set<Long> allNxOnlineVsIds) {
        this.allNxOnlineVsIds = allNxOnlineVsIds;
    }

    public List<Rule> getDefaultRules() {
        return defaultRules;
    }

    public ModelEntities setDefaultRules(List<Rule> defaultRules) {
        this.defaultRules = defaultRules;
        return this;
    }

    public Set<Long> getIncrementalVses() {
        return incrementalVses;
    }

    public ModelEntities setIncrementalVses(Set<Long> incrementalVses) {
        this.incrementalVses = incrementalVses;
        return this;
    }

    public Set<Long> getRemoveVsIds() {
        return removeVsIds;
    }

    public ModelEntities setRemoveVsIds(Set<Long> removeVsIds) {
        this.removeVsIds = removeVsIds;
        return this;
    }

    public Map<Long, List<ExtendedView.ExtendedGroup>> getGroupReferrerOfVses() {
        return groupReferrerOfVses;
    }

    public ModelEntities setGroupReferrerOfVses(Map<Long, List<ExtendedView.ExtendedGroup>> groupReferrerOfVses) {
        this.groupReferrerOfVses = groupReferrerOfVses;
        return this;
    }

    public Map<Long, List<ExtendedView.ExtendedTrafficPolicy>> getPolicyReferrerOfVses() {
        return policyReferrerOfVses;
    }

    public ModelEntities setPolicyReferrerOfVses(Map<Long, List<ExtendedView.ExtendedTrafficPolicy>> policyReferrerOfVses) {
        this.policyReferrerOfVses = policyReferrerOfVses;
        return this;
    }

    public Map<Long, Map<Long, Map<Long, Integer>>> getVsIdSourceGroupIdTargetSlbIdWeightMap() {
        return vsIdSourceGroupIdTargetSlbIdWeightMap;
    }

    public ModelEntities setVsIdSourceGroupIdTargetSlbIdWeightMap(Map<Long, Map<Long, Map<Long, Integer>>> vsIdSourceGroupIdTargetSlbIdWeightMap) {
        this.vsIdSourceGroupIdTargetSlbIdWeightMap = vsIdSourceGroupIdTargetSlbIdWeightMap;
        return this;
    }

    public Map<Long, ExtendedView.ExtendedDr> getGroupIdDrMap() {
        return groupIdDrMap;
    }

    public ModelEntities setGroupIdDrMap(Map<Long, ExtendedView.ExtendedDr> groupIdDrMap) {
        this.groupIdDrMap = groupIdDrMap;
        return this;
    }
}
