package com.ctrip.zeus.service.model.validation;

import com.ctrip.zeus.dao.entity.SlbArchiveGroup;
import com.ctrip.zeus.dao.entity.SlbArchiveGroupExample;
import com.ctrip.zeus.dao.entity.SlbGroupStatusR;
import com.ctrip.zeus.dao.entity.SlbGroupStatusRExample;
import com.ctrip.zeus.dao.mapper.SlbArchiveGroupMapper;
import com.ctrip.zeus.dao.mapper.SlbGroupStatusRMapper;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.model.model.Group;
import com.ctrip.zeus.model.model.GroupServer;
import com.ctrip.zeus.model.model.GroupVirtualServer;
import com.ctrip.zeus.model.model.Rule;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.PathRewriteParser;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.common.ErrorType;
import com.ctrip.zeus.service.model.common.LocationEntry;
import com.ctrip.zeus.service.model.common.MetaType;
import com.ctrip.zeus.service.model.common.ValidationContext;
import com.ctrip.zeus.service.model.handler.impl.ContentReaders;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.TrafficPolicyQuery;
import com.ctrip.zeus.service.rule.model.RuleType;
import com.ctrip.zeus.tag.PropertyNames;
import com.ctrip.zeus.tag.PropertyService;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2015/6/29.
 */
@Component("groupModelValidator")
public class DefaultGroupValidator implements GroupValidator {

    @Resource
    private SlbGroupStatusRMapper slbGroupStatusRMapper;

    @Resource
    private SlbArchiveGroupMapper slbArchiveGroupMapper;

    @Resource
    private TrafficPolicyQuery trafficPolicyQuery;
    @Resource
    private PathValidator pathValidator;

    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private PropertyService propertyService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

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

