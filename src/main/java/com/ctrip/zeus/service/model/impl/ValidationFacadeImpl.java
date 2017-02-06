package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.PathRewriteParser;
import com.ctrip.zeus.service.model.PathValidator;
import com.ctrip.zeus.service.model.ValidationFacade;
import com.ctrip.zeus.service.model.common.ErrorType;
import com.ctrip.zeus.service.model.common.LocationEntry;
import com.ctrip.zeus.service.model.common.MetaType;
import com.ctrip.zeus.service.model.common.ValidationContext;
import com.ctrip.zeus.service.model.handler.VsEntryFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2017/2/4.
 */
@Service("validationFacade")
public class ValidationFacadeImpl implements ValidationFacade {
    @Resource
    private VsEntryFactory vsEntryFactory;
    @Resource
    private PathValidator pathValidator;

    @Override
    public void validateGroup(Group group, ValidationContext context) {
        if (group.getName() == null || group.getName().isEmpty()
                || group.getAppId() == null || group.getAppId().isEmpty()) {
            context.error(group.getId(), MetaType.GROUP, ErrorType.FIELD_VALIDATION, "Field `name` and `app-id` are not allowed empty.");
        }
        if (!context.shouldProceed()) return;

        if (group.getHealthCheck() != null
                && (group.getHealthCheck().getUri() == null || group.getHealthCheck().getUri().isEmpty())) {
            context.error(group.getId(), MetaType.GROUP, ErrorType.FIELD_VALIDATION, "Field `health-check` is missing `uri` value.");
        }
        if (!context.shouldProceed()) return;

        try {
            validateMembers(group.getGroupServers());
        } catch (ValidationException e) {
            context.error(group.getId(), MetaType.GROUP, ErrorType.FIELD_VALIDATION, e.getMessage());
            return;
        }

        Map<Long, GroupVirtualServer> groupOnVses;
        try {
            groupOnVses = validateGroupOnVses(group.getGroupVirtualServers());
            if (groupOnVses.size() == 0) return;

            List<LocationEntry> policyEntries = null;
            try {
                policyEntries = vsEntryFactory.getGroupRelatedPolicyEntries(group.getId());
            } catch (Exception e) {
            }
            validatePolicyRestriction(groupOnVses, policyEntries);
        } catch (ValidationException e) {
            context.error(group.getId(), MetaType.GROUP, ErrorType.DEPENDENCY_VALIDATION, e.getMessage());
            return;
        }

        try {
            Map<Long, List<LocationEntry>> locationEntries;
            if (group.getId() == null) {
                locationEntries = vsEntryFactory.buildLocationEntriesByVs(groupOnVses.keySet().toArray(new Long[groupOnVses.size()]), null, null);
            } else {
                locationEntries = vsEntryFactory.buildLocationEntriesByVs(groupOnVses.keySet().toArray(new Long[groupOnVses.size()]), new Long[]{group.getId()}, null);
            }
            for (Map.Entry<Long, GroupVirtualServer> e : groupOnVses.entrySet()) {
                List<LocationEntry> v = locationEntries.get(e.getKey());
                if (v == null) {
                    v = new ArrayList<>();
                    locationEntries.put(e.getKey(), v);
                }
                v.add(new LocationEntry().setEntryId(group.getId()).setEntryType(MetaType.GROUP).setVsId(e.getKey()).setPath(e.getValue().getPath()).setPriority(e.getValue().getPriority()));
            }
            for (List<LocationEntry> entries : locationEntries.values()) {
                pathValidator.checkOverlapRestricition(entries, context);
            }
        } catch (ValidationException e) {
            context.error(group.getId(), MetaType.GROUP, ErrorType.DEPENDENCY_VALIDATION, e.getMessage());
        }
    }

    private Map<Long, GroupVirtualServer> validateGroupOnVses(List<GroupVirtualServer> groupOnVses) throws ValidationException {
        if (groupOnVses == null || groupOnVses.size() == 0)
            throw new ValidationException("Group is missing `group-virtual-server` field.");

        Map<Long, GroupVirtualServer> result = new HashMap<>();
        for (int i = 0; i < groupOnVses.size(); i++) {
            GroupVirtualServer e = groupOnVses.get(i);
            if (e.getRewrite() != null && !e.getRewrite().isEmpty()) {
                if (!PathRewriteParser.validate(e.getRewrite())) {
                    throw new ValidationException("Invalid `rewrite` field value. \"rewrite\" : " + e.getRewrite() + ".");
                }
            }
            GroupVirtualServer prev = result.put(e.getVirtualServer().getId(), e);
            if (prev != null) {
                throw new ValidationException("Group can have and only have one combination to the same virtual-server. \"vs-id\" : " + e.getVirtualServer().getId() + ".");
            }
        }
        return result;
    }

