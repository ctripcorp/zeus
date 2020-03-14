package com.ctrip.zeus.service.model.validation;

import com.ctrip.zeus.dao.entity.SlbTrafficPolicy;
import com.ctrip.zeus.dao.mapper.SlbTrafficPolicyMapper;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.common.ErrorType;
import com.ctrip.zeus.service.model.common.LocationEntry;
import com.ctrip.zeus.service.model.common.MetaType;
import com.ctrip.zeus.service.model.common.ValidationContext;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.TrafficPolicyQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2017/2/6.
 */
@Service("trafficPolicyValidator")
public class DefaultTrafficPolicyValidator implements TrafficPolicyValidator {

    @Resource
    private SlbTrafficPolicyMapper slbTrafficPolicyMapper;

    @Resource
    private PathValidator pathValidator;
    @Resource
    private TrafficPolicyQuery trafficPolicyQuery;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Autowired
    private GroupRepository groupRepository;


    public static final String RELATED_GROUP_OPS_REMOVE_GVS = "REMOVE_GVS";
    public static final String RELATED_GROUP_OPS_DEACTIVATE = "DEACTIVATE";

    @Override
    public void checkRestrictionForUpdate(TrafficPolicy target) throws Exception {
        SlbTrafficPolicy e = slbTrafficPolicyMapper.selectByPrimaryKey(target.getId());
        if (e == null) throw new ValidationException("Traffic policy that you try to update does not exist.");
        if (e.getVersion() > target.getVersion()) {
            throw new ValidationException("Newer offline version is detected.");
        }
        if (!e.getVersion().equals(target.getVersion())) {
            throw new ValidationException("Mismatched offline version is detected.");
        }
    }

    @Override
    public void removable(Long targetId) throws Exception {
        if (slbTrafficPolicyMapper.selectByPrimaryKey(targetId).getActiveVersion() > 0) {
            throw new ValidationException("Traffic policy that you try to delete is still active.");
        }
    }

    @Override
    public void validateFields(TrafficPolicy policy, ValidationContext context) {
        if (policy.getName() == null) {
            context.error(policy.getId(), MetaType.TRAFFIC_POLICY, ErrorType.FIELD_VALIDATION, "Field `name` is empty.");
        }
        if (policy.getPolicyVirtualServers() == null || policy.getPolicyVirtualServers().size() == 0) {
            context.error(policy.getId(), MetaType.TRAFFIC_POLICY, ErrorType.FIELD_VALIDATION, "Field `policy-virtual-servers` is empty.");
        }
    }

    @Override
    public  Map<Long,String> validatedForDeactivate(TrafficPolicy policy) throws Exception {
        Long savedGroupId = null;
        List<Long> groups = new ArrayList<>();
        Map<Long,String> relatedGroupOpsType= new HashMap<>();
        // 1. weight check
        for (TrafficControl c : policy.getControls()) {
            if (c.getWeight() > 0) {
                if (savedGroupId != null) {
                    throw new ValidationException("Policy has multi groups which have the weight large then 0.GroupId:" + savedGroupId);
                } else {
                    savedGroupId = c.getGroup().getId();
                }
            }
            groups.add(c.getGroup().getId());
        }
        // 2. multi policies check
        for (Long gid : groups) {
            Set<IdVersion> ids = trafficPolicyQuery.queryByGroupId(gid);
            for (IdVersion idv : ids) {
                if (!idv.getId().equals(policy.getId())) {
                    throw new ValidationException("Policy groups has multi policies. Policy:" + idv.getId());
                }
            }
        }
        // 3. path & priority check
        groups.remove(savedGroupId);
        Set<IdVersion> groupIdvs = groupCriteriaQuery.queryByIdsAndMode(groups.toArray(new Long[groups.size()]), SelectionMode.ONLINE_FIRST);
        List<Group> groupList = groupRepository.list(groupIdvs.toArray(new IdVersion[groupIdvs.size()]));
        Map<Long, PolicyVirtualServer> policyVses = new HashMap<>();
        for (PolicyVirtualServer pvs : policy.getPolicyVirtualServers()) {
            policyVses.put(pvs.getVirtualServer().getId(), pvs);
        }
        for (Group group : groupList) {
            if (group.getId().equals(savedGroupId)) continue;
            Map<Long, GroupVirtualServer> groupVirtualServerMap = new HashMap<>();
            for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
                groupVirtualServerMap.put(gvs.getVirtualServer().getId(), gvs);
            }
            if (policyVses.size() == groupVirtualServerMap.size() && policyVses.keySet().containsAll(groupVirtualServerMap.keySet())) {
                for (Long vsId : policyVses.keySet()) {
                    String policyPath = policyVses.get(vsId).getPath();
                    String groupPath = groupVirtualServerMap.get(vsId).getPath();
                    if (!pathValidator.contains(policyPath, groupPath)) {
                        throw new ValidationException("Group's path contains policy's path.Policy:" + policy.getId() + ",path:" + policyPath
                                + ";Group:" + group.getId() + ",path:" + groupPath);
                    }
                }
                relatedGroupOpsType.put(group.getId(), RELATED_GROUP_OPS_DEACTIVATE);
            } else {
                relatedGroupOpsType.put(group.getId(), RELATED_GROUP_OPS_REMOVE_GVS);
                //throw new ValidationException("Group have more virtual servers than policy.Policy:" + policy.getId() + ";Group:" + group.getId());
            }
        }

