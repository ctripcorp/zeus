package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.common.ErrorType;
import com.ctrip.zeus.service.model.common.LocationEntry;
import com.ctrip.zeus.service.model.common.MetaType;
import com.ctrip.zeus.service.model.common.ValidationContext;
import com.ctrip.zeus.service.model.handler.VsEntryFactory;
import org.springframework.stereotype.Repository;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2017/2/4.
 */
@Repository("vsEntryFactory")
public class VsEntryFactoryImpl implements VsEntryFactory {
    @Resource
    private RTrafficPolicyGroupDao rTrafficPolicyGroupDao;
    @Resource
    private RTrafficPolicyVsDao rTrafficPolicyVsDao;
    @Resource
    private RGroupVsDao rGroupVsDao;
    @Resource
    private SlbVirtualServerDao slbVirtualServerDao;

    @Override
    public List<LocationEntry> getGroupRelatedPolicyEntries(Long groupId) throws Exception {
        List<LocationEntry> result = new ArrayList<>();
        List<RTrafficPolicyGroupDo> groupRelatedPolicies = rTrafficPolicyGroupDao.findByGroupsAndPolicyVersion(new Long[]{groupId}, RTrafficPolicyGroupEntity.READSET_FULL);
        Integer[] hashes = new Integer[groupRelatedPolicies.size()];
        for (int i = 0; i < groupRelatedPolicies.size(); i++) {
            hashes[i] = groupRelatedPolicies.get(i).getHash();
        }
        for (RTrafficPolicyVsDo e : rTrafficPolicyVsDao.findAllByHash(hashes, RTrafficPolicyVsEntity.READSET_FULL)) {
            int i = Arrays.binarySearch(hashes, e.getHash());
            RTrafficPolicyGroupDo cmp = groupRelatedPolicies.get(i);
            if (e.getPolicyId() == cmp.getPolicyId() && e.getPolicyVersion() == cmp.getPolicyVersion()) {
                result.add(new LocationEntry().setEntryId(e.getPolicyId()).setEntryType(MetaType.TRAFFIC_POLICY).setVsId(e.getVsId()).setPriority(e.getPriority()).setPath(e.getPath()));
            }
        }
        return result;
    }

    @Override
    public Map<Long, List<LocationEntry>> getGroupRelatedPolicyEntriesByVs(Long[] groupIds) throws Exception {
        Map<Long, List<LocationEntry>> result = new HashMap<>();
        List<RTrafficPolicyGroupDo> groupRelatedPolicies = rTrafficPolicyGroupDao.findByGroupsAndPolicyVersion(groupIds, RTrafficPolicyGroupEntity.READSET_FULL);
        Integer[] hashes = new Integer[groupRelatedPolicies.size()];
        for (int i = 0; i < groupRelatedPolicies.size(); i++) {
            hashes[i] = groupRelatedPolicies.get(i).getHash();
        }
        for (RTrafficPolicyVsDo e : rTrafficPolicyVsDao.findAllByHash(hashes, RTrafficPolicyVsEntity.READSET_FULL)) {
            int i = Arrays.binarySearch(hashes, e.getHash());
            RTrafficPolicyGroupDo cmp = groupRelatedPolicies.get(i);
            if (e.getPolicyId() == cmp.getPolicyId() && e.getPolicyVersion() == cmp.getPolicyVersion()) {
                putArrayValueToMap(result, e.getVsId(), new LocationEntry().setEntryId(e.getPolicyId()).setEntryType(MetaType.TRAFFIC_POLICY).setVsId(e.getVsId()).setPriority(e.getPriority()).setPath(e.getPath()));
            }
        }
        return result;
    }

