package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.PathRewriteParser;
import com.ctrip.zeus.service.model.ValidationFacade;
import com.ctrip.zeus.service.model.common.MetaType;
import com.ctrip.zeus.service.model.common.ValidationContext;
import com.ctrip.zeus.service.model.handler.VsEntryQuery;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zhoumy on 2017/2/4.
 */
@Service("validationFacade")
public class ValidationFacadeImpl implements ValidationFacade {
    private static final String ERROR_TYPE = "FIELD_VALIDATION";

    @Resource
    private VsEntryQuery vsEntryQuery;

    @Override
    public void validateGroup(Group group, ValidationContext context) {
        if (group.getName() == null || group.getName().isEmpty()
                || group.getAppId() == null || group.getAppId().isEmpty()) {
            context.error(group.getId(), MetaType.GROUP, ERROR_TYPE, "Field `name` and `app-id` are not allowed empty.");
        }
        if (group.getHealthCheck() != null
                && (group.getHealthCheck().getUri() == null || group.getHealthCheck().getUri().isEmpty())) {
            context.error(group.getId(), MetaType.GROUP, ERROR_TYPE, "Field `health-check` is missing `uri` value.");
        }

        try {
            validateGroupOnVses(group.getGroupVirtualServers());
        } catch (ValidationException e) {
            context.error(group.getId(), MetaType.GROUP, ERROR_TYPE, e.getMessage());
        }

        try {
            validateMembers(group.getGroupServers());
        } catch (ValidationException e) {
            context.error(group.getId(), MetaType.GROUP, ERROR_TYPE, e.getMessage());
        }
    }

    private void validateGroupOnVses(List<GroupVirtualServer> groupOnVses) throws ValidationException {
        if (groupOnVses == null || groupOnVses.size() == 0)
            throw new ValidationException("Group is missing `group-virtual-server` field.");

        Long[] vsIds = new Long[groupOnVses.size()];
        for (int i = 0; i < groupOnVses.size(); i++) {
            GroupVirtualServer e = groupOnVses.get(i);
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

        vsEntryQuery.getLocationEntriesByVs(vsIds);
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
