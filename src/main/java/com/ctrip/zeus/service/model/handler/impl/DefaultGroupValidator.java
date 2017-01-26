package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.PathRewriteParser;
import com.ctrip.zeus.service.model.PathValidator;
import com.ctrip.zeus.service.model.common.LocationEntry;
import com.ctrip.zeus.service.model.common.MetaType;
import com.ctrip.zeus.service.model.handler.GroupServerValidator;
import com.ctrip.zeus.service.model.handler.GroupValidator;
import com.google.common.base.Joiner;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2015/6/29.
 */
@Component("groupModelValidator")
public class DefaultGroupValidator implements GroupValidator {
    @Resource
    private SlbVirtualServerDao slbVirtualServerDao;
    @Resource
    private GroupServerValidator groupServerModelValidator;
    @Resource
    private PathValidator pathValidator;
    @Resource
    private RTrafficPolicyVsDao rTrafficPolicyVsDao;
    @Resource
    private RTrafficPolicyGroupDao rTrafficPolicyGroupDao;
    @Resource
    private RGroupVsDao rGroupVsDao;
    @Resource
    private RGroupVgDao rGroupVgDao;
    @Resource
    private RGroupStatusDao rGroupStatusDao;
    @Resource
    private GroupDao groupDao;

    @Override
    public boolean exists(Long targetId) throws Exception {
        return groupDao.findById(targetId, GroupEntity.READSET_FULL) != null
                && rGroupVgDao.findByGroup(targetId, RGroupVgEntity.READSET_FULL) == null;
    }

    @Override
    public void validate(Group target) throws Exception {
        validate(target, false);
    }

    @Override
    public void validateForActivate(Group[] toBeActivatedItems, boolean escapePathValidation) throws Exception {
        Long[] groupIds = new Long[toBeActivatedItems.length];
        for (int i = 0; i < toBeActivatedItems.length; i++) {
            groupIds[i] = toBeActivatedItems[i].getId();
        }

        Map<Long, List<RTrafficPolicyVsDo>> policyListByGroupId = new HashMap<>();
        List<RTrafficPolicyGroupDo> groupRelatedPolicies = rTrafficPolicyGroupDao.findByGroupsAndPolicyActiveVersion(groupIds, RTrafficPolicyGroupEntity.READSET_FULL);
        Integer[] hashes = new Integer[groupRelatedPolicies.size()];
        for (int i = 0; i < groupRelatedPolicies.size(); i++) {
            hashes[i] = groupRelatedPolicies.get(i).getHash();
        }
        for (RTrafficPolicyVsDo e : rTrafficPolicyVsDao.findAllByHash(hashes, RTrafficPolicyVsEntity.READSET_FULL)) {
            int i = Arrays.binarySearch(hashes, e.getHash());
            RTrafficPolicyGroupDo cmp = groupRelatedPolicies.get(i);
            if (e.getPolicyId() == cmp.getPolicyId() && e.getPolicyVersion() == cmp.getPolicyVersion()) {
                putArrayEntryValue(policyListByGroupId, cmp.getGroupId(), e);
            }
        }

        Map<Long, List<RTrafficPolicyVsDo>> pvsListByVsId = new HashMap<>();
        Map<Long, List<RelGroupVsDo>> gvsListByVsId = new HashMap<>();

        if (escapePathValidation) {
            for (Group e : toBeActivatedItems) {
                Long[] v = new Long[e.getGroupVirtualServers().size()];
                for (int i = 0; i < e.getGroupVirtualServers().size(); i++) {
                    v[i] = e.getGroupVirtualServers().get(i).getVirtualServer().getId();
                }
                compareAndBuildCurrentLocationEntries(e, v, policyListByGroupId, pvsListByVsId, gvsListByVsId, null);
                validatePathPriority(e, true, null);
            }
            return;
        }

        Set<Long> vsLookup = new HashSet<>();
        for (Group i : toBeActivatedItems) {
            for (GroupVirtualServer e : i.getGroupVirtualServers()) {
                vsLookup.add(e.getVirtualServer().getId());
            }
        }
        Long[] vsIds = vsLookup.toArray(new Long[vsLookup.size()]);
        for (RTrafficPolicyVsDo e : rTrafficPolicyVsDao.findByVsesAndPolicyActiveVersion(vsIds, RTrafficPolicyVsEntity.READSET_FULL)) {
            putArrayEntryValue(pvsListByVsId, e.getVsId(), e);
        }
        for (RelGroupVsDo e : rGroupVsDao.findByVsesAndGroupOnlineVersion(vsIds, RGroupVsEntity.READSET_FULL)) {
            if (Arrays.binarySearch(groupIds, e.getGroupId()) < 0) {
                putArrayEntryValue(gvsListByVsId, e.getVsId(), e);
            }
        }
        for (RelGroupVsDo e : rGroupVsDao.findAllByGroups(groupIds, RGroupVsEntity.READSET_FULL)) {
            int i = Arrays.binarySearch(groupIds, e.getGroupId());
            if (toBeActivatedItems[i].getId().equals(e.getGroupId()) && toBeActivatedItems[i].getVersion().equals(e.getGroupVersion())) {
                putArrayEntryValue(gvsListByVsId, e.getVsId(), e);
            }
        }

        for (Group e : toBeActivatedItems) {
            Long[] v = new Long[e.getGroupVirtualServers().size()];
            for (int i = 0; i < e.getGroupVirtualServers().size(); i++) {
                v[i] = e.getGroupVirtualServers().get(i).getVirtualServer().getId();
            }

            Map<Long, List<LocationEntry>> currentLocationEntriesByVs = new HashMap<>();
            compareAndBuildCurrentLocationEntries(e, v, policyListByGroupId, pvsListByVsId, gvsListByVsId, currentLocationEntriesByVs);

            validatePathPriority(e, false, currentLocationEntriesByVs);
        }
    }

