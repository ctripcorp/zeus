package com.ctrip.zeus.service.model.validation;

import com.ctrip.zeus.dao.entity.*;
import com.ctrip.zeus.dao.mapper.SlbGroupVsRMapper;
import com.ctrip.zeus.dao.mapper.SlbTrafficPolicyGroupRMapper;
import com.ctrip.zeus.dao.mapper.SlbTrafficPolicyVsRMapper;
import com.ctrip.zeus.dao.mapper.SlbVirtualServerMapper;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.service.model.common.ErrorType;
import com.ctrip.zeus.service.model.common.LocationEntry;
import com.ctrip.zeus.service.model.common.MetaType;
import com.ctrip.zeus.service.model.common.ValidationContext;
import com.ctrip.zeus.service.model.handler.model.GroupVirtualServerContent;
import com.ctrip.zeus.support.DefaultObjectJsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2017/2/4.
 */
@Repository("vsEntryFactory")
public class VsEntryFactoryImpl implements VsEntryFactory {
    @Resource
    private SlbTrafficPolicyGroupRMapper slbTrafficPolicyGroupRMapper;
    @Resource
    private SlbTrafficPolicyVsRMapper slbTrafficPolicyVsRMapper;
    @Resource
    private SlbGroupVsRMapper slbGroupVsRMapper;

    @Resource
    private SlbVirtualServerMapper slbVirtualServerMapper;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public List<LocationEntry> getGroupRelatedPolicyEntries(Long groupId) throws Exception {
        List<LocationEntry> result = new ArrayList<>();
        List<Long> groupIds = new ArrayList<>();
        groupIds.add(groupId);

        List<SlbTrafficPolicyGroupR> groupRelatedPolicies = slbTrafficPolicyGroupRMapper.findByGroupsAndPolicyVersion(groupIds);
        if (groupRelatedPolicies.size() == 0) return result;

        Integer[] hashes = new Integer[groupRelatedPolicies.size()];
        for (int i = 0; i < groupRelatedPolicies.size(); i++) {
            hashes[i] = groupRelatedPolicies.get(i).getHash();
        }

        for (SlbTrafficPolicyVsR e : slbTrafficPolicyVsRMapper.selectByExample(new SlbTrafficPolicyVsRExample().createCriteria().andHashIn(Arrays.asList(hashes)).example())) {
            int i = Arrays.binarySearch(hashes, e.getHash());
            SlbTrafficPolicyGroupR cmp = groupRelatedPolicies.get(i);
            if (e.getPolicyId().equals(cmp.getPolicyId()) && e.getPolicyVersion().equals(cmp.getPolicyVersion())) {
                result.add(new LocationEntry().setEntryId(e.getPolicyId()).setEntryType(MetaType.TRAFFIC_POLICY).setVsId(e.getVsId()).setPriority(e.getPriority()).setPath(e.getPath()));
            }
        }
        return result;
    }

    @Override
    public Map<Long, List<LocationEntry>> getGroupRelatedPolicyEntriesByVs(Long[] groupIds) throws Exception {
        Map<Long, List<LocationEntry>> result = new HashMap<>();
        if (groupIds == null || groupIds.length == 0) return result;
        List<SlbTrafficPolicyGroupR> groupRelatedPolicies = slbTrafficPolicyGroupRMapper.findByGroupsAndPolicyVersion(Arrays.asList(groupIds));
        if (groupRelatedPolicies.size() == 0) return result;

        Integer[] hashes = new Integer[groupRelatedPolicies.size()];
        for (int i = 0; i < groupRelatedPolicies.size(); i++) {
            hashes[i] = groupRelatedPolicies.get(i).getHash();
        }
        for (SlbTrafficPolicyVsR e : slbTrafficPolicyVsRMapper.selectByExample(new SlbTrafficPolicyVsRExample().createCriteria().andHashIn(Arrays.asList(hashes)).example())) {
            int i = Arrays.binarySearch(hashes, e.getHash());
            SlbTrafficPolicyGroupR cmp = groupRelatedPolicies.get(i);
            if (e.getPolicyId().equals(cmp.getPolicyId()) && e.getPolicyVersion().equals(cmp.getPolicyVersion())) {
                putArrayValueToMap(result, e.getVsId(), new LocationEntry().setEntryId(e.getPolicyId()).setEntryType(MetaType.TRAFFIC_POLICY).setVsId(e.getVsId()).setPriority(e.getPriority()).setPath(e.getPath()));
            }
        }
        return result;
    }

