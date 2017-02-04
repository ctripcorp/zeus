package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.model.entity.TrafficPolicy;
import com.ctrip.zeus.service.model.common.LocationEntry;
import com.ctrip.zeus.service.model.common.MetaType;
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
    public List<LocationEntry> getPolicyEntriesByGroup(Long groupId) throws Exception {
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
    public Map<Long, List<LocationEntry>> compareAndBuildLocationEntries(Long[] vsIds, Long escapedGroup) throws ValidationException {
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
                if (escapedGroup != null && e.getGroupId() == escapedGroup) continue;
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

    @Override
    public Map<Long, List<LocationEntry>> compareAndBuildLocationEntries(Long[] vsId, TrafficPolicy newGroupEntry) {
        return null;
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