package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.PathValidator;
import com.ctrip.zeus.service.model.VersionUtils;
import com.ctrip.zeus.service.model.common.MetaType;
import com.ctrip.zeus.service.model.handler.TrafficPolicyValidator;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Service;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2017/1/13.
 */
@Service("trafficPolicyValidator")
public class DefaultTrafficPolicyValidator implements TrafficPolicyValidator {
    @Resource
    private TrafficPolicyDao trafficPolicyDao;
    @Resource
    private RTrafficPolicyVsDao rTrafficPolicyVsDao;
    @Resource
    private RTrafficPolicyGroupDao rTrafficPolicyGroupDao;
    @Resource
    private RGroupVsDao rGroupVsDao;
    @Resource
    private PathValidator pathValidator;
    @Resource
    private RGroupStatusDao rGroupStatusDao;

    @Override
    public boolean exists(Long targetId) throws Exception {
        return trafficPolicyDao.findById(targetId, TrafficPolicyEntity.READSET_FULL) != null;
    }

    @Override
    public void validate(TrafficPolicy target) throws Exception {
        validate(target, false);
    }

    @Override
    public void validate(TrafficPolicy target, boolean escapePathValidation) throws Exception {
        if (target.getName() == null) {
            throw new ValidationException("Field `name` is empty.");
        }
        TrafficPolicyDo nameCheck = trafficPolicyDao.findByName(target.getName(), TrafficPolicyEntity.READSET_FULL);
        if (nameCheck != null) {
            throw new ValidationException("Traffic policy is requesting a name " + target.getName() + " which has been taken by an existing policy " + nameCheck.getId() + ".");
        }

        Long[] groupIds = new Long[target.getControls().size()];
        for (int i = 0; i < target.getControls().size(); i++) {
            groupIds[i] = target.getControls().get(i).getGroup().getId();
        }
        Arrays.sort(groupIds);

        Long[] vsIds = new Long[target.getPolicyVirtualServers().size()];
        for (int i = 0; i < target.getPolicyVirtualServers().size(); i++) {
            vsIds[i] = target.getPolicyVirtualServers().get(i).getVirtualServer().getId();
        }
        Arrays.sort(vsIds);
        Long prev = vsIds[0];
        for (int i = 1; i < vsIds.length; i++) {
            if (prev.equals(vsIds[i])) {
                throw new ValidationException("Traffic policy that you tries to create/modify declares the same vs " + prev + " more than once.");
            }
            prev = vsIds[i];
        }

        Map<Long, List<RelGroupVsDo>> gvsListByVsId = new HashMap<>();
        for (RelGroupVsDo e : rGroupVsDao.findByVsesAndGroupOfflineVersion(vsIds, RGroupVsEntity.READSET_FULL)) {
            putArrayEntryValue(gvsListByVsId, e.getVsId(), e);
        }
        validatePolicyControls(target, groupIds, vsIds, gvsListByVsId);

        Map<Long, List<RTrafficPolicyVsDo>> pvsListByVsId = new HashMap<>();
        for (RTrafficPolicyVsDo e : rTrafficPolicyVsDao.findByVsesAndPolicyVersion(vsIds, RTrafficPolicyVsEntity.READSET_FULL)) {
            putArrayEntryValue(pvsListByVsId, e.getVsId(), e);
        }

        Map<Long, List<RTrafficPolicyGroupDo>> policyListByGroupId = new HashMap<>();
        for (RTrafficPolicyGroupDo e : rTrafficPolicyGroupDao.findByGroupsAndPolicyVersion(groupIds, RTrafficPolicyGroupEntity.READSET_FULL)) {
            putArrayEntryValue(policyListByGroupId, e.getGroupId(), e);
        }

        Map<Long, List<PathValidator.LocationEntry>> currentLocationEntriesByVs = new HashMap<>();
        compareAndBuildCurrentLocationEntries(target, groupIds, vsIds, policyListByGroupId, pvsListByVsId, gvsListByVsId,
                escapePathValidation ? null : currentLocationEntriesByVs);

        validatePathPriority(target, escapePathValidation, currentLocationEntriesByVs);
    }