    @Override
    public void validateForDeactivate(Long[] toBeDeactivatedItems) throws Exception {
        for (RTrafficPolicyGroupDo e : rTrafficPolicyGroupDao.findByGroupsAndPolicyActiveVersion(toBeDeactivatedItems, RTrafficPolicyGroupEntity.READSET_FULL)) {
            throw new ValidationException("Group that you try to deactivate is hold by an online traffic policy " + e.getPolicyId() + ".");
        }
    }

    @Override
    public void checkVersionForUpdate(Group target) throws Exception {
        RelGroupStatusDo check = rGroupStatusDao.findByGroup(target.getId(), RGroupStatusEntity.READSET_FULL);
        if (check == null) {
            throw new ValidationException("Group that you try to update does not exist.");
        }
        if (check.getOfflineVersion() > target.getVersion()) {
            throw new ValidationException("Newer version is detected.");
        }
        if (check.getOfflineVersion() != target.getVersion()) {
            throw new ValidationException("Incompatible version.");
        }
    }

    @Override
    public void removable(Long targetId) throws Exception {
        RelGroupStatusDo check = rGroupStatusDao.findByGroup(targetId, RGroupStatusEntity.READSET_FULL);
        if (check == null) return;

        if (check.getOnlineVersion() != 0) {
            throw new ValidationException("Group that you tried to delete is still active.");
        }
        if (rTrafficPolicyGroupDao.findByGroup(targetId, RTrafficPolicyGroupEntity.READSET_FULL).size() > 0) {
            throw new ValidationException("Group that you tried to delete has one or more traffic policy dependency.");
        }
    }

    @Override
    public void validate(Group target, boolean escapePathValidation) throws Exception {
        if (target.getName() == null || target.getName().isEmpty()
                || target.getAppId() == null || target.getAppId().isEmpty()) {
            throw new ValidationException("Field `name` and `app-id` are not allowed empty.");
        }
        if (target.getHealthCheck() != null
                && (target.getHealthCheck().getUri() == null || target.getHealthCheck().getUri().isEmpty())) {
            throw new ValidationException("Field `health-check` is missing `uri` value.");
        }
        validateGroupVirtualServers(target, escapePathValidation);
        validateGroupServers(target.getGroupServers());
    }

