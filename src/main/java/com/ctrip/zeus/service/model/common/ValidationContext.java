package com.ctrip.zeus.service.model.common;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zhoumy on 2017/1/25.
 */
public class ValidationContext {
    private Map<Long, String> errorGroups = new HashMap<>();
    private Map<Long, String> errorPolicies = new HashMap<>();
    private Map<Long, String> errorSlbs = new HashMap<>();
    private Map<Long, String> errorVses = new HashMap<>();
    private AtomicBoolean success = new AtomicBoolean(true);

    public void error(Long entryId, MetaType entryType, String errorType, String cause) {
        success.set(false);
        switch (entryType) {
            case GROUP:
                errorGroups.put(entryId, errorType + "-" + cause);
                break;
            case TRAFFIC_POLICY:
                errorPolicies.put(entryId, errorType + "-" + cause);
                break;
            case SLB:
                errorSlbs.put(entryId, errorType + "-" + cause);
                break;
            case VS:
                errorVses.put(entryId, errorType + "-" + cause);
                break;
        }
    }

    public Set<Long> getErrorGroups() {
        return errorGroups.keySet();
    }

    public String getGroupErrorReason(Long groupId) {
        return errorGroups.get(groupId);
    }

    public String getPolicyErrorReason(Long policyId) {
        return errorPolicies.get(policyId);
    }

    public Set<Long> getErrorPolicies() {
        return errorPolicies.keySet();
    }

    public Map<String, String> getErrors() {
        Map<String, String> errors = new HashMap<>(errorGroups.size() + errorPolicies.size() + errorVses.size() + errorSlbs.size());
        for (Map.Entry<Long, String> e : errorGroups.entrySet()) {
            errors.put(MetaType.GROUP.toString() + "-" + (e.getKey() == null ? "new" : e.getKey()), e.getValue());
        }
        for (Map.Entry<Long, String> e : errorPolicies.entrySet()) {
            errors.put(MetaType.TRAFFIC_POLICY.toString() + "-" + (e.getKey() == null ? "new" : e.getKey()), e.getValue());
        }
        for (Map.Entry<Long, String> e : errorVses.entrySet()) {
            errors.put(MetaType.VS.toString() + "-" + (e.getKey() == null ? "new" : e.getKey()), e.getValue());
        }
        for (Map.Entry<Long, String> e : errorSlbs.entrySet()) {
            errors.put(MetaType.SLB.toString() + "-" + (e.getKey() == null ? "new" : e.getKey()), e.getValue());
        }
        return errors;
    }

    public boolean shouldProceed() {
        return success.get();
    }
}
