package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.ValidationFacade;
import com.ctrip.zeus.service.model.common.ErrorType;
import com.ctrip.zeus.service.model.common.LocationEntry;
import com.ctrip.zeus.service.model.common.MetaType;
import com.ctrip.zeus.service.model.common.ValidationContext;
import com.ctrip.zeus.service.model.handler.SlbQuery;
import com.ctrip.zeus.service.model.handler.impl.ContentReaders;
import com.ctrip.zeus.service.model.validation.*;
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
    @Resource
    private GroupValidator groupModelValidator;
    @Resource
    private TrafficPolicyValidator trafficPolicyValidator;
    @Resource
    private SlbValidator slbModelValidator;
    @Resource
    private VirtualServerValidator virtualServerModelValidator;
    @Resource
    private SlbQuery slbQuery;
    @Resource
    private ArchiveVsDao archiveVsDao;

    @Override
    public void validateGroup(Group group, ValidationContext context) {
        groupModelValidator.validateFields(group, context);
        if (!context.shouldProceed()) return;

        Map<Long, GroupVirtualServer> groupOnVses;
        try {
            groupOnVses = groupModelValidator.validateGroupOnVses(group.getGroupVirtualServers(), group.isVirtual());
        } catch (ValidationException e) {
            context.error(group.getId(), MetaType.GROUP, ErrorType.FIELD_VALIDATION, e.getMessage());
            return;
        }
        if (groupOnVses.size() == 0) return;

        List<LocationEntry> policyEntries = null;
        try {
            policyEntries = vsEntryFactory.getGroupRelatedPolicyEntries(group.getId());
        } catch (Exception e) {
        }
        try {
            groupModelValidator.validatePolicyRestriction(groupOnVses, policyEntries);
        } catch (ValidationException e) {
            context.error(group.getId(), MetaType.GROUP, ErrorType.DEPENDENCY_VALIDATION, e.getMessage());
            return;
        }

        Map<Long, LocationEntry> groupLocationEntryOnVses = new HashMap<>();
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
                LocationEntry groupEntry = new LocationEntry().setEntryId(group.getId()).setEntryType(MetaType.GROUP).setVsId(e.getKey()).setPath(e.getValue().getPath()).setPriority(e.getValue().getPriority());
                v.add(groupEntry);
                groupLocationEntryOnVses.put(e.getKey(), groupEntry);
            }
            for (List<LocationEntry> entries : locationEntries.values()) {
                pathValidator.checkOverlapRestricition(entries, context);
            }
            for (Map.Entry<Long, GroupVirtualServer> e : groupOnVses.entrySet()) {
                LocationEntry v = groupLocationEntryOnVses.get(e.getKey());
                if (e.getValue().getPriority() == null) {
                    e.getValue().setPriority(v.getPriority());
                }
            }
        } catch (ValidationException e) {
            context.error(group.getId(), MetaType.GROUP, ErrorType.DEPENDENCY_VALIDATION, e.getMessage());
        }
    }

    @Override
    public void validatePolicy(TrafficPolicy policy, ValidationContext context) {
        trafficPolicyValidator.validateFields(policy, context);
        Long[] controlIds;
        try {
            controlIds = trafficPolicyValidator.validatePolicyControl(policy);
        } catch (ValidationException e) {
            context.error(policy.getId(), MetaType.TRAFFIC_POLICY, ErrorType.FIELD_VALIDATION, e.getMessage());
            return;
        }
        Map<Long, PolicyVirtualServer> policyOnVses;
        try {
            Map<Long, List<LocationEntry>> groupEntries = vsEntryFactory.getGroupEntriesByVs(controlIds);
            Map<Long, List<LocationEntry>> groupRelatedPolicyEntries = vsEntryFactory.getGroupRelatedPolicyEntriesByVs(controlIds);
            policyOnVses = trafficPolicyValidator.validatePolicyOnVses(policy.getId(), controlIds, groupEntries, policy.getPolicyVirtualServers(), groupRelatedPolicyEntries, null);
        } catch (Exception e) {
            context.error(policy.getId(), MetaType.TRAFFIC_POLICY, ErrorType.DEPENDENCY_VALIDATION, e.getMessage());
            return;
        }

        Map<Long, LocationEntry> policyLocationEntryOnVses = new HashMap<>();
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
                LocationEntry policyEntry = new LocationEntry().setEntryId(policy.getId()).setEntryType(MetaType.TRAFFIC_POLICY).setVsId(e.getKey()).setPath(e.getValue().getPath()).setPriority(e.getValue().getPriority());
                v.add(policyEntry);
                policyLocationEntryOnVses.put(e.getKey(), policyEntry);
            }
            for (List<LocationEntry> entries : locationEntries.values()) {
                pathValidator.checkOverlapRestricition(entries, context);
            }
            for (Map.Entry<Long, PolicyVirtualServer> e : policyOnVses.entrySet()) {
                LocationEntry v = policyLocationEntryOnVses.get(e.getKey());
                if (e.getValue().getPriority() == null) {
                    e.getValue().setPriority(v.getPriority());
                }
            }
        } catch (ValidationException e) {
            context.error(policy.getId(), MetaType.TRAFFIC_POLICY, ErrorType.DEPENDENCY_VALIDATION, e.getMessage());
        }
    }

    @Override
    public void validateVs(VirtualServer vs, ValidationContext context) {
        try {
            virtualServerModelValidator.validateFields(vs);
        } catch (ValidationException e) {
            context.error(vs.getId(), MetaType.VS, ErrorType.FIELD_VALIDATION, e.getMessage());
            return;
        }
        Long[] relatedSlbIds = vs.getSlbIds().toArray(new Long[vs.getSlbIds().size()]);
        if (!slbModelValidator.exists(relatedSlbIds)) {
            context.error(vs.getId(), MetaType.VS, ErrorType.DEPENDENCY_VALIDATION, "Virtual server contains non-existent slb.");
            return;
        }
        Map<Long, List<VirtualServer>> vsesBySlb = new HashMap<>();
        try {
            for (MetaVsArchiveDo e : archiveVsDao.findAllBySlbsAndVsOfflineVersion(relatedSlbIds, ArchiveVsEntity.READSET_FULL)) {
                if (vs.getId() != null && vs.getId().equals(e.getVsId())) continue;

                VirtualServer value = ContentReaders.readVirtualServerContent(e.getContent());
                if (value == null) continue;
                for (Long slbId : value.getSlbIds()) {
                    List<VirtualServer> v = vsesBySlb.get(slbId);
                    if (v == null) {
                        v = new ArrayList<>();
                        vsesBySlb.put(slbId, v);
                    }
                    v.add(value);
                }
            }
        } catch (Exception e) {
        }
        for (Long slbId : vs.getSlbIds()) {
            List<VirtualServer> v = vsesBySlb.get(slbId);
            if (v == null) {
                v = new ArrayList<>();
                vsesBySlb.put(slbId, v);
            }
            v.add(vs);
        }
        for (Map.Entry<Long, List<VirtualServer>> e : vsesBySlb.entrySet()) {
            virtualServerModelValidator.validateDomains(e.getKey(), e.getValue(), context);
        }
    }

    @Override
    public void validateSlb(Slb slb, ValidationContext context) {
        try {
            slbModelValidator.validateFields(slb, context);
        } catch (ValidationException e) {
            context.error(slb.getId(), MetaType.SLB, ErrorType.FIELD_VALIDATION, e.getMessage());
            return;
        }
        Map<Long, List<SlbServer>> serversBySlb = new HashMap<>();
        try {
            serversBySlb = slbQuery.getServersBySlb();
        } catch (Exception e) {
        }
        if (slb.getId() != null) {
            serversBySlb.remove(slb.getId());
        }
        serversBySlb.put(slb.getId(), slb.getSlbServers());
        slbModelValidator.validateSlbServers(serversBySlb, context);
    }

    @Override
    public void validateSlbNodes(Collection<Slb> slbs, ValidationContext context) {
        if (slbs == null) return;
        if (context == null) context = new ValidationContext();

        Map<Long, List<SlbServer>> serverBySlb = new HashMap<>();
        for (Slb slb : slbs) {
            try {
                slbModelValidator.validateFields(slb, context);
            } catch (ValidationException e) {
                context.error(slb.getId(), MetaType.SLB, ErrorType.FIELD_VALIDATION, e.getMessage());
            }
            serverBySlb.put(slb.getId(), slb.getSlbServers());
        }
        slbModelValidator.validateSlbServers(serverBySlb, context);
    }

    @Override
    public void validateVsesOnSlb(Long slbId, Collection<VirtualServer> vses, ValidationContext context) {
        if (vses == null) return;
        if (context == null) context = new ValidationContext();

        for (VirtualServer vs : vses) {
            if (vs.getSlbIds().indexOf(slbId) < 0) {
                context.error(vs.getId(), MetaType.VS, ErrorType.DEPENDENCY_VALIDATION, "Cannot find target on slb " + slbId + ".");
            }
            try {
                virtualServerModelValidator.validateFields(vs);
            } catch (ValidationException e) {
                context.error(vs.getId(), MetaType.VS, ErrorType.FIELD_VALIDATION, e.getMessage());
            }
        }
        virtualServerModelValidator.validateDomains(slbId, vses, context);
    }

    @Override
    public void validateEntriesOnVs(Long vsId, List<Group> groups, List<TrafficPolicy> policies, ValidationContext context) {
        // validate groups
        if (groups == null) groups = new ArrayList<>();
        if (policies == null) policies = new ArrayList<>();
        if (context == null) context = new ValidationContext();

        for (Group group : groups) {
            groupModelValidator.validateFields(group, context);
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
                    groupModelValidator.validateGroupOnVses(gvsToBeChecked, group.isVirtual());
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
            trafficPolicyValidator.validateFields(policy, context);
            Long[] controlIds;
            try {
                controlIds = trafficPolicyValidator.validatePolicyControl(policy);
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
                    trafficPolicyValidator.validatePolicyOnVses(policy.getId(), controlIds, groupEntriesByVs, _pvsToBeChecked, _groupRelatedPolicyEntry, context);
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