package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dao.entity.SlbArchiveVs;
import com.ctrip.zeus.dao.mapper.SlbArchiveVsMapper;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.flow.mergevs.MergeVsFlowService;
import com.ctrip.zeus.flow.mergevs.model.MergeVsFlowEntity;
import com.ctrip.zeus.flow.mergevs.model.MergeVsStatus;
import com.ctrip.zeus.flow.splitvs.SplitVsFlowService;
import com.ctrip.zeus.flow.splitvs.model.SplitVsFlowEntity;
import com.ctrip.zeus.flow.splitvs.model.SplitVsStatus;
import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.common.*;
import com.ctrip.zeus.service.model.handler.SlbQuery;
import com.ctrip.zeus.service.model.handler.impl.ContentReaders;
import com.ctrip.zeus.service.model.validation.*;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.tag.ItemTypes;
import com.ctrip.zeus.tag.PropertyNames;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.tag.PropertyValues;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

;

/**
 * Created by zhoumy on 2017/2/4.
 */
@Service("validationFacade")
public class ValidationFacadeImpl implements ValidationFacade {

    // Key=relation type   Value=the validator
    private static final Map<String, Validator<GroupVirtualServer>> relatedGvsValidators = new HashMap<>();

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
    private DrValidator drValidator;
    @Resource
    private RuleValidator ruleValidator;
    @Resource
    private SlbQuery slbQuery;

    @Resource
    private SlbArchiveVsMapper slbArchiveVsMapper;
    @Resource
    private PropertyService propertyService;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Autowired
    private GroupRepository groupRepository;
    @Resource
    private VirtualServerRepository virtualServerRepository;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private EntityFactory entityFactory;
    @Resource
    private MergeVsFlowService mergeVsFlowService;
    @Resource
    private SplitVsFlowService splitVsFlowService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    static {
        // For a relatedGvsValidator:
        // itemToValidateAgainst is the "original" group.
        // itemToValidate is the new group related to the "original" group.
        relatedGvsValidators.put(PropertyValues.RelationTypes.TRAFFIC_MIGRATION,
                new Validator<GroupVirtualServer>() {
                    @Override
                    public boolean validate(GroupVirtualServer itemToValidate, GroupVirtualServer itemToValidateAgainst) {
                        return itemToValidate.getPriority() < itemToValidateAgainst.getPriority();
                    }
                });
        relatedGvsValidators.put(PropertyValues.RelationTypes.EXTENDED_ROUTING,
                new Validator<GroupVirtualServer>() {
                    @Override
                    public boolean validate(GroupVirtualServer itemToValidate, GroupVirtualServer itemToValidateAgainst) {
                        if (itemToValidate.getRouteRules() == null || itemToValidate.getRouteRules().isEmpty()) {
                            // The group related to someone else has no rule.
                            // It shall have a lower priority to let the main group take all the requests.
                            return itemToValidate.getPriority() < itemToValidateAgainst.getPriority();
                        } else if (itemToValidateAgainst.getRouteRules() == null || itemToValidateAgainst.getRouteRules().isEmpty()) {
                            // The group related to someone else has some rules, and the other group has no rule.
                            // This means, the other group should be the main one.
                            // The related group shall have a high priority so the rules can take into effect.
                            return itemToValidate.getPriority() > itemToValidateAgainst.getPriority();
                        } else {
                            // Both groups have rule, so neither of them is the main group.
                            // We just force them having different priority values.
                            return !itemToValidate.getPriority().equals(itemToValidateAgainst.getPriority());
                        }
                    }
                });
    }