    private void validatePolicyRestriction(Map<Long, GroupVirtualServer> groupOnVses, List<LocationEntry> policyEntries) throws ValidationException {
        if (policyEntries == null || policyEntries.size() == 0) return;

        for (LocationEntry e : policyEntries) {
            Long vsId = e.getVsId();
            GroupVirtualServer gvs = groupOnVses.get(vsId);
            if (gvs == null) {
                throw new ValidationException("Group is missing combination on vs " + vsId + " referring its traffic policy " + e.getEntryId() + ".");
            }
            if (gvs.getPriority() == null) {
                throw new ValidationException("Group with policies requires priority to be explicitly set.");
            }
            if (gvs.getPriority() > e.getPriority()) {
                throw new ValidationException("Group has higher `priority` than its traffic policy " + e.getEntryId() + " on vs " + vsId + ".");
            }
            if (!gvs.getPath().equals(e.getPath())) {
                throw new ValidationException("Group and its traffic policy " + e.getEntryId() + " do not have the same `path` value on vs " + vsId + ".");
            }
        }
    }

    private void validateMembers(List<GroupServer> servers) throws ValidationException {
        Set<byte[]> unique = new HashSet<>();
        for (GroupServer s : servers) {
            if (s.getIp() == null || s.getIp().isEmpty() || s.getPort() == null) {
                throw new ValidationException("Group server ip and port cannot be null.");
            }
            byte[] ip = s.getIp().getBytes();
            byte[] v = new byte[ip.length + 2];
            for (int i = 2; i < v.length; i++) {
                v[i] = ip[i - 2];
            }
            v[1] = ':';
            v[0] = s.getPort().byteValue();
            if (!unique.add(v)) {
                throw new ValidationException("Duplicate combination of ip and port " + s.getIp() + ":" + s.getPort() + " is found in group server list.");
            }
        }
    }

    @Override
    public void validatePolicy(TrafficPolicy policy, ValidationContext context) {
        if (policy.getName() == null) {
            context.error(policy.getId(), MetaType.TRAFFIC_POLICY, ErrorType.FIELD_VALIDATION, "Field `name` is empty.");
        }

        Long[] controlIds;
        try {
            controlIds = validatePolicyControl(policy);
        } catch (ValidationException e) {
            context.error(policy.getId(), MetaType.TRAFFIC_POLICY, ErrorType.FIELD_VALIDATION, e.getMessage());
            return;
        }
        Map<Long, PolicyVirtualServer> policyOnVses;
        try {
            Map<Long, List<LocationEntry>> groupEntries = vsEntryFactory.getGroupEntriesByVs(controlIds);
            Map<Long, List<LocationEntry>> groupRelatedPolicyEntries = vsEntryFactory.getGroupRelatedPolicyEntriesByVs(controlIds);
            policyOnVses = validatePolicyOnVses(policy.getId(), controlIds, groupEntries, policy.getPolicyVirtualServers(), groupRelatedPolicyEntries);
        } catch (Exception e) {
            context.error(policy.getId(), MetaType.TRAFFIC_POLICY, ErrorType.DEPENDENCY_VALIDATION, e.getMessage());
            return;
        }

        try {
            Map<Long, List<LocationEntry>> locationEntries;
            if (policy.getId() == null) {
                locationEntries = vsEntryFactory.buildLocationEntriesByVs(policyOnVses.keySet().toArray(new Long[policyOnVses.size()]), controlIds, null);
            } else {
                locationEntries = vsEntryFactory.buildLocationEntriesByVs(policyOnVses.keySet().toArray(new Long[policyOnVses.size()]), controlIds, new Long[]{policy.getId()});
            }
            for (Map.Entry<Long, PolicyVirtualServer> e : policyOnVses.entrySet()) {
                List<LocationEntry> v = locationEntries.get(e.getKey());
                if (v == null) {
                    v = new ArrayList<>();
                    locationEntries.put(e.getKey(), v);
                }
                v.add(new LocationEntry().setEntryId(policy.getId()).setEntryType(MetaType.GROUP).setVsId(e.getKey()).setPath(e.getValue().getPath()).setPriority(e.getValue().getPriority()));
            }
            for (List<LocationEntry> entries : locationEntries.values()) {
                pathValidator.checkOverlapRestricition(entries, context);
            }
        } catch (ValidationException e) {
            context.error(policy.getId(), MetaType.GROUP, ErrorType.DEPENDENCY_VALIDATION, e.getMessage());
        }
    }