    @Override
    public void validateForMerge(Long[] toBeMergedItems, Long vsId, Map<Long, Group> groupRef, Map<Long, TrafficPolicy> policyRef, boolean escapePathValidation) throws Exception {
        Map<Long, Long> policiesByGroupId = new HashMap<>();
        Map<Long, LocationEntry> policyEntriesById = new HashMap<>();
        for (TrafficPolicy p : policyRef.values()) {
            for (PolicyVirtualServer pvs : p.getPolicyVirtualServers()) {
                if (pvs.getVirtualServer().getId().equals(vsId)) {
                    policyEntriesById.put(p.getId(), new LocationEntry().setEntryId(p.getId()).setEntryType(MetaType.TRAFFIC_POLICY).setVsId(vsId).setPath(pvs.getPath()).setPriority(pvs.getPriority()));
                }
            }
            for (TrafficControl c : p.getControls()) {
                policiesByGroupId.put(c.getGroup().getId(), p.getId());
            }
        }
        Set<Long> groupAsPolicies = new HashSet<>(policiesByGroupId.keySet());
        groupAsPolicies.removeAll(groupRef.keySet());
        if (groupAsPolicies.size() > 0) {
            throw new ValidationException("Group " + Joiner.on(",").join(groupAsPolicies) + " is missing combination on vs " + vsId + ".");
        }
        for (Long i : toBeMergedItems) {
            Long id = policiesByGroupId.get(i);
            LocationEntry e = policyEntriesById.get(id);
            if (e == null) continue;

            Group g = groupRef.get(i);
            for (GroupVirtualServer gvs : g.getGroupVirtualServers()) {
                if (gvs.getVirtualServer().getId().equals(vsId)) {
                    if (e.getPriority() < gvs.getPriority()) {
                        throw new ValidationException("Group has higher `priority` than its traffic policy " + e.getEntryId() + " on vs " + vsId + ".");
                    }
                    if (!e.getPath().equals(gvs.getPath())) {
                        throw new ValidationException("Group has different `path` from its traffic policy " + e.getEntryId() + " on vs " + vsId + ".");
                    }
                }
            }
        }

        Map<Long, List<LocationEntry>> locationEntries = new HashMap<>();
        ArrayList<LocationEntry> values = new ArrayList<>(policyEntriesById.values());
        locationEntries.put(vsId, values);
        for (Group g : groupRef.values()) {
            if (policiesByGroupId.containsKey(g.getId())) continue;
            for (GroupVirtualServer gvs : g.getGroupVirtualServers()) {
                if (gvs.getVirtualServer().getId().equals(vsId)) {
                    values.add(new LocationEntry().setEntryId(g.getId()).setEntryType(MetaType.GROUP).setVsId(vsId).setPath(gvs.getPath()).setPriority(gvs.getPriority()));
                }
            }
        }

        for (Long i : toBeMergedItems) {
            if (policiesByGroupId.containsKey(i)) continue;
            validatePathPriority(groupRef.get(i), escapePathValidation, locationEntries);
        }
    }