    @VisibleForTesting
    public void setPropertyService(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @VisibleForTesting
    public void setGroupRepository(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @VisibleForTesting
    public void setGroupCriteriaQuery(GroupCriteriaQuery query) {
        this.groupCriteriaQuery = query;
    }

    @Override
    public void validateGroup(Group group, ValidationContext context) {
        validateGroup(group, context, false);
    }

    @Override
    public Map<Long, ValidationContext> validateGroupPathOverlap(Group group, Long[] vsIds) throws ValidationException {

        Map<Long, ValidationContext> result = new HashMap<>();
        Map<Long, List<LocationEntry>> locationEntries = vsEntryFactory.buildLocationEntriesByVs(vsIds, null, null);
        for (Long vsId : locationEntries.keySet()) {
            List<LocationEntry> entries = locationEntries.get(vsId);
            ValidationContext context = new ValidationContext();
            pathValidator.checkPathOverlapAsError(entries, context);
            validateSkipErrorsOfRelatedGroup(group, context);
            result.put(vsId, context);
        }
        return result;
    }

    @Override
    public void validateGroup(Group group, ValidationContext context, boolean checkPathOverlap) {
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
            // Build VS-LocationEntry mapping for all the VSes the current group binds to, excluding all the entries associated with the current group.
            Map<Long, List<LocationEntry>> locationEntries;
            if (group.getId() == null) {
                locationEntries = vsEntryFactory.buildLocationEntriesByVs(groupOnVses.keySet().toArray(new Long[groupOnVses.size()]), null, null);
            } else {
                locationEntries = vsEntryFactory.buildLocationEntriesByVs(groupOnVses.keySet().toArray(new Long[groupOnVses.size()]), new Long[]{group.getId()}, null);
            }
            // Add LocationEntries of the current group into the map.
            for (Map.Entry<Long, GroupVirtualServer> e : groupOnVses.entrySet()) {
                List<LocationEntry> v = locationEntries.get(e.getKey());
                if (v == null) {
                    v = new ArrayList<>();
                    locationEntries.put(e.getKey(), v);
                }
                LocationEntry groupEntry = new LocationEntry().setEntryId(group.getId()).setEntryType(MetaType.GROUP)
                        .setVsId(e.getKey()).setName(e.getValue().getName()).setPath(e.getValue().getPath())
                        .setPriority(e.getValue().getPriority());
                v.add(groupEntry);
                groupLocationEntryOnVses.put(e.getKey(), groupEntry);
            }

            // There shall be no unresolvable path overlapping or name conflict in any VS.
            for (List<LocationEntry> entries : locationEntries.values()) {
                pathValidator.checkOverlapRestricition(entries, context);
                if (checkPathOverlap) {
                    pathValidator.checkPathOverlapAsError(entries, context);
                }
                checkForNameConflicts(entries, context);
            }

            // If the current group binds to any policy, ignore all the path validation errors between group and policy.
            if (policyEntries != null && context.getErrorGroups().contains(group.getId())) {
                for (LocationEntry e : policyEntries) {
                    context.ignoreGroupErrors(group.getId(), ErrorType.PATH_VALIDATION, MetaType.TRAFFIC_POLICY, e.getEntryId());
                    context.ignorePolicyErrors(e.getEntryId(), ErrorType.PATH_VALIDATION, MetaType.GROUP, group.getId());
                }
            }
            // There might be some priority adjustments in checkOverlapRestricition, sync it.
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
    public void validateDr(Dr dr, ValidationContext context) {
        try {
            drValidator.validateFields(dr, context);
        } catch (Exception e) {
            context.error(dr.getId(), MetaType.DR, ErrorType.FIELD_VALIDATION, e.getMessage());
            return;
        }

        Set<Long> sourceGroupIds = new HashSet<>();
        Set<Long> desGroupIds = new HashSet<>();
        for (DrTraffic traffic : dr.getDrTraffics()) {
            sourceGroupIds.add(traffic.getGroup().getId());
            for (Destination des : traffic.getDestinations()) {
                for (TrafficControl control : des.getControls()) {
                    desGroupIds.add(control.getGroup().getId());
                }
            }
        }
        try {
            drValidator.checkGroupRelations(dr, sourceGroupIds, desGroupIds);
        } catch (Exception e) {
            context.error(dr.getId(), MetaType.DR, ErrorType.DEPENDENCY_VALIDATION, e.getMessage());
            return;
        }

        try {
            drValidator.checkDrProperties(sourceGroupIds);
        } catch (Exception e) {
            context.error(dr.getId(), MetaType.DR, ErrorType.DRPROPERTY_VALIDATION, e.getMessage());
            return;
        }

        try {
            drValidator.checkGroupAvailability(desGroupIds);
        } catch (Exception e) {
            context.error(dr.getId(), MetaType.DR, ErrorType.DEPENDENCY_VALIDATION, e.getMessage());
            return;
        }

        List<DrValidator.Node> nodes;
        try {
            Map<Long, Group> groups = new HashMap<>();
            Set<Long> vsIds = new HashSet<>();
            Map<Long, VirtualServer> vsLookup = new HashMap<>();

            for (Group g : groupRepository.list(sourceGroupIds.toArray(new Long[sourceGroupIds.size()]))) {
                groups.put(g.getId(), g);
                for (GroupVirtualServer gvs : g.getGroupVirtualServers()) {
                    vsIds.add(gvs.getVirtualServer().getId());
                }
            }
            for (VirtualServer vs : virtualServerRepository.listAll(vsIds.toArray(new Long[vsIds.size()]))) {
                vsLookup.put(vs.getId(), vs);
            }

            Set<IdVersion> idVersionSet = slbCriteriaQuery.queryAll(SelectionMode.ONLINE_EXCLUSIVE);
            List<Slb> slbs = slbRepository.list(idVersionSet.toArray(new IdVersion[idVersionSet.size()]));
            List<Long> slbIds = new ArrayList<>();
            for (Slb slb : slbs) {
                slbIds.add(slb.getId());
            }
            Map<Long, Property> slbIdcInfo = propertyService.getProperties("idc_code", "slb", slbIds.toArray(new Long[slbIds.size()]));
            Map<Long, Property> groupIdcInfo = propertyService.getProperties("idc_code", "group", sourceGroupIds.toArray(new Long[sourceGroupIds.size()]));
            nodes = drValidator.checkGroupsAndVses(dr, groups, vsLookup, slbIdcInfo, groupIdcInfo);
        } catch (Exception e) {
            context.error(dr.getId(), MetaType.DR, ErrorType.DEPENDENCY_VALIDATION, e.getMessage());
            return;
        }

        try {
            drValidator.validateLoop(nodes);
        } catch (Exception e) {
            context.error(dr.getId(), MetaType.DR, ErrorType.DEPENDENCY_VALIDATION, e.getMessage());
        }
    }

    @Override
    public void validateRule(Rule rule, ValidationContext context) {
        try {
            ruleValidator.validateFields(rule);
        } catch (Exception e) {
            context.error(rule.getId(), MetaType.RULE, ErrorType.FIELD_VALIDATION, e.getMessage());
            return;
        }
        try {
            RuleTargetType ruleTargetType = RuleTargetType.getTargetType(rule.getTargetType());
            ruleValidator.checkRuleTarget(ruleTargetType, rule.getTargetId());
        } catch (Exception e) {
            context.error(rule.getId(), MetaType.RULE, ErrorType.DEPENDENCY_VALIDATION, e.getMessage());
        }
    }

    @Override
    public Long validateDrDestination(Group sourceGroup, Group desGroup, VirtualServer sourceVs, Map<Long, VirtualServer> vsLookup, Map<Long, Property> slbIdcInfo, Map<Long, Property> groupIdcInfo) throws Exception {
        Set<Long> gid = new HashSet<>();
        gid.add(desGroup.getId());
        drValidator.checkGroupAvailability(gid);
        VirtualServer desVs = drValidator.checkAndGetDesVs(sourceGroup, desGroup, sourceVs, vsLookup);
        return drValidator.checkAndGetDesSlbId(desGroup.getId(), desVs, slbIdcInfo, groupIdcInfo);
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
            for (SlbArchiveVs e : slbArchiveVsMapper.findAllBySlbsAndVsOfflineVersion(Arrays.asList(relatedSlbIds))) {
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
                Map<Long, List<LocationEntry>> groupRelatedPolicyEntry = new HashMap<>();
                for (Long controlId : controlIds) {
                    LocationEntry p = policyEntriesByGroup.get(controlId);
                    if (p != null) {
                        List<LocationEntry> v = groupRelatedPolicyEntry.get(vsId);
                        if (v == null) {
                            v = new ArrayList<>();
                            groupRelatedPolicyEntry.put(vsId, v);
                        }
                        v.add(p);
                    }
                }
                List<PolicyVirtualServer> pvsToBeChecked = new ArrayList<>(1);
                pvsToBeChecked.add(target);
                try {
                    trafficPolicyValidator.validatePolicyOnVses(policy.getId(), controlIds, groupEntriesByVs, pvsToBeChecked, groupRelatedPolicyEntry, context);
                } catch (ValidationException e) {
                    context.error(policy.getId(), MetaType.TRAFFIC_POLICY, ErrorType.DEPENDENCY_VALIDATION, e.getMessage());
                }
            }
            Collections.addAll(escapedGroupIds, controlIds);
        }
        // validate paths
        Set<LocationEntry> locationEntries = new HashSet<>();
        locationEntries.addAll(policyEntriesByGroup.values());
        for (LocationEntry e : groupEntriesByVs.get(vsId)) {
            if (escapedGroupIds.contains(e.getEntryId())) continue;
            locationEntries.add(e);
        }
        pathValidator.checkOverlapRestricition(new ArrayList<>(locationEntries), context);
        pathValidator.checkPathOverlapAsError(groupEntriesByVs.get(vsId), context);
        checkForNameConflicts(locationEntries, context);
    }

    private static void checkForNameConflicts(Collection<LocationEntry> locationEntries, ValidationContext context) {
        Map<String, LocationEntry> namedLocationEntries = new HashMap<>();
        for (LocationEntry entry : locationEntries) {
            if (entry.getName() == null || entry.getName().isEmpty()) {
                continue;
            }
            LocationEntry existedEntry = namedLocationEntries.get(entry.getName());
            if (existedEntry != null) {
                String message = String.format("Name '%s' conflicts with %s-%d", entry.getName(),
                        existedEntry.getEntryType(), existedEntry.getEntryId());
                context.error(entry.getEntryId(), entry.getEntryType(), ErrorType.NAME_VALIDATION, message);
            } else {
                namedLocationEntries.put(entry.getName(), entry);
            }
        }
    }

    @Override
    public void validateSyngeneticVs(Set<Long> vsIds, Collection<Group> groups, Collection<TrafficPolicy> policies, Collection<Dr> drs, ValidationContext context) {
        if (vsIds == null || vsIds.size() <= 1) return;
        if (groups == null) {
            return;
        }

        Long[] vsIdArray = vsIds.toArray(new Long[vsIds.size()]);
        List<Group> inGroups = new ArrayList<>();
        List<TrafficPolicy> inPolicies = new ArrayList<>();

        //Check Groups
        for (Group group : groups) {
            Map<Long, GroupVirtualServer> gvsMap = new HashMap<>();
            for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
                gvsMap.put(gvs.getVirtualServer().getId(), gvs);
            }
            HashSet<Long> tmpIds = new HashSet<>(gvsMap.keySet());
            tmpIds.retainAll(vsIds);
            if (tmpIds.size() == 0) continue;
            inGroups.add(group);

            if (gvsMap.keySet().containsAll(vsIds)) {
                //1. compare path
                GroupVirtualServer gvs = gvsMap.get(vsIdArray[0]);
                for (int i = 1; i < vsIdArray.length; i++) {
                    GroupVirtualServer tmpGvs = gvsMap.get(vsIdArray[i]);
                    if (!equals(gvs.getPath(), tmpGvs.getPath()) || !equals(gvs.getRedirect(), tmpGvs.getRedirect())
                            || !equals(gvs.getRewrite(), tmpGvs.getRewrite()) || !equals(gvs.getCustomConf(), tmpGvs.getCustomConf())
                            || !(gvs.getRouteRules().containsAll(tmpGvs.getRouteRules()) && tmpGvs.getRouteRules().containsAll(gvs.getRouteRules()))) {
                        context.error(group.getId(), MetaType.GROUP, ErrorType.DEPENDENCY_VALIDATION,
                                "Group Virtual Server Config Not Equals; VsIds:" + vsIds + ";GroupId:" + group.getId());
                        break;
                    }
                }
            } else {
                context.error(group.getId(), MetaType.GROUP, ErrorType.DEPENDENCY_VALIDATION,
                        "Group Virtual Server Missing VsId.VsIds:" + vsIds + ";GroupId:" + group.getId());
            }
        }

        //Check Policies
        for (TrafficPolicy policy : policies) {
            Map<Long, PolicyVirtualServer> pvsMap = new HashMap<>();
            for (PolicyVirtualServer pvs : policy.getPolicyVirtualServers()) {
                pvsMap.put(pvs.getVirtualServer().getId(), pvs);
            }
            HashSet<Long> tmpIds = new HashSet<>(pvsMap.keySet());
            tmpIds.retainAll(vsIds);
            //skip objects not related to vsIds.
            if (tmpIds.size() == 0) continue;
            inPolicies.add(policy);

            if (pvsMap.keySet().containsAll(vsIds)) {
                //1. compare path
                PolicyVirtualServer pvs = pvsMap.get(vsIdArray[0]);
                for (int i = 1; i < vsIdArray.length; i++) {
                    PolicyVirtualServer tmpPvs = pvsMap.get(vsIdArray[i]);
                    if (!equals(pvs.getPath(), tmpPvs.getPath())) {
                        context.error(policy.getId(), MetaType.TRAFFIC_POLICY, ErrorType.DEPENDENCY_VALIDATION,
                                "Policy Virtual Server Config Not Equals; VsIds:" + vsIds + ";PolicyId:" + policy.getId());
                        break;
                    }
                }
            } else {
                context.error(policy.getId(), MetaType.TRAFFIC_POLICY, ErrorType.DEPENDENCY_VALIDATION,
                        "Policy Virtual Server Missing VsId.VsIds:" + vsIds + ";PolicyId:" + policy.getId());
            }
        }

        //Check DR
        for (Dr dr : drs) {
            for (DrTraffic drt : dr.getDrTraffics()) {
                Map<Long, Destination> desMap = new HashMap<>();
                for (Destination des : drt.getDestinations()) {
                    desMap.put(des.getVirtualServer().getId(), des);
                }
                HashSet<Long> tmpIds = new HashSet<>(desMap.keySet());
                tmpIds.retainAll(vsIds);
                //skip objects not related to vsIds.
                if (tmpIds.size() == 0) continue;

                if (desMap.keySet().containsAll(vsIds)) {
                    //1. compare path
                    Destination des = desMap.get(vsIdArray[0]);
                    for (int i = 1; i < vsIdArray.length; i++) {
                        Destination tmpDes = desMap.get(vsIdArray[i]);
                        if (!(des.getControls().containsAll(tmpDes.getControls()) && tmpDes.getControls().containsAll(des.getControls()))) {
                            context.error(dr.getId(), MetaType.DR, ErrorType.DEPENDENCY_VALIDATION,
                                    "Destination Config Not Equals; VsIds:" + vsIds + ";DrId:" + dr.getId());
                            break;
                        }
                    }
                } else {
                    context.error(dr.getId(), MetaType.DR, ErrorType.DEPENDENCY_VALIDATION,
                            "Destination Config Not Equals.VsIds:" + vsIds + ";DrId:" + dr.getId());
                }
            }
        }

        //Check Priority
        if (!context.shouldProceed()) {
            return;
        }
        List<Long> gids = new ArrayList<>();
        List<Long> pids = new ArrayList<>();
        sortByPriority(vsIdArray[0], inGroups, inPolicies);
        for (Group g : inGroups) {
            gids.add(g.getId());
        }
        for (TrafficPolicy policy : inPolicies) {
            pids.add(policy.getId());
        }
        for (int i = 1; i < vsIdArray.length; i++) {
            sortByPriority(vsIdArray[i], inGroups, inPolicies);
            for (int j = 0; j < inGroups.size(); j++) {
                if (!gids.get(j).equals(inGroups.get(j).getId())) {
                    context.error(gids.get(j), MetaType.GROUP, ErrorType.DEPENDENCY_VALIDATION,
                            "Group Have Different Priority Stage In Vses.VsIds:" + vsIds + ";GroupId:" + gids.get(j));
                }
            }
            for (int k = 0; k < inPolicies.size(); k++) {
                if (!pids.get(k).equals(inPolicies.get(k).getId())) {
                    context.error(pids.get(k), MetaType.TRAFFIC_POLICY, ErrorType.DEPENDENCY_VALIDATION,
                            "Policy Have Different Priority Stage In Vses.VsIds:" + vsIds + ";PolicyId:" + pids.get(k));
                }
            }
        }
    }

    @Override
    public void validateForSplitVs(Long vsId, Long entityId) throws Exception {
        Long[] vsIds = new Long[]{vsId};

        ModelStatusMapping<VirtualServer> vsMap = entityFactory.getVsesByIds(vsIds);
        Map<Long, VirtualServer> onlineVs = vsMap.getOnlineMapping();
        Map<Long, VirtualServer> offlineVs = vsMap.getOfflineMapping();
        if (!offlineVs.containsKey(vsId)) {
            throw new ValidationException("Not Found Vs.VsID:" + vsId);
        }
        for (Long vid : offlineVs.keySet()) {
            if (!onlineVs.containsKey(vid) || !offlineVs.get(vid).getVersion().equals(onlineVs.get(vid).getVersion())) {
                throw new ValidationException("Vs Is Deactivated  Or ToBeActivate .Please Delete Or Activate Vs First.VsId:" + vid);
            }
        }

        validateInMergeOrSplitFlow(Collections.singletonList(vsId), entityId, null);

        ModelStatusMapping<Group> groupMap = entityFactory.getGroupsByVsIds(vsIds);
        Map<Long, Group> onlineGroup = groupMap.getOnlineMapping();
        Map<Long, Group> offlineGroup = groupMap.getOfflineMapping();

        for (Long gid : offlineGroup.keySet()) {
            if (!onlineGroup.containsKey(gid) || !offlineGroup.get(gid).getVersion().equals(onlineGroup.get(gid).getVersion())) {
                throw new ValidationException("Have Deactivated Group Or ToBeActivate Group.Please Delete Or Activate Group First.GroupId:" + gid);
            }
        }

        ModelStatusMapping<TrafficPolicy> policyMap = entityFactory.getPoliciesByVsIds(vsIds);
        Map<Long, TrafficPolicy> onlinePolicy = policyMap.getOnlineMapping();
        Map<Long, TrafficPolicy> offlinePolicy = policyMap.getOfflineMapping();
        for (Long pid : offlinePolicy.keySet()) {
            if (!onlinePolicy.containsKey(pid) || !offlinePolicy.get(pid).getVersion().equals(onlinePolicy.get(pid).getVersion())) {
                throw new ValidationException("Have Deactivated Policy Or ToBeActivate Policy.Please Delete Or Activate Policy First.PolicyId:" + pid);
            }
        }

        ModelStatusMapping<Dr> drMap = entityFactory.getDrsByVsIds(vsIds);
        Map<Long, Dr> onlineDr = drMap.getOnlineMapping();
        Map<Long, Dr> offlineDr = drMap.getOfflineMapping();
        for (Long did : offlineDr.keySet()) {
            if (!onlineDr.containsKey(did) || !offlineDr.get(did).getVersion().equals(onlineDr.get(did).getVersion())) {
                throw new ValidationException("Have Deactivated Dr Or ToBeActivate Dr.Please Delete Or Activate Dr First.DrId:" + did);
            }
        }
    }

    @Override
    public void validateForMergeVs(List<Long> vses, Long mergeVsId) throws Exception {
        Long[] vsIds = vses.toArray(new Long[vses.size()]);

        ModelStatusMapping<VirtualServer> vsMap = entityFactory.getVsesByIds(vsIds);
        Map<Long, VirtualServer> onlineVs = vsMap.getOnlineMapping();
        Map<Long, VirtualServer> offlineVs = vsMap.getOfflineMapping();
        if (!offlineVs.keySet().containsAll(vses)) {
            throw new ValidationException("Not Found All Vses:" + vses.toString() + ";Offline Vses:" + offlineVs.keySet().toString());
        }
        for (Long vid : offlineVs.keySet()) {
            if (!onlineVs.containsKey(vid) || !offlineVs.get(vid).getVersion().equals(onlineVs.get(vid).getVersion())) {
                throw new ValidationException("Vs Is Deactivated  Or ToBeActivate .Please Delete Or Activate Vs First.VsId:" + vid);
            }
        }

        validateInMergeOrSplitFlow(vses, null, mergeVsId);

        //Validate SlbIds, Vses Should Have Same SlbIds, and have same ssl config.
        Long tmpId = vsIds[0];
        List<Long> slbIds = new ArrayList<>(onlineVs.get(tmpId).getSlbIds());
        String port = onlineVs.get(tmpId).getPort();
        Boolean ssl = onlineVs.get(tmpId).getSsl();
        for (int i = 1; i < vsIds.length; i++) {
            slbIds.retainAll(onlineVs.get(vsIds[i]).getSlbIds());
            if (!ssl.equals(onlineVs.get(vsIds[i]).getSsl()) || !port.equalsIgnoreCase(onlineVs.get(vsIds[i]).getPort())) {
                throw new ValidationException("Vses Have Different Port/SSL List.VsIds:" + vses);
            }
        }
        if (!slbIds.containsAll(onlineVs.get(tmpId).getSlbIds())) {
            throw new ValidationException("Vses Have Different SlbId List.VsIds:" + vses);
        }


        // Group Policy Dr Should Be Activated.
        ModelStatusMapping<Group> groupMap = entityFactory.getGroupsByVsIds(vsIds);
        Map<Long, Group> onlineGroup = groupMap.getOnlineMapping();
        Map<Long, Group> offlineGroup = groupMap.getOfflineMapping();

        for (Long gid : offlineGroup.keySet()) {
            if (!onlineGroup.containsKey(gid) || !offlineGroup.get(gid).getVersion().equals(onlineGroup.get(gid).getVersion())) {
                throw new ValidationException("Have Deactivated Group Or ToBeActivate Group.Please Delete Or Activate Group First.GroupId:" + gid);
            }
        }

        ModelStatusMapping<TrafficPolicy> policyMap = entityFactory.getPoliciesByVsIds(vsIds);
        Map<Long, TrafficPolicy> onlinePolicy = policyMap.getOnlineMapping();
        Map<Long, TrafficPolicy> offlinePolicy = policyMap.getOfflineMapping();
        for (Long pid : offlinePolicy.keySet()) {
            if (!onlinePolicy.containsKey(pid) || !offlinePolicy.get(pid).getVersion().equals(onlinePolicy.get(pid).getVersion())) {
                throw new ValidationException("Have Deactivated Policy Or ToBeActivate Policy.Please Delete Or Activate Policy First.PolicyId:" + pid);
            }
        }

        ModelStatusMapping<Dr> drMap = entityFactory.getDrsByVsIds(vsIds);
        Map<Long, Dr> onlineDr = drMap.getOnlineMapping();
        Map<Long, Dr> offlineDr = drMap.getOfflineMapping();
        for (Long did : offlineDr.keySet()) {
            if (!onlineDr.containsKey(did) || !offlineDr.get(did).getVersion().equals(onlineDr.get(did).getVersion())) {
                throw new ValidationException("Have Deactivated Dr Or ToBeActivate Dr.Please Delete Or Activate Dr First.DrId:" + did);
            }
        }

        // Relationship Between Vses and Group Policy Dr Should Be The Same.
        // Priority Sort Should Be The Same
        ValidationContext context = new ValidationContext();
        validateSyngeneticVs(new HashSet<>(vses), new ArrayList<>(offlineGroup.values()),
                new ArrayList<>(offlinePolicy.values()), new ArrayList<>(offlineDr.values()), context);
        if (!context.shouldProceed()) {
            throw new ValidationException("Validate Syngenetic Vs Failed.Errors:" + context.getErrors().toString());
        }

    }

    private void validateInMergeOrSplitFlow(List<Long> vses, Long splitEntity, Long mergeEntity) throws Exception {
        List<MergeVsFlowEntity> entities = mergeVsFlowService.queryAll();
        for (MergeVsFlowEntity entity : entities) {
            if (!MergeVsStatus.FINISH_CLEAN.equalsIgnoreCase(entity.getStatus()) && !entity.getId().equals(mergeEntity)) {
                List<Long> sourceVsIds = new ArrayList<>(entity.getSourceVsId());
                sourceVsIds.retainAll(vses);
                if (vses.contains(entity.getNewVsId()) || sourceVsIds.size() > 0) {
                    throw new ValidationException("Vs Is Doing Merge Vs In Vs Merge Flow Id:" + entity.getId());
                }
            }
        }
        List<SplitVsFlowEntity> splitVsFlowEntities = splitVsFlowService.queryAll();
        for (SplitVsFlowEntity entity : splitVsFlowEntities) {
            if (!SplitVsStatus.FINISH_SPLIT_VS.equalsIgnoreCase(entity.getStatus()) && !entity.getId().equals(splitEntity)) {
                List<Long> newVsIds = new ArrayList<>(entity.getNewVsIds());
                newVsIds.retainAll(vses);
                if (vses.contains(entity.getSourceVsId()) || newVsIds.size() > 0) {
                    throw new ValidationException("Vs Is Doing Split Vs In Vs Split Flow Id:" + entity.getId());
                }
            }
        }
    }

    protected void sortByPriority(Long vsId, List<Group> groups, List<TrafficPolicy> policies) {
        final Map<String, Object> objectOnVsReferrer = new HashMap<>();
        for (TrafficPolicy p : policies) {
            for (PolicyVirtualServer pvs : p.getPolicyVirtualServers()) {
                if (pvs.getVirtualServer().getId().equals(vsId)) {
                    objectOnVsReferrer.put("pvs-" + p.getId(), pvs);
                }
            }
        }
        for (Group g : groups) {
            for (GroupVirtualServer gvs : g.getGroupVirtualServers()) {
                if (gvs.getVirtualServer().getId().equals(vsId)) {
                    objectOnVsReferrer.put("gvs-" + g.getId(), gvs);
                }
            }
        }
        Collections.sort(policies, new Comparator<TrafficPolicy>() {
            @Override
            public int compare(TrafficPolicy o1, TrafficPolicy o2) {
                int result = ((PolicyVirtualServer) objectOnVsReferrer.get("pvs-" + o2.getId())).getPriority() -
                        ((PolicyVirtualServer) objectOnVsReferrer.get("pvs-" + o1.getId())).getPriority();
                return result == 0 ? o2.getId().compareTo(o1.getId()) : result;
            }
        });
        Collections.sort(groups, new Comparator<Group>() {
            @Override
            public int compare(Group o1, Group o2) {
                int result = ((GroupVirtualServer) objectOnVsReferrer.get("gvs-" + o2.getId())).getPriority() -
                        ((GroupVirtualServer) objectOnVsReferrer.get("gvs-" + o1.getId())).getPriority();
                return result == 0 ? o2.getId().compareTo(o1.getId()) : result;
            }
        });
    }

    protected boolean equals(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        } else if (o2 == null) {
            return false;
        } else {
            return o1.equals(o2);
        }
    }

