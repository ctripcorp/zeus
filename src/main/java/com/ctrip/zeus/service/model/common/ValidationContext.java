package com.ctrip.zeus.service.model.common;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zhoumy on 2017/1/25.
 */
public class ValidationContext {
    private Map<Long, String> errorGroups = new HashMap<>();
    private Map<Long, String> errorPolicies = new HashMap<>();
    private AtomicBoolean success = new AtomicBoolean(true);

    public void error(Long entryId, MetaType entryType, String errorType, String cause) {
        success.set(false);
        if (MetaType.GROUP.equals(entryType)) {
            errorGroups.put(entryId, errorType + "-" + cause);
        } else if (MetaType.TRAFFIC_POLICY.equals(entryType)) {
            errorPolicies.put(entryId, errorType + "-" + cause);
        }
    }

    public Set<Long> getErrorGroups() {
        return errorGroups.keySet();
    }

    public String getGroupErrorReason(Long groupId) {
        return errorGroups.get(groupId);
    }

    public Set<Long> getErrorPolicies() {
        return errorPolicies.keySet();
    }

    public Map<String, String> getErrors() {
        Map<String, String> errors = new HashMap<>(errorGroups.size() + errorPolicies.size());
        for (Map.Entry<Long, String> e : errorGroups.entrySet()) {
            errors.put(MetaType.GROUP.toString() + "-" + e.getKey(), e.getValue());
        }
        for (Map.Entry<Long, String> e : errorPolicies.entrySet()) {
            errors.put(MetaType.TRAFFIC_POLICY.toString() + "-" + e.getKey(), e.getValue());
        }
        return errors;
    }

    public boolean shouldProceed() {
        return success.get();
    }
}
