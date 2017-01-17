package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.PolicyVirtualServer;
import com.ctrip.zeus.model.entity.TrafficPolicy;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.PathValidator;
import com.ctrip.zeus.service.model.common.MetaType;
import com.ctrip.zeus.service.model.handler.TrafficPolicyValidator;
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

        validatePolicyControls(target, groupIds, vsIds);

        Map<Long, List<PathValidator.LocationEntry>> currentLocationEntriesByVs = new HashMap<>();
        compareAndBuildCurrentLocationEntries(target.getId(), groupIds, vsIds, escapePathValidation ? null : currentLocationEntriesByVs);


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

    private void compareAndBuildCurrentLocationEntries(Long trafficPolicyId, Long[] groupIds, Long[] vsIds, Map<Long, List<PathValidator.LocationEntry>> currentLocationEntriesByVs) throws DalException, ValidationException {
        // Check if traffic-policy is unique for every group-vs pair
        List<Long> policies = new ArrayList<>();
        for (RTrafficPolicyGroupDo e : rTrafficPolicyGroupDao.findByGroupsAndPolicyVersion(groupIds, RTrafficPolicyGroupEntity.READSET_FULL)) {
            if (trafficPolicyId != null && e.getPolicyId() == trafficPolicyId) continue;
            policies.add(e.getPolicyId());
        }

        for (RTrafficPolicyVsDo e : rTrafficPolicyVsDao.findByVsesAndPolicyVersion(vsIds, RTrafficPolicyVsEntity.READSET_FULL)) {
            if (trafficPolicyId != null && e.getPolicyId() == trafficPolicyId) continue;
            if (policies.indexOf(e.getPolicyId()) >= 0) {
                throw new ValidationException("Another traffic policy " + e.getId() + " has occupied one of the traffic-controls on vs " + e.getVsId() + ".");
            }
            if (currentLocationEntriesByVs != null) {
                List<PathValidator.LocationEntry> v = currentLocationEntriesByVs.get(e.getVsId());
                if (v == null) {
                    v = new ArrayList<>();
                    currentLocationEntriesByVs.put(e.getVsId(), v);
                }
                v.add(new PathValidator.LocationEntry().setEntryId(e.getPolicyId()).setEntryType(MetaType.TRAFFIC_POLICY).setVsId(e.getVsId()).setPath(e.getPath()).setPriority(e.getPriority()));
            }
        }

        for (RelGroupVsDo e : rGroupVsDao.findByVsesAndGroupOfflineVersion(vsIds, RGroupVsEntity.READSET_FULL)) {
            if (Arrays.binarySearch(groupIds, e.getGroupId()) >= 0) continue;

            if (currentLocationEntriesByVs != null) {
                List<PathValidator.LocationEntry> v = currentLocationEntriesByVs.get(e.getVsId());
                if (v == null) {
                    v = new ArrayList<>();
                    currentLocationEntriesByVs.put(e.getVsId(), v);
                }
                v.add(new PathValidator.LocationEntry().setEntryId(e.getGroupId()).setEntryType(MetaType.GROUP).setVsId(e.getVsId()).setPath(e.getPath()).setPriority(e.getPriority()));
            }
        }
    }

    private void validatePolicyControls(TrafficPolicy target, Long[] groupIds, Long[] vsIds) throws DalException, ValidationException {
        Long prev = groupIds[0];
        for (int i = 1; i < groupIds.length; i++) {
            if (prev.equals(groupIds[i])) {
                throw new ValidationException("Traffic policy that you tries to create/modify declares the same group " + prev + " more than once.");
            }
            prev = groupIds[i];
        }
        if (groupIds.length <= 1) {
            throw new ValidationException("Traffic policy that you tries to create/modify does not have enough traffic-controls.");
        }

        prev = vsIds[0];
        for (int i = 1; i < vsIds.length; i++) {
            if (prev.equals(vsIds[i])) {
                throw new ValidationException("Traffic policy that you tries to create/modify declares the same vs " + prev + " more than once.");
            }
            prev = vsIds[i];
        }

        int[] versions = new int[groupIds.length];
        RelGroupVsDo[][] combinations = new RelGroupVsDo[groupIds.length][vsIds.length];
        for (RelGroupVsDo e : rGroupVsDao.findAllByGroups(groupIds, RGroupVsEntity.READSET_FULL)) {
            int i = Arrays.binarySearch(groupIds, e.getGroupId());
            int j = Arrays.binarySearch(vsIds, e.getVsId());
            if (i >= 0 && j >= 0) {
                if (versions[i] <= e.getGroupVersion()) {
                    versions[i] = e.getGroupVersion();
                    combinations[i][j] = e;
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

    @Override
    public void checkVersionForUpdate(TrafficPolicy target) throws Exception {
        TrafficPolicyDo d = trafficPolicyDao.findById(target.getId(), TrafficPolicyEntity.READSET_FULL);
        if (d == null) {
            throw new ValidationException("Traffic policy that you tries to update does not exist.");
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
            throw new ValidationException("Traffic policy that you tried to delete is still active.");
        }
    }

}
