package com.ctrip.zeus.service.model.common;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zhoumy on 2017/1/25.
 */
public class ValidationContext {
    private Map<MetaType, Error> errors = new HashMap<>();
    private AtomicBoolean success = new AtomicBoolean(true);

    public void error(Long entryId, MetaType entryType, String errorType, String cause) {
        success.set(false);
        Error ev = errors.get(entryType);
        if (ev == null) {
            ev = new Error();
            errors.put(entryType, ev);
        }
        ev.report(entryId, errorType, cause);
    }

    public Set<String> getGroupErrorType(Long groupId) {
        return getErrorTypeByEntry(groupId, MetaType.GROUP);
    }

    public Set<String> getVsErrorType(Long vsId) {
        return getErrorTypeByEntry(vsId, MetaType.VS);
    }

    public Set<String> getSlbErrorType(Long slbId) {
        return getErrorTypeByEntry(slbId, MetaType.SLB);
    }

    public Set<String> getPolicyErrorType(Long policyId) {
        return getErrorTypeByEntry(policyId, MetaType.TRAFFIC_POLICY);
    }

    private Set<String> getErrorTypeByEntry(Long entryId, MetaType entryType) {
        Set<String> result = new HashSet<>();
        for (Error.Node n : errors.get(entryType).getErrorMessage(entryId).getFlattenNodes()) {
            result.add(n.getErrorType());
        }
        return result;
    }

    public String getGroupErrorReason(Long groupId) {
        return errors.get(MetaType.GROUP).getErrorMessage(groupId).toString();
    }

    public String getPolicyErrorReason(Long policyId) {
        return errors.get(MetaType.TRAFFIC_POLICY).getErrorMessage(policyId).toString();
    }

    public String getVsErrorReason(Long vsId) {
        return errors.get(MetaType.VS).getErrorMessage(vsId).toString();
    }

    public String getSlbErrorReason(Long slbId) {
        return errors.get(MetaType.SLB).getErrorMessage(slbId).toString();
    }

    public Set<Long> getErrorGroups() {
        return getErrorEntries(MetaType.GROUP);
    }

    public Set<Long> getErrorPolicies() {
        return getErrorEntries(MetaType.TRAFFIC_POLICY);
    }

    public Set<Long> getErrorVses() {
        return getErrorEntries(MetaType.VS);
    }

    public Set<Long> getErrorSlbs() {
        return getErrorEntries(MetaType.SLB);
    }

    private Set<Long> getErrorEntries(MetaType type) {
        Error n = errors.get(type);
        if (n == null) return new HashSet<>();
        return n.getErrorIds();
    }

    public Map<String, String> getErrors() {
        int size = 0;
        for (Error error : errors.values()) {
            size += error.size();
        }
        Map<String, String> errorMessage = new HashMap<>(size);
        for (Map.Entry<MetaType, Error> e : errors.entrySet()) {
            for (Map.Entry<Long, String> ee : e.getValue().listErrors().entrySet()) {
                errorMessage.put(e.getKey().toString() + "-" + ((ee.getKey() == null || ee.getKey().equals(0L)) ? "new" : ee.getKey()), ee.getValue());
            }
        }
        return errorMessage;
    }

    public boolean shouldProceed() {
        return success.get();
    }
}
