package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupServer;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.PathRewriteParser;
import com.ctrip.zeus.service.model.PathValidator;
import com.ctrip.zeus.service.model.handler.GroupServerValidator;
import com.ctrip.zeus.service.model.handler.GroupValidator;
import com.ctrip.zeus.service.model.handler.VirtualServerValidator;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2015/6/29.
 */
@Component("groupModelValidator")
public class DefaultGroupValidator implements GroupValidator {
    @Resource
    private VirtualServerValidator virtualServerModelValidator;
    @Resource
    private GroupServerValidator groupServerModelValidator;
    @Resource
    private PathValidator pathValidator;
    @Resource
    private RGroupVsDao rGroupVsDao;
    @Resource
    private RGroupVgDao rGroupVgDao;
    @Resource
    private RGroupStatusDao rGroupStatusDao;
    @Resource
    private RTrafficPolicyGroupDao rTrafficPolicyGroupDao;
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
    public void checkVersion(Group target) throws Exception {
        RelGroupStatusDo check = rGroupStatusDao.findByGroup(target.getId(), RGroupStatusEntity.READSET_FULL);
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

        Map<Long, PathValidator.LocationEntry> groupEntriesByVs = validateAndBuildLocationEntry(groupId, groupVirtualServers, escapePathValidation);

        if (escapePathValidation || groupEntriesByVs.size() == 0) return;


        Map<Long, List<PathValidator.LocationEntry>> retainedGroupEntriesByVs = new HashMap<>();
        for (RelGroupVsDo e : rGroupVsDao.findAllByVses(groupEntriesByVs.keySet().toArray(new Long[groupEntriesByVs.size()]), RGroupVsEntity.READSET_FULL)) {
            List<PathValidator.LocationEntry> entryList = retainedGroupEntriesByVs.get(e.getVsId());
            if (entryList == null) {
                entryList = new ArrayList<>();
                retainedGroupEntriesByVs.put(e.getVsId(), entryList);
            }
            entryList.add(new PathValidator.LocationEntry().setVsId(e.getVsId()).setEntryId(e.getGroupId()).setPath(e.getPath()).setPriority(e.getPriority() == 0 ? 1000 : e.getPriority()));
        }

        for (Map.Entry<Long, PathValidator.LocationEntry> e : groupEntriesByVs.entrySet()) {
            pathValidator.checkOverlapRestriction(e.getKey(), e.getValue(), retainedGroupEntriesByVs.get(e.getKey()));
        }

        // reset values if auto-reorder("priority" : null) is enabled
        for (GroupVirtualServer e : groupVirtualServers) {
            Long vsId = e.getVirtualServer().getId();
            e.setVirtualServer(new VirtualServer().setId(vsId));
            PathValidator.LocationEntry ref = groupEntriesByVs.get(vsId);
            e.setPath(ref.getPath());
            if (e.getPriority() == null) {
                e.setPriority(ref.getPriority());
            } else if (!e.getPriority().equals(ref.getPriority())) {
                throw new ValidationException("Group that you tries to create/modify may cause path prefix-overlap problem with other groups on virtual-server " + e.getVirtualServer().getId() + ". Recommend priority will be " + ref + ".");
            }
        }
    }

    private Map<Long, PathValidator.LocationEntry> validateAndBuildLocationEntry(Long groupId, List<GroupVirtualServer> groupVirtualServers, boolean escapePathValidation) throws Exception {
        Map<Long, PathValidator.LocationEntry> groupEntriesByVs = new HashMap<>();
        for (GroupVirtualServer gvs : groupVirtualServers) {
            if (gvs.getRewrite() != null && !gvs.getRewrite().isEmpty()) {
                if (!PathRewriteParser.validate(gvs.getRewrite())) {
                    throw new ValidationException("Invalid `rewrite` field value. \"rewrite\" : " + gvs.getRewrite() + ".");
                }
            }

            VirtualServer currentVs = gvs.getVirtualServer();
            if (!virtualServerModelValidator.exists(currentVs.getId())) {
                throw new ValidationException("Field `group-virtual-server` is requesting a combination to an non-existing virtual-server. \"vs-id\" : " + currentVs.getId() + ".");
            }
            if (groupEntriesByVs.containsKey(currentVs.getId())) {
                throw new ValidationException("Group can have and only have one combination to the same virtual-server. \"vs-id\" : " + currentVs.getId() + ".");
            } else if (!escapePathValidation) {
                PathValidator.LocationEntry le = new PathValidator.LocationEntry().setVsId(currentVs.getId()).setEntryId(groupId).setPath(gvs.getPath()).setPriority(gvs.getPriority());
                groupEntriesByVs.put(currentVs.getId(), le);
            }
        }
        return groupEntriesByVs;
    }

    @Override
    public void validateGroupServers(List<GroupServer> groupServers) throws Exception {
        groupServerModelValidator.validateGroupServers(groupServers);
    }
}