    @Override
    public void validateForMerge(Long[] toBeMergedItems, Long vsId, Map<Long, Group> groupRef, Map<Long, TrafficPolicy> policyRef, boolean escapePathValidation) throws Exception {
        Map<Long, PathValidator.LocationEntry> groupEntriesById = new HashMap<>();
        Set<Long> groupAsTrafficControl = new HashSet<>();
        for (TrafficPolicy p : policyRef.values()) {
            for (TrafficControl c : p.getControls()) {
                if (!groupAsTrafficControl.add(c.getGroup().getId())) {
                    throw new ValidationException("Another traffic policy " + c.getGroup().getId() + " has occupied one of the traffic-controls on vs " + vsId + ".");
                }
            }
        }
        for (Group g : groupRef.values()) {
            if (groupAsTrafficControl.contains(g.getId())) continue;
            for (GroupVirtualServer gvs : g.getGroupVirtualServers()) {
                if (gvs.getVirtualServer().getId().equals(vsId)) {
                    groupEntriesById.put(g.getId(), new PathValidator.LocationEntry().setEntryId(g.getId()).setEntryType(MetaType.GROUP).setVsId(vsId).setPath(gvs.getPath()).setPriority(gvs.getPriority()));
                }
            }
        }

        for (Long i : toBeMergedItems) {
            TrafficPolicy p = policyRef.get(i);
            String path = null;
            Integer priority = Integer.MAX_VALUE;
            for (PolicyVirtualServer pvs : p.getPolicyVirtualServers()) {
                if (pvs.getVirtualServer().getId().equals(vsId)) {
                    path = pvs.getPath();
                    priority = pvs.getPriority();
                }
            }
            for (TrafficControl c : p.getControls()) {
                Group e = groupRef.get(c.getGroup().getId());
                if (e == null) {
                    throw new ValidationException("Group " + c.getGroup().getId() + " is missing combination on vs " + vsId + ".");
                }
                for (GroupVirtualServer gvs : e.getGroupVirtualServers()) {
                    if (gvs.getVirtualServer().getId().equals(vsId)) {
                        if (gvs.getPriority() > priority) {
                            throw new ValidationException("Traffic policy has lower `priority` than its control item " + c.getGroup().getId() + " on vs " + vsId + ".");
                        }
                        if (!gvs.getPath().equals(path)) {
                            throw new ValidationException("Traffic policy and its control item " + c.getGroup().getId() + " does not have the same `path` value on vs " + vsId + ".");
                        }
                    }
                }
            }

        }

        Map<Long, List<PathValidator.LocationEntry>> locationEntries = new HashMap<>();
        ArrayList<PathValidator.LocationEntry> values = new ArrayList<>(groupEntriesById.values());
        locationEntries.put(vsId, values);
        for (TrafficPolicy p : policyRef.values()) {
            for (PolicyVirtualServer pvs : p.getPolicyVirtualServers()) {
                if (pvs.getVirtualServer().getId().equals(vsId)) {
                    values.add(new PathValidator.LocationEntry().setEntryId(p.getId()).setEntryType(MetaType.TRAFFIC_POLICY).setVsId(vsId).setPath(pvs.getPath()).setPriority(pvs.getPriority()));
                }
            }
        }
        for (Long i : toBeMergedItems) {
            TrafficPolicy p = policyRef.get(i);
            validatePathPriority(p, escapePathValidation, locationEntries);
        }
    }

