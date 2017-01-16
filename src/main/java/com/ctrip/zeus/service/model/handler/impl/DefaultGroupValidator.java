package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupServer;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.PathRewriteParser;
import com.ctrip.zeus.service.model.PathValidator;
import com.ctrip.zeus.service.model.common.MetaType;
import com.ctrip.zeus.service.model.handler.GroupServerValidator;
import com.ctrip.zeus.service.model.handler.GroupValidator;
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
    public void checkVersionForUpdate(Group target) throws Exception {
        RelGroupStatusDo check = rGroupStatusDao.findByGroup(target.getId(), RGroupStatusEntity.READSET_FULL);
        if (check == null) {
            throw new ValidationException("Group that you tries to update does not exist.");
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
        validateGroupVirtualServers(target.getId(), target.getGroupVirtualServers(), escapePathValidation);
        validateGroupServers(target.getGroupServers());
    }

    @Override
    public void validateGroupVirtualServers(Long groupId, List<GroupVirtualServer> groupVirtualServers, boolean escapePathValidation) throws Exception {
        if (groupVirtualServers == null || groupVirtualServers.size() == 0)
            throw new ValidationException("Group is missing `group-virtual-server` field.");
        if (groupId == null) groupId = 0L;

        Long[] vsIds = new Long[groupVirtualServers.size()];
        for (int i = 0; i < groupVirtualServers.size(); i++) {
            GroupVirtualServer e = groupVirtualServers.get(i);
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

        Map<Long, List<PathValidator.LocationEntry>> currentLocationEntriesByVs = new HashMap<>();
        compareAndBuildCurrentLocationEntries(groupId, vsIds, groupVirtualServers, escapePathValidation ? null : currentLocationEntriesByVs);

        // reformat path if validation is processed
        // fulfill priority if auto-reorder("priority" : null) is enabled
        for (GroupVirtualServer e : groupVirtualServers) {
            Long vsId = e.getVirtualServer().getId();
            e.setVirtualServer(new VirtualServer().setId(vsId));
            if (escapePathValidation) {
                if (e.getPriority() == null) {
                    throw new ValidationException("Field `priority` cannot be empty if validation is escaped.");
                }
            } else {
                PathValidator.LocationEntry insertEntry = new PathValidator.LocationEntry().setEntryId(groupId).setEntryType(MetaType.GROUP).setVsId(vsId).setPath(e.getPath()).setPriority(e.getPriority());
                pathValidator.checkOverlapRestriction(vsId, insertEntry, currentLocationEntriesByVs.get(vsId));
                if (e.getPriority() == null) {
                    // auto reorder and reformat
                    e.setPriority(insertEntry.getPriority());
                    e.setPath(insertEntry.getPath());
                } else {
                    // check priority and reformat
                    if (!e.getPriority().equals(insertEntry.getPriority())) {
                        throw new ValidationException("Group that you tries to create/modify may cause path prefix-overlap problem with other entries on virtual-server " + vsId + ". Recommend priority will be " + insertEntry.getPriority() + ".");
                    }
                    e.setPath(insertEntry.getPath());
                }
            }
        }
    }

    private void compareAndBuildCurrentLocationEntries(Long groupId, Long[] vsIds, List<GroupVirtualServer> groupVirtualServers, Map<Long, List<PathValidator.LocationEntry>> currentLocationEntriesByVs) throws DalException, ValidationException {
        List<Long> relatedPolicies = new ArrayList<>();
        for (RTrafficPolicyGroupDo e : rTrafficPolicyGroupDao.findAllByGroups(new Long[]{groupId}, RTrafficPolicyGroupEntity.READSET_FULL)) {
            relatedPolicies.add(e.getPolicyId());
        }

        Integer[] priorities = new Integer[vsIds.length];
        String[] paths = new String[vsIds.length];
        for (GroupVirtualServer e : groupVirtualServers) {
            int i = Arrays.binarySearch(vsIds, e.getVirtualServer().getId());
            priorities[i] = e.getPriority();
            paths[i] = e.getPath();
        }

        for (RTrafficPolicyVsDo e : rTrafficPolicyVsDao.findByVsesAndPolicyVersion(vsIds, RTrafficPolicyVsEntity.READSET_FULL)) {
            if (relatedPolicies.contains(e.getPolicyId())) {
                int i = Arrays.binarySearch(vsIds, e.getVsId());
                if (priorities[i] != null && priorities[i] > e.getPriority()) {
                    throw new ValidationException("Group has higher `priority` than its traffic policy " + e.getPolicyId() + " on vs " + e.getVsId() + ".");
                }
                if (!paths[i].equals(e.getPath())) {
                    throw new ValidationException("Group has different `path` from its traffic policy " + e.getPolicyId() + " on vs " + e.getVsId() + ".");
                }
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

        if (currentLocationEntriesByVs != null) {
            for (RelGroupVsDo e : rGroupVsDao.findByVsesAndGroupOfflineVersion(vsIds, RGroupVsEntity.READSET_FULL)) {
                if (e.getGroupId() == groupId) continue;
                List<PathValidator.LocationEntry> v = currentLocationEntriesByVs.get(e.getVsId());
                if (v == null) {
                    v = new ArrayList<>();
                    currentLocationEntriesByVs.put(e.getVsId(), v);
                }
                v.add(new PathValidator.LocationEntry().setVsId(e.getVsId()).setEntryId(e.getGroupId()).setPath(e.getPath()).setEntryType(MetaType.GROUP).setPriority(e.getPriority() == 0 ? 1000 : e.getPriority()));
            }
        }
    }

    @Override
    public void validateGroupServers(List<GroupServer> groupServers) throws Exception {
        groupServerModelValidator.validateGroupServers(groupServers);
    }
}