    @Override
    public void validateGroupVirtualServers(Group target, boolean escapePathValidation) throws Exception {
        if (target.getGroupVirtualServers() == null || target.getGroupVirtualServers().size() == 0)
            throw new ValidationException("Group is missing `group-virtual-server` field.");

        Long[] vsIds = new Long[target.getGroupVirtualServers().size()];
        for (int i = 0; i < target.getGroupVirtualServers().size(); i++) {
            GroupVirtualServer e = target.getGroupVirtualServers().get(i);
            vsIds[i] = e.getVirtualServer().getId();
            if (e.getRewrite() != null && !e.getRewrite().isEmpty()) {
                if (!PathRewriteParser.validate(e.getRewrite())) {
                    throw new ValidationException("Invalid `rewrite` field value. \"rewrite\" : " + e.getRewrite() + ".");
                }
            }
        }
        Arrays.sort(vsIds);
        Long prev = vsIds[0];
        for (int i = 1; i < vsIds.length; i++) {
            if (prev.equals(vsIds[i])) {
                throw new ValidationException("Group can have and only have one combination to the same virtual-server. \"vs-id\" : " + vsIds[i] + ".");
            }
            prev = vsIds[i];
        }

        if (slbVirtualServerDao.findAllByIds(vsIds, SlbVirtualServerEntity.READSET_IDONLY).size() != vsIds.length) {
            throw new ValidationException("Field `group-virtual-server` is requesting combination to a non-existing virtual-server.");
        }

        Map<Long, List<RTrafficPolicyVsDo>> policyListByGroupId = new HashMap<>();
        if (target.getId() != null) {
            Long groupId = target.getId();
            List<RTrafficPolicyGroupDo> groupRelatedPolicies = rTrafficPolicyGroupDao.findByGroupsAndPolicyVersion(new Long[]{target.getId()}, RTrafficPolicyGroupEntity.READSET_FULL);
            Integer[] hashes = new Integer[groupRelatedPolicies.size()];
            for (int i = 0; i < groupRelatedPolicies.size(); i++) {
                hashes[i] = groupRelatedPolicies.get(i).getHash();
            }
            for (RTrafficPolicyVsDo e : rTrafficPolicyVsDao.findAllByHash(hashes, RTrafficPolicyVsEntity.READSET_FULL)) {
                int i = Arrays.binarySearch(hashes, e.getHash());
                RTrafficPolicyGroupDo cmp = groupRelatedPolicies.get(i);
                if (e.getPolicyId() == cmp.getPolicyId() && e.getPolicyVersion() == cmp.getPolicyVersion()) {
                    putArrayEntryValue(policyListByGroupId, groupId, e);
                }
            }
        }

        Map<Long, List<RTrafficPolicyVsDo>> pvsListByVsId = new HashMap<>();
        Map<Long, List<RelGroupVsDo>> gvsListByVsId = new HashMap<>();

        if (escapePathValidation) {
            compareAndBuildCurrentLocationEntries(target, vsIds, policyListByGroupId, pvsListByVsId, gvsListByVsId, null);
            validatePathPriority(target, escapePathValidation, null);
            return;
        }

        for (RTrafficPolicyVsDo e : rTrafficPolicyVsDao.findByVsesAndPolicyVersion(vsIds, RTrafficPolicyVsEntity.READSET_FULL)) {
            putArrayEntryValue(pvsListByVsId, e.getVsId(), e);
        }
        for (RelGroupVsDo e : rGroupVsDao.findByVsesAndGroupOfflineVersion(vsIds, RGroupVsEntity.READSET_FULL)) {
            putArrayEntryValue(gvsListByVsId, e.getVsId(), e);
        }

        Map<Long, List<LocationEntry>> currentLocationEntriesByVs = new HashMap<>();
        compareAndBuildCurrentLocationEntries(target, vsIds, policyListByGroupId, pvsListByVsId, gvsListByVsId, currentLocationEntriesByVs);

        validatePathPriority(target, escapePathValidation, currentLocationEntriesByVs);
    }

    private void validatePathPriority(Group target, boolean escapePathValidation, Map<Long, List<LocationEntry>> currentLocationEntriesByVs) throws ValidationException {
        // reformat path if validation is processed
        // fulfill priority if auto-reorder("priority" : null) is enabled
        for (GroupVirtualServer e : target.getGroupVirtualServers()) {
            Long vsId = e.getVirtualServer().getId();
            e.setVirtualServer(new VirtualServer().setId(vsId));
            if (escapePathValidation) {
                if (e.getPriority() == null) {
                    throw new ValidationException("Field `priority` cannot be empty if validation is escaped.");
                }
                continue;
            }

            LocationEntry insertEntry = new LocationEntry().setEntryId(target.getId()).setEntryType(MetaType.GROUP).setVsId(vsId).setPath(e.getPath()).setPriority(e.getPriority());
            pathValidator.checkOverlapRestriction(vsId, insertEntry, currentLocationEntriesByVs.get(vsId));
            if (e.getPriority() == null) {
                // auto reorder and reformat
                e.setPriority(insertEntry.getPriority());
                e.setPath(insertEntry.getPath());
            } else {
                // check priority and reformat
                if (!e.getPriority().equals(insertEntry.getPriority())) {
                    throw new ValidationException("Group that you try to create/modify may cause path prefix-overlap problem with other entries on virtual-server " + vsId + ". Recommend priority will be " + insertEntry.getPriority() + ".");
                }
                e.setPath(insertEntry.getPath());
            }
        }
    }