    @Override
    public Map<Long, List<LocationEntry>> getGroupEntriesByVs(Long[] groupIds) throws Exception {
        Map<Long, List<LocationEntry>> result = new HashMap<>();
        if (groupIds == null || groupIds.length == 0) return result;
        for (SlbGroupVsR e : slbGroupVsRMapper.findAllByGroupOfflineVersion(Arrays.asList(groupIds))) {
            putArrayValueToMap(result, e.getVsId(), buildLocationEntry(e));
        }
        return result;
    }

    @Override
    public Map<Long, LocationEntry> mapPolicyEntriesByGroup(Long vsId, List<TrafficPolicy> policies, ValidationContext context) {
        Map<Long, LocationEntry> result = new HashMap<>();
        for (TrafficPolicy policy : policies) {
            PolicyVirtualServer target = null;
            for (PolicyVirtualServer pvs : policy.getPolicyVirtualServers()) {
                if (pvs.getVirtualServer().getId().equals(vsId)) {
                    target = pvs;
                    break;
                }
            }
            if (target == null) {
                context.error(policy.getId(), MetaType.TRAFFIC_POLICY, ErrorType.DEPENDENCY_VALIDATION, "Cannot find target on virtual server " + vsId + ".");
                continue;
            }
            LocationEntry e = new LocationEntry().setEntryId(policy.getId()).setEntryType(MetaType.TRAFFIC_POLICY).setVsId(vsId).setPath(target.getPath()).setPriority(target.getPriority());
            for (TrafficControl c : policy.getControls()) {
                LocationEntry prev = result.put(c.getGroup().getId(), e);
                if (prev != null) {
                    context.error(policy.getId(), MetaType.TRAFFIC_POLICY, ErrorType.DEPENDENCY_VALIDATION, "Some other traffic policies have occupied traffic-controls on vs " + vsId + ".");
                }
            }
        }
        return result;
    }