    private void validatePathPriority(TrafficPolicy target, boolean escapePathValidation, Map<Long, List<PathValidator.LocationEntry>> currentLocationEntriesByVs) throws ValidationException {
        for (PolicyVirtualServer e : target.getPolicyVirtualServers()) {
            Long vsId = e.getVirtualServer().getId();
            e.setVirtualServer(new VirtualServer().setId(vsId));
            if (escapePathValidation) {
                if (e.getPriority() == null) {
                    throw new ValidationException("Field `priority` cannot be empty if validation is escaped.");
                }
            } else {
                PathValidator.LocationEntry insertEntry = new PathValidator.LocationEntry().setEntryId(target.getId()).setEntryType(MetaType.TRAFFIC_POLICY).setVsId(vsId).setPath(e.getPath()).setPriority(e.getPriority() == null ? 1000 : e.getPriority());
                pathValidator.checkOverlapRestriction(vsId, insertEntry, currentLocationEntriesByVs.get(vsId));
                if (e.getPriority() == null) {
                    e.setPriority(insertEntry.getPriority());
                    e.setPath(insertEntry.getPath());
                } else {
                    if (!e.getPriority().equals(insertEntry.getPriority())) {
                        throw new ValidationException("Traffic policy that you tries to create/modify may cause path prefix-overlap problem with other entries on virtual-server " + vsId + ". Recommend priority will be " + insertEntry.getPriority() + ".");
                    }
                    e.setPath(insertEntry.getPath());
                }
            }
        }
    }

    @Override
    public void validateForActivate(TrafficPolicy[] toBeActivatedItems, boolean escapedPathValidation) throws Exception {
        Set<Long> vsLookup = new HashSet<>();
        Set<Long> groupLookup = new HashSet<>();
        Long[] policies = new Long[toBeActivatedItems.length];
        Integer[] hashes = new Integer[toBeActivatedItems.length];
        for (int i = 0; i < toBeActivatedItems.length; i++) {
            TrafficPolicy e = toBeActivatedItems[i];
            for (PolicyVirtualServer ee : e.getPolicyVirtualServers()) {
                vsLookup.add(ee.getVirtualServer().getId());
            }
            for (TrafficControl c : e.getControls()) {
                groupLookup.add(c.getGroup().getId());
            }
            policies[i] = e.getId();
            hashes[i] = VersionUtils.getHash(e.getId(), e.getVersion());
        }
        Long[] groupIds = groupLookup.toArray(new Long[groupLookup.size()]);
        for (RelGroupStatusDo e : rGroupStatusDao.findByGroups(groupIds, RGroupStatusEntity.READSET_FULL)) {
            if (e.getOnlineVersion() < 0) {
                throw new ValidationException("Group " + e.getGroupId() + " has not been activated.");
            }
        }

        Long[] vsIds = vsLookup.toArray(new Long[vsLookup.size()]);

        Map<Long, List<RelGroupVsDo>> gvsListByVsId = new HashMap<>();
        for (RelGroupVsDo e : rGroupVsDao.findByVsesAndGroupOnlineVersion(vsIds, RGroupVsEntity.READSET_FULL)) {
            putArrayEntryValue(gvsListByVsId, e.getVsId(), e);
        }

        Map<Long, List<RTrafficPolicyVsDo>> pvsListByVsId = new HashMap<>();
        for (RTrafficPolicyVsDo e : rTrafficPolicyVsDao.findByVsesAndPolicyActiveVersion(vsIds, RTrafficPolicyVsEntity.READSET_FULL)) {
            if (Arrays.binarySearch(policies, e.getPolicyId()) >= 0) continue;
            putArrayEntryValue(pvsListByVsId, e.getVsId(), e);
        }
        for (RTrafficPolicyVsDo e : rTrafficPolicyVsDao.findAllByHash(hashes, RTrafficPolicyVsEntity.READSET_FULL)) {
            int i = Arrays.binarySearch(policies, e.getPolicyId());
            if (i >= 0 && toBeActivatedItems[i].getVersion().equals(e.getPolicyVersion())) {
                putArrayEntryValue(pvsListByVsId, e.getVsId(), e);
            }
        }

        Map<Long, List<RTrafficPolicyGroupDo>> policyListByGroupId = new HashMap<>();
        for (RTrafficPolicyGroupDo e : rTrafficPolicyGroupDao.findByGroupsAndPolicyActiveVersion(groupIds, RTrafficPolicyGroupEntity.READSET_FULL)) {
            if (Arrays.binarySearch(policies, e.getPolicyId()) >= 0) continue;
            putArrayEntryValue(policyListByGroupId, e.getGroupId(), e);
        }
        for (RTrafficPolicyGroupDo e : rTrafficPolicyGroupDao.findAllByHash(hashes, RTrafficPolicyGroupEntity.READSET_FULL)) {
            int i = Arrays.binarySearch(policies, e.getPolicyId());
            if (i >= 0 && toBeActivatedItems[i].getVersion().equals(e.getPolicyVersion())) {
                putArrayEntryValue(policyListByGroupId, e.getGroupId(), e);
            }
        }

        for (TrafficPolicy e : toBeActivatedItems) {
            Long[] g = new Long[e.getControls().size()];
            for (int i = 0; i < e.getControls().size(); i++) {
                g[i] = e.getControls().get(i).getGroup().getId();
            }
            Arrays.sort(g);
            Long[] v = new Long[e.getPolicyVirtualServers().size()];
            for (int i = 0; i < e.getPolicyVirtualServers().size(); i++) {
                v[i] = e.getPolicyVirtualServers().get(i).getVirtualServer().getId();
            }
            Arrays.sort(v);

            validatePolicyControls(e, g, v, gvsListByVsId);

            Map<Long, List<PathValidator.LocationEntry>> currentLocationEntriesByVs = new HashMap<>();
            compareAndBuildCurrentLocationEntries(e, g, v, policyListByGroupId, pvsListByVsId, gvsListByVsId,
                    escapedPathValidation ? null : currentLocationEntriesByVs);

            validatePathPriority(e, escapedPathValidation, currentLocationEntriesByVs);
        }
    }