    private Long[] validatePolicyControl(TrafficPolicy policy) throws ValidationException {
        Long[] groupIds = new Long[policy.getControls().size()];
        for (int i = 0; i < policy.getControls().size(); i++) {
            groupIds[i] = policy.getControls().get(i).getGroup().getId();
        }
        Arrays.sort(groupIds);
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
        return groupIds;
    }

    private Map<Long, PolicyVirtualServer> validatePolicyOnVses(Long policyId, Long[] controlIds, Map<Long, List<LocationEntry>> groupEntries, List<PolicyVirtualServer> policyOnVses, Map<Long, List<LocationEntry>> groupRelatedPolicyEntries) throws ValidationException {
        return validatePolicyOnVses(policyId, controlIds, groupEntries, policyOnVses, groupRelatedPolicyEntries, null);
    }

    private Map<Long, PolicyVirtualServer> validatePolicyOnVses(Long policyId, Long[] controlIds,
                                                                Map<Long, List<LocationEntry>> groupEntries, List<PolicyVirtualServer> policyOnVses,
                                                                Map<Long, List<LocationEntry>> groupRelatedPolicyEntries,
                                                                ValidationContext context) throws ValidationException {
        ValidationContext _context = context == null ? new ValidationContext() : context;

        Map<Long, PolicyVirtualServer> result = new HashMap<>();
        int i = 0;
        int[] visited = new int[controlIds.length];
        for (PolicyVirtualServer e : policyOnVses) {
            Long vsId = e.getVirtualServer().getId();
            List<LocationEntry> another = groupRelatedPolicyEntries.get(vsId);
            if (another != null && another.size() > 0) {
                for (LocationEntry ee : another) {
                    if (!ee.getEntryId().equals(policyId)) {
                        String error = "Some other traffic policies have occupied traffic-controls on vs " + vsId + ".";
                        _context.error(policyId, MetaType.TRAFFIC_POLICY, ErrorType.DEPENDENCY_VALIDATION, error);
                        _context.error(ee.getEntryId(), MetaType.TRAFFIC_POLICY, ErrorType.DEPENDENCY_VALIDATION, error);
                        if (context == null) throw new ValidationException(error);
                    }
                }
            }
            i++;
            for (LocationEntry ee : groupEntries.get(vsId)) {
                int j = Arrays.binarySearch(controlIds, ee.getEntryId());
                if (j < 0) continue;
                visited[j] = i;
                if (e.getPriority() < ee.getPriority()) {
                    String error = "Traffic policy has lower `priority` than its control item " + controlIds[j] + " on vs " + vsId + ".";
                    _context.error(policyId, MetaType.TRAFFIC_POLICY, ErrorType.DEPENDENCY_VALIDATION, error);
                    _context.error(controlIds[j], MetaType.GROUP, ErrorType.DEPENDENCY_VALIDATION, error);
                    if (context == null) throw new ValidationException(error);
                }
                if (!e.getPath().equals(ee.getPath())) {
                    String error = "Traffic policy and its control item " + controlIds[j] + " does not have the same `path` value on vs " + vsId + ".";
                    _context.error(policyId, MetaType.TRAFFIC_POLICY, ErrorType.DEPENDENCY_VALIDATION, error);
                    _context.error(controlIds[j], MetaType.GROUP, ErrorType.DEPENDENCY_VALIDATION, error);
                    if (context == null) throw new ValidationException(error);
                }
            }
            for (int k = 0; k < visited.length; k++) {
                if (visited[k] != i) {
                    String error = "Group " + controlIds[k] + " is missing combination on vs " + vsId + ".";
                    _context.error(policyId, MetaType.TRAFFIC_POLICY, ErrorType.DEPENDENCY_VALIDATION, error);
                    _context.error(controlIds[k], MetaType.GROUP, ErrorType.DEPENDENCY_VALIDATION, error);
                    if (context == null) throw new ValidationException(error);
                }
            }
            PolicyVirtualServer prev = result.put(vsId, e);
            if (prev != null) {
                String error = "Traffic policy can have and only have one combination to the same virtual-server. \"vs-id\" : " + e.getVirtualServer().getId() + ".";
                _context.error(policyId, MetaType.TRAFFIC_POLICY, ErrorType.DEPENDENCY_VALIDATION, error);
                if (context == null) throw new ValidationException(error);
            }
        }
        return result;
    }

