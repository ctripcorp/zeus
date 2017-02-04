package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.PathRewriteParser;
import com.ctrip.zeus.service.model.PathValidator;
import com.ctrip.zeus.service.model.ValidationFacade;
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
    private static final String FIELD_VALIDATION = "FIELD_VALIDATION";
    private static final String DEPENDENCY_VALIDATION = "DEPENDENCY_VALIDATION";

    @Resource
    private VsEntryFactory vsEntryFactory;
    @Resource
    private PathValidator pathValidator;

    @Override
    public void validateGroup(Group group, ValidationContext context) {
        if (group.getName() == null || group.getName().isEmpty()
                || group.getAppId() == null || group.getAppId().isEmpty()) {
            context.error(group.getId(), MetaType.GROUP, FIELD_VALIDATION, "Field `name` and `app-id` are not allowed empty.");
        }
        if (!context.shouldProceed()) return;

        if (group.getHealthCheck() != null
                && (group.getHealthCheck().getUri() == null || group.getHealthCheck().getUri().isEmpty())) {
            context.error(group.getId(), MetaType.GROUP, FIELD_VALIDATION, "Field `health-check` is missing `uri` value.");
        }
        if (!context.shouldProceed()) return;

        try {
            validateMembers(group.getGroupServers());
        } catch (ValidationException e) {
            context.error(group.getId(), MetaType.GROUP, FIELD_VALIDATION, e.getMessage());
            return;
        }

        Map<Long, GroupVirtualServer> groupOnVses;
        try {
            groupOnVses = validateGroupOnVses(group.getGroupVirtualServers());
        } catch (ValidationException e) {
            context.error(group.getId(), MetaType.GROUP, FIELD_VALIDATION, e.getMessage());
            return;
        }

        if (groupOnVses == null || groupOnVses.size() == 0) return;

        List<LocationEntry> policyEntries = null;
        try {
            policyEntries = vsEntryFactory.getPolicyEntriesByGroup(group.getId());
        } catch (Exception e) {
        }
        try {
            validatePolicyRestriction(groupOnVses, policyEntries);
        } catch (ValidationException e) {
            context.error(group.getId(), MetaType.GROUP, DEPENDENCY_VALIDATION, e.getMessage());
            return;
        }

        try {
            Map<Long, List<LocationEntry>> locationEntries = vsEntryFactory.compareAndBuildLocationEntries(groupOnVses.keySet().toArray(new Long[groupOnVses.size()]), group.getId());
            for (int i = 0; i < group.getGroupVirtualServers().size(); i++) {
                GroupVirtualServer gvs = group.getGroupVirtualServers().get(i);
                List<LocationEntry> v = locationEntries.get(gvs.getVirtualServer().getId());
                if (v == null) {
                    v = new ArrayList<>();
                    locationEntries.put(gvs.getVirtualServer().getId(), v);
                }
                v.add(new LocationEntry().setEntryId(group.getId()).setEntryType(MetaType.GROUP).setVsId(gvs.getVirtualServer().getId()).setPath(gvs.getPath()).setPriority(gvs.getPriority()));
            }
            for (List<LocationEntry> entries : locationEntries.values()) {
                pathValidator.checkOverlapRestricition(entries, context);
            }
        } catch (ValidationException e) {
            context.error(group.getId(), MetaType.GROUP, DEPENDENCY_VALIDATION, e.getMessage());
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
                throw new ValidationException("Group has different `path` from its traffic policy " + e.getEntryId() + " on vs " + vsId + ".");
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

    }

    @Override
    public void validateVs(VirtualServer vs, ValidationContext context) {

    }

    @Override
    public void validateSlb(Slb slb, ValidationContext context) {

    }

    @Override
    public void validateEntriesOnVs(Long vsId, Set<Group> groups, Set<TrafficPolicy> policies, ValidationContext context) {

    }
}