    @Override
    public void validateForDeactivate(Long[] toBeDeactivatedItems) throws Exception {

    }

    private <T> void putArrayEntryValue(Map<Long, List<T>> map, Long key, T e) {
        List<T> v = map.get(key);
        if (v == null) {
            v = new ArrayList<>();
            map.put(key, v);
        }
        v.add(e);
    }

    private void validatePolicyControls(TrafficPolicy target, Long[] groupIds, Long[] vsIds, Map<Long, List<RelGroupVsDo>> gvsListByVsId) throws DalException, ValidationException {
        Long prev = groupIds[0];
        for (int i = 1; i < groupIds.length; i++) {
            if (prev.equals(groupIds[i])) {
                throw new ValidationException("Traffic policy that you try to create/modify declares the same group " + prev + " more than once.");
            }
            prev = groupIds[i];
        }
        if (groupIds.length <= 1) {
            throw new ValidationException("Traffic policy that you try to create/modify does not have enough traffic-controls.");
        }
        if (rGroupStatusDao.findByGroups(groupIds, RGroupStatusEntity.READSET_FULL).size() != groupIds.length) {
            throw new ValidationException("Traffic policy that you try to create/modify contains group that does not exist.");
        }

        int[] versions = new int[groupIds.length];
        RelGroupVsDo[][] combinations = new RelGroupVsDo[groupIds.length][vsIds.length];
        for (Long vsId : vsIds) {
            List<RelGroupVsDo> relGroupVsList = gvsListByVsId.get(vsId);
            if (relGroupVsList == null || relGroupVsList.size() == 0) continue;

            for (RelGroupVsDo e : relGroupVsList) {
                int i = Arrays.binarySearch(groupIds, e.getGroupId());
                int j = Arrays.binarySearch(vsIds, e.getVsId());
                if (i >= 0 && j >= 0) {
                    if (versions[i] <= e.getGroupVersion()) {
                        versions[i] = e.getGroupVersion();
                        combinations[i][j] = e;
                    }
                }
            }
        }

        // Check if traffic-control item shares the same/more vs combinations with traffic-policy
        for (PolicyVirtualServer e : target.getPolicyVirtualServers()) {
            int j = Arrays.binarySearch(vsIds, e.getVirtualServer().getId());
            for (int i = 0; i < groupIds.length; i++) {
                RelGroupVsDo v = combinations[i][j];
                if (v == null || v.getGroupVersion() != versions[i]) {
                    throw new ValidationException("Group " + groupIds[i] + " is missing combination on vs " + vsIds[j] + ".");
                }
                if (v.getPriority() > e.getPriority()) {
                    throw new ValidationException("Traffic policy has lower `priority` than its control item " + groupIds[i] + " on vs " + vsIds[j] + ".");
                }
                if (!v.getPath().equals(e.getPath())) {
                    throw new ValidationException("Traffic policy and its control item " + groupIds[i] + " does not have the same `path` value on vs " + vsIds[i] + ".");
                }
            }
        }
    }

