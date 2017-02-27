package com.ctrip.zeus.service.model.validation;

import com.ctrip.zeus.dal.core.TrafficPolicyDao;
import com.ctrip.zeus.dal.core.TrafficPolicyDo;
import com.ctrip.zeus.dal.core.TrafficPolicyEntity;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.PolicyVirtualServer;
import com.ctrip.zeus.model.entity.TrafficControl;
import com.ctrip.zeus.model.entity.TrafficPolicy;
import com.ctrip.zeus.service.model.common.ErrorType;
import com.ctrip.zeus.service.model.common.LocationEntry;
import com.ctrip.zeus.service.model.common.MetaType;
import com.ctrip.zeus.service.model.common.ValidationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2017/2/6.
 */
@Service("trafficPolicyValidator")
public class DefaultTrafficPolicyValidator implements TrafficPolicyValidator {
    @Resource
    private TrafficPolicyDao trafficPolicyDao;
    @Resource
    private PathValidator pathValidator;

    @Override
    public void checkRestrictionForUpdate(TrafficPolicy target) throws Exception {
        TrafficPolicyDo e = trafficPolicyDao.findById(target.getId(), TrafficPolicyEntity.READSET_FULL);
        if (e == null) throw new ValidationException("Traffic policy that you try to update does not exist.");
        if (e.getVersion() > target.getVersion()) {
            throw new ValidationException("Newer offline version is detected.");
        }
        if (e.getVersion() != target.getVersion()) {
            throw new ValidationException("Mismatched offline version is detected.");
        }
    }

    @Override
    public void removable(Long targetId) throws Exception {
        if (trafficPolicyDao.findById(targetId, TrafficPolicyEntity.READSET_FULL).getActiveVersion() > 0) {
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
        for (TrafficControl c : policy.getControls()) {
            if (c.getWeight() <= 0) {
                throw new ValidationException("Field `weight` only accepts positive value.");
            }
        }
        return groupIds;
    }

    @Override
    public Map<Long, PolicyVirtualServer> validatePolicyOnVses(Long policyId, Long[] controlIds, Map<Long, List<LocationEntry>> groupEntries, List<PolicyVirtualServer> policyOnVses, Map<Long, List<LocationEntry>> groupRelatedPolicyEntries, ValidationContext context) throws ValidationException {
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
            List<LocationEntry> entriesOnVs = groupEntries.get(vsId);
            if (entriesOnVs == null || entriesOnVs.size() == 0) {
                String error = "Traffic policy that you try to create/modify does not have enough traffic-controls.";
                _context.error(policyId, MetaType.TRAFFIC_POLICY, ErrorType.FIELD_VALIDATION, error);
                if (context == null) throw new ValidationException(error);
            }
            for (LocationEntry ee : entriesOnVs) {
                int j = Arrays.binarySearch(controlIds, ee.getEntryId());
                if (j < 0) continue;
                visited[j] = i;
                if (e.getPriority() <= ee.getPriority()) {
                    String error = "Traffic policy has lower `priority` than its control item " + controlIds[j] + " on vs " + vsId + ".";
                    _context.error(policyId, MetaType.TRAFFIC_POLICY, ErrorType.DEPENDENCY_VALIDATION, error);
                    _context.error(controlIds[j], MetaType.GROUP, ErrorType.DEPENDENCY_VALIDATION, error);
                    if (context == null) throw new ValidationException(error);
                }


                if (!pathValidator.contains(ee.getPath(), e.getPath())) {
                    String error = "Traffic policy is neither sharing the same `path` with nor containing part of the `path` of its control item " + controlIds[j] + " on vs " + vsId + ".";
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
}