    @Override
    public Map<Long, List<LocationEntry>> getGroupEntriesByVs(Long[] groupIds) throws Exception {
        Map<Long, List<LocationEntry>> result = new HashMap<>();
        for (RelGroupVsDo e : rGroupVsDao.findAllByGroupOfflineVersion(groupIds, RGroupVsEntity.READSET_FULL)) {
            putArrayValueToMap(result, e.getVsId(), new LocationEntry().setVsId(e.getVsId()).setEntryId(e.getGroupId()).setPath(e.getPath()).setEntryType(MetaType.GROUP).setPriority(e.getPriority()));
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
                    result.add(new LocationEntry().setEntryId(group.getId()).setEntryType(MetaType.GROUP).setVsId(vsId).setPath(gvs.getPath()).setPriority(gvs.getPriority()));
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public Map<Long, List<LocationEntry>> buildLocationEntriesByVs(Long[] vsIds, Long[] escapedGroups, Long[] escapedPolicies) throws ValidationException {
        try {
            int vsCount = slbVirtualServerDao.findAllByIds(vsIds, SlbVirtualServerEntity.READSET_IDONLY).size();
            if (vsCount != vsIds.length) {
                throw new ValidationException("Field `group-virtual-server` is requesting combination to a non-existing virtual-server.");
            }
        } catch (DalException e) {
        }

        Map<Long, List<RTrafficPolicyVsDo>> pvsListByVsId = new HashMap<>();
        Map<Long, Set<Long>> groupIdsByPolicy = new HashMap<>();
        try {
            List<RTrafficPolicyVsDo> _pvsList = rTrafficPolicyVsDao.findByVsesAndPolicyVersion(vsIds, RTrafficPolicyVsEntity.READSET_FULL);
            Long[] _policyIds = new Long[_pvsList.size()];
            for (int i = 0; i < _pvsList.size(); i++) {
                RTrafficPolicyVsDo e = _pvsList.get(i);
                if (escapedPolicies != null && Arrays.binarySearch(escapedPolicies, e.getPolicyId()) >= 0) {
                    _policyIds[i] = -1L;
                    continue;
                }
                _policyIds[i] = e.getPolicyId();
                putArrayValueToMap(pvsListByVsId, e.getVsId(), e);
            }

            for (RTrafficPolicyGroupDo e : rTrafficPolicyGroupDao.findByPolicyAndPolicyVersion(_policyIds, RTrafficPolicyGroupEntity.READSET_FULL)) {
                Set<Long> v = groupIdsByPolicy.get(e.getPolicyId());
                if (v == null) {
                    v = new HashSet<>();
                    groupIdsByPolicy.put(e.getPolicyId(), v);
                }
                v.add(e.getGroupId());
            }
        } catch (DalException e) {
        }

        Map<Long, List<RelGroupVsDo>> gvsListByVsId = new HashMap<>();
        try {
            for (RelGroupVsDo e : rGroupVsDao.findByVsesAndGroupOfflineVersion(vsIds, RGroupVsEntity.READSET_FULL)) {
                if (escapedGroups != null && Arrays.binarySearch(escapedGroups, e.getGroupId()) >= 0) continue;
                putArrayValueToMap(gvsListByVsId, e.getVsId(), e);
            }
        } catch (DalException e) {
        }

        Map<Long, List<LocationEntry>> result = new HashMap<>();
        for (Long vsId : vsIds) {
            result.put(vsId, build(vsId, groupIdsByPolicy, pvsListByVsId, gvsListByVsId));
        }
        return result;
    }

    private List<LocationEntry> build(Long vsId, Map<Long, Set<Long>> groupIdsByPolicy,
                                      Map<Long, List<RTrafficPolicyVsDo>> pvsListByVsId, Map<Long, List<RelGroupVsDo>> gvsListByVsId) {
        List<LocationEntry> result = new ArrayList<>();
        List<RTrafficPolicyVsDo> pvsList = pvsListByVsId.get(vsId);
        Set<Long> escapedGroupIds = new HashSet<>();
        if (pvsList != null && pvsList.size() > 0) {
            for (RTrafficPolicyVsDo e : pvsList) {
                result.add(new LocationEntry().setEntryId(e.getPolicyId()).setEntryType(MetaType.TRAFFIC_POLICY).setVsId(e.getVsId()).setPath(e.getPath()).setPriority(e.getPriority()));
                Set<Long> groupIds = groupIdsByPolicy.get(e.getPolicyId());
                if (groupIds != null) {
                    escapedGroupIds.addAll(groupIds);
                }
            }
        }

        List<RelGroupVsDo> gvsList = gvsListByVsId.get(vsId);
        if (gvsList != null && gvsList.size() > 0) {
            for (RelGroupVsDo e : gvsList) {
                if (!escapedGroupIds.contains(e.getGroupId())) {
                    result.add(new LocationEntry().setVsId(e.getVsId()).setEntryId(e.getGroupId()).setPath(e.getPath()).setEntryType(MetaType.GROUP).setPriority(e.getPriority()));
                }
            }
        }
        return result;
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