    private void compareAndBuildCurrentLocationEntries(TrafficPolicy trafficPolicy, Long[] groupIds, Long[] vsIds,
                                                       Map<Long, List<RTrafficPolicyGroupDo>> policiesByGroupId,
                                                       Map<Long, List<RTrafficPolicyVsDo>> pvsListByVsId,
                                                       Map<Long, List<RelGroupVsDo>> gvsListByVsId,
                                                       Map<Long, List<PathValidator.LocationEntry>> currentLocationEntriesByVs) throws ValidationException {
        // Check if traffic-policy is unique for every group-vs pair
        Set<Long> tmp = Sets.newHashSet(groupIds);
        tmp.retainAll(policiesByGroupId.keySet());
        List<Long> groupRelatedPolicies = new ArrayList<>();
        for (Long i : tmp) {
            for (RTrafficPolicyGroupDo e : policiesByGroupId.get(i)) {
                if (trafficPolicy.getId() != null && e.getPolicyId() == trafficPolicy.getId()) continue;
                groupRelatedPolicies.add(e.getPolicyId());
            }
        }

        for (Long vsId : vsIds) {
            List<RTrafficPolicyVsDo> pvsList = pvsListByVsId.get(vsId);
            if (pvsList == null || pvsList.size() == 0) continue;

            for (RTrafficPolicyVsDo e : pvsList) {
                if (trafficPolicy.getId() != null && e.getPolicyId() == trafficPolicy.getId()) continue;
                if (groupRelatedPolicies.indexOf(e.getPolicyId()) >= 0) {
                    throw new ValidationException("Another traffic policy " + e.getId() + " has occupied one of the traffic-controls on vs " + e.getVsId() + ".");
                }
                if (currentLocationEntriesByVs != null) {
                    putArrayEntryValue(currentLocationEntriesByVs, e.getVsId(), new PathValidator.LocationEntry().setEntryId(e.getPolicyId()).setEntryType(MetaType.TRAFFIC_POLICY).setVsId(e.getVsId()).setPath(e.getPath()).setPriority(e.getPriority()));
                }
            }
        }

        for (Long vsId : vsIds) {
            List<RelGroupVsDo> gvsList = gvsListByVsId.get(vsId);
            if (gvsList == null || gvsList.size() == 0) continue;

            for (RelGroupVsDo e : gvsList) {
                if (Arrays.binarySearch(groupIds, e.getGroupId()) >= 0) continue;
                putArrayEntryValue(currentLocationEntriesByVs, e.getVsId(), new PathValidator.LocationEntry().setEntryId(e.getGroupId()).setEntryType(MetaType.GROUP).setVsId(e.getVsId()).setPath(e.getPath()).setPriority(e.getPriority()));
            }
        }
    }

    @Override
    public void checkVersionForUpdate(TrafficPolicy target) throws Exception {
        TrafficPolicyDo d = trafficPolicyDao.findById(target.getId(), TrafficPolicyEntity.READSET_FULL);
        if (d == null) {
            throw new ValidationException("Traffic policy that you try to update does not exist.");
        }
        if (d.getVersion() > target.getVersion()) {
            throw new ValidationException("Newer version is detected.");
        }
        if (d.getVersion() != target.getVersion()) {
            throw new ValidationException("Incompatible version.");
        }
    }

    @Override
    public void removable(Long targetId) throws Exception {
        TrafficPolicyDo d = trafficPolicyDao.findById(targetId, TrafficPolicyEntity.READSET_FULL);
        if (d != null && d.getActiveVersion() > 0) {
            throw new ValidationException("Traffic policy that you try to delete is still active.");
        }
    }

}