    @Override
    public boolean validateRelatedGroup(Group group, String relatedAppId, String relationType, Long targetVsId, List<Group> targetGroups) {
        logger.info("starting validate related group");
        Map<Long, GroupVirtualServer> groupGvses = new HashMap<>();
        for (GroupVirtualServer tmp : group.getGroupVirtualServers()) {
            if (targetVsId == null || targetVsId.equals(tmp.getVirtualServer().getId())) {
                groupGvses.put(tmp.getVirtualServer().getId(), tmp);
            }
        }
        if (groupGvses.isEmpty() && targetVsId != null) {
            // The caller specifies a VS, but the group isn't bound to it. Fail!
            return false;
        }

        logger.info("starting validate outgoing and incoming relationship");
        return validateOutgoingRelationship(group, relatedAppId, relationType, targetGroups, groupGvses)
                && validateIncomingRelationship(group, groupGvses);
    }

    private boolean validateOutgoingRelationship(Group group, String relatedAppId, String relationType, List<Group> targetGroups,
                                                 Map<Long, GroupVirtualServer> groupGvses) {
        if (relatedAppId == null) {
            try {
                relatedAppId = propertyService.getPropertyValue(PropertyNames.RELATED_APP_ID, group.getId(), ItemTypes.GROUP, null);
            } catch (Exception e) {
                logger.error("[[method=validateRelatedGroup]]Get relatedAppId property failed. Group: " + group.getId(), e);
                return false;
            }
        }

        if (relatedAppId == null) {
            // It's related to nobody. Good.
            return true;
        }

        if (targetGroups == null) {
            try {
                Set<Long> groupIds = groupCriteriaQuery.queryByAppId(relatedAppId);
                groupIds.remove(group.getId());
                targetGroups = groupRepository.list((groupIds.toArray(new Long[groupIds.size()])));
            } catch (Exception e) {
                logger.error("[[method=validateRelatedGroup]]Get target groups failed. RelatedAppId: " + relatedAppId, e);
                return false;
            }
        }

        if (relationType == null) {
            try {
                relationType = propertyService.getPropertyValue(PropertyNames.RELATION_TYPE, group.getId(), ItemTypes.GROUP, PropertyValues.RelationTypes.DEFAULT);
            } catch (Exception e) {
                logger.error("[[method=validateRelatedGroup]]Get relationType property failed. Group: " + group.getId(), e);
                return false;
            }
        }

        // Modify validation logic here
        // Pass validation when there's at least one target group whose paths are exactly same with to-be-created group's paths
        logger.info("validate outgoing new logic.");
        for (Group targetGroup : targetGroups) {
            logger.info("target group: " + targetGroup.getId());
            if (targetGroup.getId().equals(group.getId()) || !relatedAppId.equals(targetGroup.getAppId())) {
                continue;
            }
            boolean allPathEquals = true;
            for (GroupVirtualServer targetGvs : targetGroup.getGroupVirtualServers()) {
                GroupVirtualServer groupGvs = groupGvses.get(targetGvs.getVirtualServer().getId());
                if (groupGvs == null) {
                    continue;
                }
                if (!validateRelatedPair(groupGvs, targetGvs, relationType)) {
                    allPathEquals = false;
                }
            }
            if (allPathEquals) {
                logger.info("all path equals with related group");
                return true;
            }
        }
        logger.info("none of old groups share same path with related group");
        return false;
    }