        return relatedGroupOpsType;
    }

    @Override
    public Long[] validatePolicyControl(TrafficPolicy policy) throws ValidationException {
        Long[] groupIds = new Long[policy.getControls().size()];
        for (int i = 0; i < policy.getControls().size(); i++) {
            groupIds[i] = policy.getControls().get(i).getGroup().getId();
        }
        if (groupIds.length < 1) {
            throw new ValidationException("Traffic policy that you try to create/modify does not have enough traffic-controls.");
        }
        Arrays.sort(groupIds);
        Long prev = groupIds[0];
        for (int i = 1; i < groupIds.length; i++) {
            if (prev.equals(groupIds[i])) {
                throw new ValidationException("Traffic policy that you try to create/modify declares the same group " + prev + " more than once.");
            }
            prev = groupIds[i];
        }
        double totalWeight = 0.0;
        for (TrafficControl c : policy.getControls()) {
            if (c.getWeight() < 0) {
                throw new ValidationException("Field `weight` only accepts positive value.");
            }
            totalWeight += c.getWeight();
        }
        if (totalWeight == 0) {
            throw new ValidationException("Sum of field `weight` can not be zero.");
        }
        return groupIds;
    }

    @Override
    public Map<Long, PolicyVirtualServer> validatePolicyOnVses(Long policyId, Long[] controlIds, Map<Long, List<LocationEntry>> groupEntries, List<PolicyVirtualServer> policyOnVses, Map<Long, List<LocationEntry>> groupRelatedPolicyEntries, ValidationContext context) throws ValidationException {
        ValidationContext validationContext = context == null ? new ValidationContext() : context;

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
                        validationContext.error(policyId, MetaType.TRAFFIC_POLICY, ErrorType.DEPENDENCY_VALIDATION, error);
                        validationContext.error(ee.getEntryId(), MetaType.TRAFFIC_POLICY, ErrorType.DEPENDENCY_VALIDATION, error);
                        if (context == null) throw new ValidationException(error);
                    }
                }
            }
            i++;
            List<LocationEntry> entriesOnVs = groupEntries.get(vsId);
            if (entriesOnVs == null || entriesOnVs.size() == 0) {
                String error = "Traffic policy that you try to create/modify does not have enough traffic-controls.";
                validationContext.error(policyId, MetaType.TRAFFIC_POLICY, ErrorType.FIELD_VALIDATION, error);
                if (context == null) throw new ValidationException(error);
            }
            for (LocationEntry ee : entriesOnVs) {
                int j = Arrays.binarySearch(controlIds, ee.getEntryId());
                if (j < 0) continue;
                visited[j] = i;
                if (e.getPriority() <= ee.getPriority()) {
                    String error = "Traffic policy has lower `priority` than its control item " + controlIds[j] + " on vs " + vsId + ".";
                    validationContext.error(policyId, MetaType.TRAFFIC_POLICY, ErrorType.DEPENDENCY_VALIDATION, error);
                    validationContext.error(controlIds[j], MetaType.GROUP, ErrorType.DEPENDENCY_VALIDATION, error);
                    if (context == null) throw new ValidationException(error);
                }


                if (!pathValidator.contains(ee.getPath(), e.getPath())) {
                    String error = "Traffic policy is neither sharing the same `path` with nor containing part of the `path` of its control item " + controlIds[j] + " on vs " + vsId + ".";
                    validationContext.error(policyId, MetaType.TRAFFIC_POLICY, ErrorType.DEPENDENCY_VALIDATION, error);
                    validationContext.error(controlIds[j], MetaType.GROUP, ErrorType.DEPENDENCY_VALIDATION, error);
                    if (context == null) throw new ValidationException(error);
                }
            }
            for (int k = 0; k < visited.length; k++) {
                if (visited[k] != i) {
                    String error = "Group " + controlIds[k] + " is missing combination on vs " + vsId + ".";
                    validationContext.error(policyId, MetaType.TRAFFIC_POLICY, ErrorType.DEPENDENCY_VALIDATION, error);
                    validationContext.error(controlIds[k], MetaType.GROUP, ErrorType.DEPENDENCY_VALIDATION, error);
                    if (context == null) throw new ValidationException(error);
                }
            }
            PolicyVirtualServer prev = result.put(vsId, e);
            if (prev != null) {
                String error = "Traffic policy can have and only have one combination to the same virtual-server. \"vs-id\" : " + e.getVirtualServer().getId() + ".";
                validationContext.error(policyId, MetaType.TRAFFIC_POLICY, ErrorType.DEPENDENCY_VALIDATION, error);
                if (context == null) throw new ValidationException(error);
            }
        }
        return result;
    }
}