    private void compareAndBuildCurrentLocationEntries(Group target, Long[] vsIds,
                                                       Map<Long, List<RTrafficPolicyVsDo>> policyListByGroupId,
                                                       Map<Long, List<RTrafficPolicyVsDo>> pvsListByVsId,
                                                       Map<Long, List<RelGroupVsDo>> gvsListByVsId,
                                                       Map<Long, List<LocationEntry>> currentLocationEntriesByVs) throws DalException, ValidationException {
        GroupVirtualServer[] gvs = new GroupVirtualServer[vsIds.length];
        for (GroupVirtualServer e : target.getGroupVirtualServers()) {
            int i = Arrays.binarySearch(vsIds, e.getVirtualServer().getId());
            gvs[i] = e;
        }

        List<Long> groupRelatedPolicies = new ArrayList<>();
        if (target.getId() != null && policyListByGroupId.containsKey(target.getId())) {
            for (RTrafficPolicyVsDo e : policyListByGroupId.get(target.getId())) {
                int i = Arrays.binarySearch(vsIds, e.getVsId());
                if (i < 0) {
                    throw new ValidationException("Group is missing combination on vs " + e.getVsId() + " referring its traffic policy " + e.getPolicyId() + ".");
                }
                if (gvs[i] != null && gvs[i].getPriority() > e.getPriority()) {
                    throw new ValidationException("Group has higher `priority` than its traffic policy " + e.getPolicyId() + " on vs " + e.getVsId() + ".");
                }
                if (gvs[i] != null && !gvs[i].getPath().equals(e.getPath())) {
                    throw new ValidationException("Group has different `path` from its traffic policy " + e.getPolicyId() + " on vs " + e.getVsId() + ".");
                }
                groupRelatedPolicies.add(e.getPolicyId());
            }
        }

        if (currentLocationEntriesByVs == null) return;

        for (Long vsId : vsIds) {
            List<RTrafficPolicyVsDo> pvsList = pvsListByVsId.get(vsId);
            if (pvsList != null) {
                for (RTrafficPolicyVsDo e : pvsList) {
                    if (groupRelatedPolicies.indexOf(e.getPolicyId()) < 0) {
                        putArrayEntryValue(currentLocationEntriesByVs, e.getVsId(), new LocationEntry().setEntryId(e.getPolicyId()).setEntryType(MetaType.TRAFFIC_POLICY).setVsId(e.getVsId()).setPath(e.getPath()).setPriority(e.getPriority()));
                    }
                }
            }

            List<RelGroupVsDo> gvsList = gvsListByVsId.get(vsId);
            if (gvsList != null) {
                for (RelGroupVsDo e : gvsList) {
                    if (target.getId() != null && e.getGroupId() == target.getId()) continue;
                    putArrayEntryValue(currentLocationEntriesByVs, e.getVsId(), new LocationEntry().setVsId(e.getVsId()).setEntryId(e.getGroupId()).setPath(e.getPath()).setEntryType(MetaType.GROUP).setPriority(e.getPriority() == 0 ? 1000 : e.getPriority()));
                }
            }
        }// end of vsIds iteration
    }

    private <T> void putArrayEntryValue(Map<Long, List<T>> map, Long key, T e) {
        List<T> v = map.get(key);
        if (v == null) {
            v = new ArrayList<>();
            map.put(key, v);
        }
        v.add(e);
    }

    @Override
    public void validateGroupServers(List<GroupServer> groupServers) throws Exception {
        groupServerModelValidator.validateGroupServers(groupServers);
    }
}