    private boolean validateIncomingRelationship(Group group, Map<Long, GroupVirtualServer> groupGvses) {
        logger.info("validate incoming relationship");
        List<Group> groupsRelatedToMe;
        try {
            List<Long> groupIdsRelatedToMe = propertyService.queryTargets(PropertyNames.RELATED_APP_ID, group.getAppId(), ItemTypes.GROUP);
            groupIdsRelatedToMe.remove(group.getId());
            groupsRelatedToMe = groupRepository.list((groupIdsRelatedToMe.toArray(new Long[groupIdsRelatedToMe.size()])));
        } catch (Exception e) {
            logger.error("[[method=validateRelatedGroup]]Failed to load groups related to group " + group.getId(), e);
            return false;
        }
        if (groupsRelatedToMe == null || groupsRelatedToMe.size() <= 0) {
            return true;
        }
        // Modify validation logic here
        // Pass validation when there's at least one target group whose paths are exactly same with to-be-created group's paths
        logger.info("validate incoming groups new logic");
        for (Group groupRelatedToMe : groupsRelatedToMe) {
            logger.info("validate related group to me. group id: " + groupRelatedToMe.getId());
            boolean allPathEqual = true;
            for (GroupVirtualServer relatedGvs : groupRelatedToMe.getGroupVirtualServers()) {
                GroupVirtualServer groupGvs = groupGvses.get(relatedGvs.getVirtualServer().getId());
                if (groupGvs == null) {
                    continue;
                }
                String relationType;
                try {
                    relationType = propertyService.getPropertyValue(PropertyNames.RELATION_TYPE, groupRelatedToMe.getId(),
                            ItemTypes.GROUP, PropertyValues.RelationTypes.DEFAULT);
                } catch (Exception e) {
                    logger.error("[[method=validateRelatedGroup]]Get relationType property failed. Group: " + group.getId(), e);
                    return false;
                }
                if (!validateRelatedPair(relatedGvs, groupGvs, relationType)) {
                    allPathEqual = false;
                }
            }
            if (allPathEqual) {
                logger.info("one group existed with all path equals");
                return true;
            }
        }
        logger.info("none group existed with all path equals");
        return false;
    }