    @Override
    public void validateVs(VirtualServer vs, ValidationContext context) {

    }

    @Override
    public void validateSlb(Slb slb, ValidationContext context) {

    }

    @Override
    public void validateEntriesOnVs(Long vsId, List<Group> groups, List<TrafficPolicy> policies, ValidationContext context) {
        // validate groups
        for (Group group : groups) {
            GroupVirtualServer target = null;
            for (GroupVirtualServer e : group.getGroupVirtualServers()) {
                if (e.getVirtualServer().getId().equals(vsId)) {
                    target = e;
                    break;
                }
            }
            if (target == null) {
                context.error(group.getId(), MetaType.GROUP, ErrorType.DEPENDENCY_VALIDATION, "Cannot find target on vs " + vsId + ".");
            } else {
                List<GroupVirtualServer> gvsToBeChecked = new ArrayList<>(1);
                gvsToBeChecked.add(target);
                try {
                    validateGroupOnVses(gvsToBeChecked);
                } catch (ValidationException e) {
                    context.error(group.getId(), MetaType.GROUP, ErrorType.FIELD_VALIDATION, e.getMessage());
                }
            }
        }

        // validate policies
        Set<Long> escapedGroupIds = new HashSet<>();
        Map<Long, LocationEntry> policyEntriesByGroup = vsEntryFactory.mapPolicyEntriesByGroup(vsId, policies, context);
        Map<Long, List<LocationEntry>> groupEntriesByVs = new HashMap<>(1);
        groupEntriesByVs.put(vsId, vsEntryFactory.filterGroupEntriesByVs(vsId, groups, context));
        for (TrafficPolicy policy : policies) {
            Long[] controlIds;
            try {
                controlIds = validatePolicyControl(policy);
            } catch (ValidationException e) {
                context.error(policy.getId(), MetaType.TRAFFIC_POLICY, ErrorType.FIELD_VALIDATION, e.getMessage());
                break;
            }
            PolicyVirtualServer target = null;
            for (PolicyVirtualServer e : policy.getPolicyVirtualServers()) {
                if (e.getVirtualServer().getId().equals(vsId)) {
                    target = e;
                    break;
                }
            }
            if (target == null) {
                context.error(policy.getId(), MetaType.TRAFFIC_POLICY, ErrorType.DEPENDENCY_VALIDATION, "Cannot find target on vs " + vsId + ".");
            } else {
                Map<Long, List<LocationEntry>> _groupRelatedPolicyEntry = new HashMap<>();
                for (Long controlId : controlIds) {
                    LocationEntry _p = policyEntriesByGroup.get(controlId);
                    if (_p != null) {
                        List<LocationEntry> v = _groupRelatedPolicyEntry.get(vsId);
                        if (v == null) {
                            v = new ArrayList<>();
                            _groupRelatedPolicyEntry.put(vsId, v);
                        }
                        v.add(_p);
                    }
                }
                List<PolicyVirtualServer> _pvsToBeChecked = new ArrayList<>(1);
                _pvsToBeChecked.add(target);
                try {
                    validatePolicyOnVses(policy.getId(), controlIds, groupEntriesByVs, _pvsToBeChecked, _groupRelatedPolicyEntry, context);
                } catch (ValidationException e) {
                    context.error(policy.getId(), MetaType.TRAFFIC_POLICY, ErrorType.DEPENDENCY_VALIDATION, e.getMessage());
                }
            }
            for (Long c : controlIds) {
                escapedGroupIds.add(c);
            }
        }

        // validate paths
        Set<LocationEntry> locationEntries = new HashSet<>();
        locationEntries.addAll(policyEntriesByGroup.values());
        for (LocationEntry e : groupEntriesByVs.get(vsId)) {
            if (escapedGroupIds.contains(e.getEntryId())) continue;
            locationEntries.add(e);
        }
        pathValidator.checkOverlapRestricition(new ArrayList<>(locationEntries), context);
    }
}