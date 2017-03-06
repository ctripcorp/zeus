package com.ctrip.zeus.service.model.validation;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.PathRewriteParser;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.common.ErrorType;
import com.ctrip.zeus.service.model.common.LocationEntry;
import com.ctrip.zeus.service.model.common.MetaType;
import com.ctrip.zeus.service.model.common.ValidationContext;
import com.ctrip.zeus.service.query.TrafficPolicyQuery;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2015/6/29.
 */
@Component("groupModelValidator")
public class DefaultGroupValidator implements GroupValidator {
    @Resource
    private RGroupVgDao rGroupVgDao;
    @Resource
    private RGroupStatusDao rGroupStatusDao;
    @Resource
    private GroupDao groupDao;
    @Resource
    private TrafficPolicyQuery trafficPolicyQuery;
    @Resource
    private PathValidator pathValidator;

    @Override
    public void validateFields(Group group, ValidationContext context) {
        if (group.getName() == null || group.getName().isEmpty()
                || group.getAppId() == null || group.getAppId().isEmpty()) {
            context.error(group.getId(), MetaType.GROUP, ErrorType.FIELD_VALIDATION, "Field `name` and `app-id` are not allowed empty.");
            return;
        }

        if (group.getHealthCheck() != null
                && (group.getHealthCheck().getUri() == null || group.getHealthCheck().getUri().isEmpty())) {
            context.error(group.getId(), MetaType.GROUP, ErrorType.FIELD_VALIDATION, "Field `health-check` is missing `uri` value.");
            return;
        }

        try {
            if (group.isVirtual() && (group.getGroupServers() != null && group.getGroupServers().size() > 0)) {
                context.error(group.getId(), MetaType.GROUP, ErrorType.FIELD_VALIDATION, "Field `group-servers` is not allowed if group is virtual type.");
                return;
            }
            validateMembers(group.getGroupServers());
        } catch (ValidationException e) {
            context.error(group.getId(), MetaType.GROUP, ErrorType.FIELD_VALIDATION, e.getMessage());
        }
    }

    @Override
    public boolean exists(Long targetId, boolean virtual) throws Exception {
        boolean result = groupDao.findById(targetId, GroupEntity.READSET_FULL) != null;
        RelGroupVgDo value = rGroupVgDao.findByGroup(targetId, RGroupVgEntity.READSET_FULL);
        result &= (virtual ^ value == null);
        return result;
    }

    @Override
    public Map<Long, GroupVirtualServer> validateGroupOnVses(List<GroupVirtualServer> groupOnVses, boolean virtual) throws ValidationException {
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
            if (virtual && e.getRedirect() == null) {
                throw new ValidationException("Field `redirect` is not allowed empty if group is virtual type.");
            }
        }
        return result;
    }

    @Override
    public void validateMembers(List<GroupServer> servers) throws ValidationException {
        if (servers == null || servers.size() == 0) return;
        Set<String> uniqueServerCheck = new HashSet<>();
        for (GroupServer s : servers) {
            if (s.getIp() == null || s.getIp().isEmpty() || s.getPort() == null) {
                throw new ValidationException("Group server ip and port cannot be null.");
            }
            if (!uniqueServerCheck.add(s.getIp() + ":" + s.getPort())) {
                throw new ValidationException("Duplicate combination of ip and port " + s.getIp() + ":" + s.getPort() + " is found in group server list.");
            }
        }
    }

    @Override
    public void validatePolicyRestriction(Map<Long, GroupVirtualServer> groupOnVses, List<LocationEntry> policyEntries) throws ValidationException {
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
            if (!pathValidator.contains(gvs.getPath(), e.getPath())) {
                throw new ValidationException("Traffic policy " + e.getEntryId() + " is neither sharing the same `path` with nor containing part of the `path` of the current group on vs " + vsId + ".");
            }
        }
    }

    @Override
    public void checkRestrictionForUpdate(Group target) throws Exception {
        RelGroupStatusDo check = rGroupStatusDao.findByGroup(target.getId(), RGroupStatusEntity.READSET_FULL);
        RelGroupVgDo value = rGroupVgDao.findByGroup(target.getId(), RGroupVgEntity.READSET_FULL);
        if (check == null || (target.isVirtual() == (value == null))) {
            throw new ValidationException("Group that you try to update does not exist.");
        }
        check = rGroupStatusDao.findByGroup(target.getId(), RGroupStatusEntity.READSET_FULL);
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
            throw new ValidationException("Group that you try to delete is still active.");
        }
        Set<IdVersion> keys = trafficPolicyQuery.queryByGroupId(targetId);
        Set<Long> _policyIds = new HashSet<>();
        for (IdVersion key : keys) {
            _policyIds.add(key.getId());
        }
        keys.retainAll(trafficPolicyQuery.queryByIdsAndMode(_policyIds.toArray(new Long[_policyIds.size()]), SelectionMode.OFFLINE_FIRST));
        if (keys.size() > 0) {
            throw new ValidationException("Group that you try to delete has one or more traffic policy dependency.");
        }
    }
}