    /**
     * Validate a related group virtual server pair.
     *
     * @param outgoingEndGvs the GroupVirtualServer object of the group related to some other group
     * @param incomingEndGvs the GroupVirtualServer object of the group being related by some other group
     * @param relationType   the type of the relationship
     * @return true if the relationship is valid.
     */
    private boolean validateRelatedPair(GroupVirtualServer outgoingEndGvs, GroupVirtualServer incomingEndGvs, String relationType) {

        if (!outgoingEndGvs.getPath().startsWith("~")) {
            //Only path start with "~" can be multi in nginx conf.
            return false;
        }
        if (!(outgoingEndGvs.getPath() != null && outgoingEndGvs.getPath().equals(incomingEndGvs.getPath())) &&
                !(pathValidator.contains(outgoingEndGvs.getPath(), incomingEndGvs.getPath()) && pathValidator.contains(incomingEndGvs.getPath(), outgoingEndGvs.getPath()))) {
            //if path is not completely equals and not equals in logic, then return false instead.
            return false;
        }
        Validator<GroupVirtualServer> relatedGvsValidator = relatedGvsValidators.get(relationType);
        if (relatedGvsValidator == null) {
            relatedGvsValidator = relatedGvsValidators.get(PropertyValues.RelationTypes.DEFAULT);
        }
        if (!relatedGvsValidator.validate(outgoingEndGvs, incomingEndGvs)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean validateRelatedGroup(Long vsId, List<Group> vsGroups, Group group, String relatedAppId) {
        if (relatedAppId == null) {
            try {
                Property p = propertyService.getProperty("relatedAppId", group.getId(), "group");
                if (p != null) {
                    relatedAppId = p.getValue();
                }
            } catch (Exception e) {
                logger.error("Get Property Failed.relatedAppId,group," + group.getId(), e);
            }
        }
        if (vsId == null || relatedAppId == null || group == null) {
            return false;
        }
        GroupVirtualServer gvs = null;
        for (GroupVirtualServer tmp : group.getGroupVirtualServers()) {
            if (tmp.getVirtualServer().getId().equals(vsId)) {
                gvs = tmp;
                break;
            }
        }
        if (gvs == null) return false;
        for (Group g : vsGroups) {
            if (g.getAppId().equals(relatedAppId)) {
                GroupVirtualServer groupVirtualServer = null;
                for (GroupVirtualServer tmp : g.getGroupVirtualServers()) {
                    if (tmp.getVirtualServer().getId().equals(vsId)) {
                        groupVirtualServer = tmp;
                        break;
                    }
                }
                if (groupVirtualServer != null) {
                    if (!pathValidator.contains(gvs.getPath(), groupVirtualServer.getPath())
                            || !pathValidator.contains(groupVirtualServer.getPath(), gvs.getPath())
                            || !gvs.getPath().startsWith("~")) {
                        return false;
                    }
                    if (groupVirtualServer.getPriority() < gvs.getPriority()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean validateRelatedGroup(Group group, String relatedAppId) {
        if (relatedAppId == null) {
            try {
                Property p = propertyService.getProperty("relatedAppId", group.getId(), "group");
                if (p != null) {
                    relatedAppId = p.getValue();
                }
                if (relatedAppId == null) {
                    return true;
                }
            } catch (Exception e) {
                logger.error("Get Property Failed.relatedAppId,group," + group.getId(), e);
                return false;
            }
        }
        Map<Long, GroupVirtualServer> gvsMap = new HashMap<>();
        for (GroupVirtualServer tmp : group.getGroupVirtualServers()) {
            gvsMap.put(tmp.getVirtualServer().getId(), tmp);
        }

        List<Group> groups = null;
        try {
            Set<Long> groupIds = groupCriteriaQuery.queryByAppId(relatedAppId);
            groupIds.remove(group.getId());
            groups = groupRepository.list((groupIds.toArray(new Long[groupIds.size()])));
        } catch (Exception e) {
            logger.error("Get Groups Failed.RelatedAppId:" + relatedAppId, e);
            return false;
        }
        if (groups == null) {
            return true;
        }
        for (Group g : groups) {
            for (GroupVirtualServer gvs : g.getGroupVirtualServers()) {
                if (!gvsMap.containsKey(gvs.getVirtualServer().getId())) continue;
                GroupVirtualServer relatedGvs = gvsMap.get(gvs.getVirtualServer().getId());
                if (!pathValidator.contains(gvs.getPath(), relatedGvs.getPath())
                        || !pathValidator.contains(relatedGvs.getPath(), gvs.getPath())
                        || !gvs.getPath().startsWith("~")) {
                    return false;
                }
                if (relatedGvs.getPriority() > gvs.getPriority()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void validateSkipErrorsOfWhiteList(String type, ValidationContext context) {
        try {
            if (type.equalsIgnoreCase("group")) {
                List<Long> whiteList = propertyService.queryTargets("skipPathValidation", "true", "group");
                for (Long id : context.getErrorGroups()) {
                    if (whiteList.contains(id)) {
                        context.ignoreGroupErrors(id, ErrorType.PATH_VALIDATION);
                        context.ignoreGroupErrors(id, ErrorType.PATH_OVERLAP);
                    }
                }
            } else if (type.equalsIgnoreCase("policy")) {
                List<Long> whiteList = propertyService.queryTargets("skipPathValidation", "true", "policy");
                for (Long id : context.getErrorPolicies()) {
                    if (whiteList.contains(id)) {
                        context.ignorePolicyErrors(id, ErrorType.PATH_VALIDATION);
                        context.ignorePolicyErrors(id, ErrorType.PATH_OVERLAP);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Get Property Failed.", e);
        }
    }

    @Override
    public void validateSkipErrorsOfRelatedGroup(Group group, ValidationContext context) {
        try {
            logger.info("skip related group error: " + group.getId());
            List<Long> groupIds = propertyService.queryTargets("relatedAppId", group.getAppId(), "group");
            logger.info("groups with relatedAppId equals to " + group.getAppId() + ": " + groupIds);
            for (Long gid : groupIds) {
                context.ignoreGroupErrors(group.getId(), ErrorType.PATH_VALIDATION, MetaType.GROUP, gid);
            }
            context.ignoreGroupErrors(group.getId(), ErrorType.PATH_OVERLAP);
            Property property = propertyService.getProperty("relatedAppId", group.getId(), "group");
            if (property != null) {
                Set<Long> gids = groupCriteriaQuery.queryByAppId(property.getValue());
                logger.info("groups under appId : " + property.getValue() + ": " + gids);
                for (Long gid : gids) {
                    context.ignoreGroupErrors(group.getId(), ErrorType.PATH_VALIDATION, MetaType.GROUP, gid);
                }
            }
        } catch (Exception e) {
            logger.error("Get Property Failed.", e);
        }
    }

    private interface Validator<T> {
        boolean validate(T itemToValidate, T itemToValidateAgainst);
    }
}