    @Override
    public List<LocationEntry> filterGroupEntriesByVs(Long vsId, List<Group> groups, ValidationContext context) {
        List<LocationEntry> result = new ArrayList<>();
        for (Group group : groups) {
            for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
                if (gvs.getVirtualServer().getId().equals(vsId)) {
                    result.add(new LocationEntry().setEntryId(group.getId()).setEntryType(MetaType.GROUP).setVsId(vsId).setName(gvs.getName()).setPath(gvs.getPath()).setPriority(gvs.getPriority()));
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public Map<Long, List<LocationEntry>> buildLocationEntriesByVs(Long[] vsIds, Long[] escapedGroups, Long[] escapedPolicies) throws ValidationException {
        Map<Long, List<LocationEntry>> result = new HashMap<>();

        if (vsIds != null && vsIds.length > 0) {
            int vsCount = slbVirtualServerMapper.selectByExampleSelective(new SlbVirtualServerExample().createCriteria().andIdIn(Arrays.asList(vsIds)).example(), SlbVirtualServer.Column.id).size();
            if (vsCount != vsIds.length) {
                throw new ValidationException("Field `group-virtual-server` is requesting combination to a non-existing virtual-server.");
            }
        }
        Map<Long, Set<Long>> groupIdsByPolicy = new HashMap<>();
        Map<Long, List<SlbTrafficPolicyVsR>> slbpvsListByVsId = new HashMap<>();
        try {
            if (vsIds != null && vsIds.length > 0) {
                List<SlbTrafficPolicyVsR> pvsTmpList = slbTrafficPolicyVsRMapper.findByVsesAndPolicyVersion(Arrays.asList(vsIds));
                if (pvsTmpList != null && pvsTmpList.size() > 0) {
                    Long[] policyIds = new Long[pvsTmpList.size()];
                    for (int i = 0; i < pvsTmpList.size(); i++) {
                        SlbTrafficPolicyVsR e = pvsTmpList.get(i);
                        if (escapedPolicies != null && Arrays.binarySearch(escapedPolicies, e.getPolicyId()) >= 0) {
                            policyIds[i] = -1L;
                            continue;
                        }
                        policyIds[i] = e.getPolicyId();
                        putArrayValueToMap(slbpvsListByVsId, e.getVsId(), e);
                    }

                    for (SlbTrafficPolicyGroupR e : slbTrafficPolicyGroupRMapper.findByPolicyAndPolicyVersion(Arrays.asList(policyIds))) {
                        Set<Long> v = groupIdsByPolicy.get(e.getPolicyId());
                        if (v == null) {
                            v = new HashSet<>();
                            groupIdsByPolicy.put(e.getPolicyId(), v);
                        }
                        v.add(e.getGroupId());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to get Vs-Policy Map, Exception:" + e);
        }


        Map<Long, List<SlbGroupVsR>> gvsListByVsId = new HashMap<>();
        if (vsIds == null || vsIds.length == 0) return result;

        try {
            for (SlbGroupVsR e : slbGroupVsRMapper.findByVsesAndGroupOfflineVersion(Arrays.asList(vsIds))) {
                if (escapedGroups != null && Arrays.binarySearch(escapedGroups, e.getGroupId()) >= 0) continue;
                putArrayValueToMap(gvsListByVsId, e.getVsId(), e);
            }
        } catch (Exception ex) {
            logger.error("Failed to get Group-Vs Relation with error message: " + ex);
        }

        for (Long vsId : vsIds) {
            result.put(vsId, build(vsId, groupIdsByPolicy, slbpvsListByVsId, gvsListByVsId));
        }
        return result;
    }

    private List<LocationEntry> build(Long vsId, Map<Long, Set<Long>> groupIdsByPolicy,
                                      Map<Long, List<SlbTrafficPolicyVsR>> pvsListByVsId, Map<Long, List<SlbGroupVsR>> gvsListByVsId) {
        List<LocationEntry> result = new ArrayList<>();
        List<SlbTrafficPolicyVsR> pvsList = pvsListByVsId.get(vsId);
        Set<Long> escapedGroupIds = new HashSet<>();
        if (pvsList != null && pvsList.size() > 0) {
            for (SlbTrafficPolicyVsR e : pvsList) {
                result.add(new LocationEntry().setEntryId(e.getPolicyId()).setEntryType(MetaType.TRAFFIC_POLICY).setVsId(e.getVsId()).setPath(e.getPath()).setPriority(e.getPriority()));
                Set<Long> groupIds = groupIdsByPolicy.get(e.getPolicyId());
                if (groupIds != null) {
                    escapedGroupIds.addAll(groupIds);
                }
            }
        }

        List<SlbGroupVsR> gvsList = gvsListByVsId.get(vsId);
        if (gvsList != null && gvsList.size() > 0) {
            for (SlbGroupVsR e : gvsList) {
                if (!escapedGroupIds.contains(e.getGroupId())) {
                    result.add(buildLocationEntry(e));
                }
            }
        }
        return result;
    }

    private LocationEntry buildLocationEntry(SlbGroupVsR e) {
        LocationEntry entry = new LocationEntry().setVsId(e.getVsId()).setEntryId(e.getGroupId()).setEntryType(MetaType.GROUP);
        if (e.getContent() != null) {
            GroupVirtualServerContent gvsContent = DefaultObjectJsonParser.parse(e.getContent(), GroupVirtualServerContent.class);
            if (gvsContent != null) {
                entry.setName(gvsContent.getName()).setPath(gvsContent.getPath()).setPriority(gvsContent.getPriority()).setName(gvsContent.getName());
            }
        }
        return entry;
    }

    private <T> void putArrayValueToMap(Map<Long, List<T>> map, Long key, T e) {
        List<T> v = map.get(key);
        if (v == null) {
            v = new ArrayList<>();
            map.put(key, v);
        }
        v.add(e);
    }
}