        try {
            validateGroupVirtualServers(group.getGroupVirtualServers(), group);
        } catch (ValidationException e) {
            context.error(group.getId(), MetaType.GROUP, ErrorType.FIELD_VALIDATION, e.getMessage());
        }
    }

    @Override
    public void validateCanaryVersion(Group group) throws ValidationException {
        IdVersion[] canaryIdv = new IdVersion[0];
        try {
            canaryIdv = groupCriteriaQuery.queryByIdAndMode(group.getId(), SelectionMode.CANARY);
        } catch (Exception e) {
            logger.warn("Get Group Canary Version Failed.", e);
        }
        if (canaryIdv.length > 0) {
            throw new ValidationException("Group Has Canary Version. Please Activate Group First.");
        }
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
            if (!InetAddressValidator.getInstance().isValidInet4Address(s.getIp())) {
                throw new ValidationException("Group server ip is invalidated.");
            }
            if (!uniqueServerCheck.add(s.getIp() + ":" + s.getPort())) {
                throw new ValidationException("Duplicate combination of ip and port " + s.getIp() + ":" + s.getPort() + " is found in group server list.");
            }
        }
    }

    private void validateGroupVirtualServers(List<GroupVirtualServer> groupVirtualServers, Group group) throws ValidationException {
        if (groupVirtualServers == null || groupVirtualServers.size() == 0) return;
        List<Long> ids = new ArrayList<>();
        for (GroupVirtualServer gvs : groupVirtualServers) {
            if (gvs.getVirtualServer() == null || gvs.getVirtualServer().getId() == null) {
                throw new ValidationException("Virtual server of a GVS cannot be null.");
            }
            ids.add(gvs.getVirtualServer().getId());
            boolean isValidBinding = false;
            if (gvs.getPath() != null && !gvs.getPath().isEmpty()) {
                if (gvs.getPath().trim().equalsIgnoreCase("/")) {
                    throw new ValidationException(
                            "Path of a GVS cannot be \"/\". VS ID=" + gvs.getVirtualServer().getId());
                }
                isValidBinding = true;
            } else if (gvs.getRouteRules() != null && !gvs.getRouteRules().isEmpty()) {
                throw new ValidationException(
                        "Route rule is not allowed when path is not set. VS ID=" + gvs.getVirtualServer().getId());
            }
            if (gvs.getName() != null && !gvs.getName().isEmpty()) {
                if (gvs.getName().trim().charAt(0) != '@') {
                    throw new ValidationException("The name shall begin with '@'. Name=" + gvs.getName() + " VS ID="
                            + gvs.getVirtualServer().getId());
                }
                isValidBinding = true;
            }
            if (!isValidBinding) {
                throw new ValidationException("Invalid GVS binding. At least path or name shall be set in a GVS.");
            }
        }
        for (Rule rule : group.getRuleSet()) {
            if (RuleType.SHARDING_RULE.getName().equalsIgnoreCase(rule.getRuleType())) {
                Map<Long, Property> vsPropertyMap = new HashMap<>();
                try {
                    vsPropertyMap = propertyService.getProperties(PropertyNames.REGION_TRAFFIC_SHARDING, "vs", ids.toArray(new Long[0]));
                } catch (Exception e) {
                    logger.warn("Query Properties Failed.Pname:" + PropertyNames.REGION_TRAFFIC_SHARDING + ";Type:vs;Ids:" + ids.toString(), e);
                }
                if (!vsPropertyMap.keySet().containsAll(ids)) {
                    ids.removeAll(vsPropertyMap.keySet());
                    throw new ValidationException("Group Has Group Virtual Servers Not For Region Traffic Sharding.VsIds:" + ids.toString());
                }
                break;
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
        SlbGroupStatusR check = slbGroupStatusRMapper.selectOneByExampleSelective(new SlbGroupStatusRExample().createCriteria().andGroupIdEqualTo(target.getId()).example());
        if (check == null) {
            throw new ValidationException("Group that you try to update does not exist.");
        }

        if (check.getOfflineVersion() > target.getVersion()) {
            throw new ValidationException("Newer version is detected.");
        }
        if (!check.getOfflineVersion().equals(target.getVersion())) {
            throw new ValidationException("Incompatible version.");
        }

        SlbArchiveGroup offlineGroupArchive = slbArchiveGroupMapper.selectOneByExampleWithBLOBs(new SlbArchiveGroupExample().
                createCriteria().
                andGroupIdEqualTo(check.getGroupId()).
                andVersionEqualTo(check.getOfflineVersion()).
                example());
        if (offlineGroupArchive == null) {
            throw new ValidationException("Unable to validate the update due to the missing of current offline archive.");
        }
        Group offlineGroup;
        try {
            offlineGroup = ContentReaders.readGroupContent(offlineGroupArchive.getContent());
        } catch (Exception e) {
            throw new ValidationException("Failed to parse the current offline archive: " + e.getMessage());
        }
        if (offlineGroup.isVirtual() != target.isVirtual()) {
            throw new ValidationException("The virtual flag must not be changed during update.");
        }
    }

    @Override
    public void removable(Long targetId) throws Exception {
        SlbGroupStatusR check = slbGroupStatusRMapper.selectOneByExampleSelective(new SlbGroupStatusRExample().createCriteria().andGroupIdEqualTo(targetId).example(), SlbGroupStatusR.Column.onlineVersion);

        if (check == null) return;

        if (check.getOnlineVersion() != 0) {
            throw new ValidationException("Group that you try to delete is still active.");
        }
        Set<IdVersion> keys = trafficPolicyQuery.queryByGroupId(targetId);
        Set<Long> policyIds = new HashSet<>();
        for (IdVersion key : keys) {
            policyIds.add(key.getId());
        }
        keys.retainAll(trafficPolicyQuery.queryByIdsAndMode(policyIds.toArray(new Long[policyIds.size()]), SelectionMode.OFFLINE_FIRST));
        if (keys.size() > 0) {
            throw new ValidationException("Group that you try to delete has one or more traffic policy dependency.");
        }
    }
}
