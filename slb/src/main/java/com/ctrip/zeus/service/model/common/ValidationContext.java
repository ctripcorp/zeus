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

    public void error(Long entryId, MetaType entryType, Long relatedId, MetaType relatedType, String errorType, String cause) {
        success.set(false);
        Error ev = errors.get(entryType);
        if (ev == null) {
            ev = new Error();
            errors.put(entryType, ev);
        }
        ev.report(entryId, relatedId, relatedType, errorType, cause);
    }

    public Set<String> getGroupErrorType(Long groupId) {
        return getErrorTypeByEntry(groupId, MetaType.GROUP);
    }

    public Set<Long> getGroupRelatedIdsByErrorTypeAndId(Long groupId, String type) {
        List<RelatedItem> ids = getErrorRelatedEntryByType(groupId, MetaType.GROUP, type);
        Set<Long> result = new HashSet<>();
        for (RelatedItem p : ids) {
            result.add(p.getRelatedId());
        }
        return result;
    }

    public List<RelatedItem> getGroupErrorNodeByEntry(Long groupId, String type) {
        return getErrorRelatedEntryByType(groupId, MetaType.GROUP, type);
    }

    public List<RelatedItem> getVsErrorNodeByEntry(Long vsId, String type) {
        return getErrorRelatedEntryByType(vsId, MetaType.VS, type);
    }

    public List<RelatedItem> getSlbErrorNodeByEntry(Long slbId, String type) {
        return getErrorRelatedEntryByType(slbId, MetaType.SLB, type);
    }

    public List<RelatedItem> getPolicyErrorNodeByEntry(Long pid, String type) {
        return getErrorRelatedEntryByType(pid, MetaType.TRAFFIC_POLICY, type);
    }

    public List<RelatedItem> getDrErrorNodeByEntry(Long dr, String type) {
        return getErrorRelatedEntryByType(dr, MetaType.DR, type);
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

    public void ignoreGroupErrors(Long groupId, String type, MetaType rt, Long relatedId) {
        ignoreErrors(groupId, MetaType.GROUP, type, rt, relatedId);
    }

    public void ignoreGroupErrors(Long groupId, String type) {
        ignoreErrors(groupId, MetaType.GROUP, type);
    }

    public void ignorePolicyErrors(Long policyId, String type, MetaType rt, Long relatedId) {
        ignoreErrors(policyId, MetaType.TRAFFIC_POLICY, type, rt, relatedId);
    }

    public void ignorePolicyErrors(Long policyId, String type) {
        ignoreErrors(policyId, MetaType.TRAFFIC_POLICY, type);
    }

    private void ignoreErrors(Long entryId, MetaType entryType, String type, MetaType rt, Long relatedId) {
        if (errors.get(entryType) == null) return;
        errors.get(entryType).ignore(entryId, type, rt, relatedId);
        if (errors.get(entryType).isEmpty()) {
            errors.remove(entryType);
            if (errors.size() == 0) {
                success.set(true);
            }
        }
    }

    private void ignoreErrors(Long entryId, MetaType entryType, String type) {
        if (errors.get(entryType) == null) return;
        errors.get(entryType).ignore(entryId, type);
        if (errors.get(entryType).isEmpty()) {
            errors.remove(entryType);
            if (errors.size() == 0) {
                success.set(true);
            }
        }
    }

    private Set<String> getErrorTypeByEntry(Long entryId, MetaType entryType) {
        if (errors.get(entryType) == null) return new HashSet<>();
        return errors.get(entryType).getErrorTypes(entryId);
    }

    private List<RelatedItem> getErrorRelatedEntryByType(Long entryId, MetaType entryType, String errorType) {
        if (errors.get(entryType) == null) return new ArrayList<>();

        List<Error.Node> nodes = errors.get(entryType).getErrorNodes(entryId);
        List<RelatedItem> result = new ArrayList<>();
        for (Error.Node node : nodes) {
            if (errorType == null || errorType.equalsIgnoreCase(node.getErrorType())) {
                if (node.getRelatedType() != null && node.getRelatedId() != null) {
                    result.add(new RelatedItem(node.getRelatedType(), node.getRelatedId()));
                }
            }
        }
        return result;
    }

    public String getGroupErrorReason(Long groupId) {
        return errors.get(MetaType.GROUP).getErrorMessage(groupId).toString();
    }

    public String getPolicyErrorReason(Long policyId) {
        return errors.get(MetaType.TRAFFIC_POLICY).getErrorMessage(policyId).toString();
    }

    public String getDrErrorReason(Long drId) {
        return errors.get(MetaType.DR).getErrorMessage(drId).toString();
    }

    public String getRuleErrorReason(Long ruleId) {
        return errors.get(MetaType.RULE).getErrorMessage(ruleId).toString();
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

    public Set<Long> getErrorDrs() {
        return getErrorEntries(MetaType.DR);
    }

    public Set<Long> getErrorRules() {
        return getErrorEntries(MetaType.RULE);
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

    static class RelatedItem{
        MetaType type;
        Long relatedId;
        public RelatedItem(MetaType type,Long relatedId){
            this.type = type;
            this.relatedId = relatedId;
        }

        public MetaType getType() {
            return type;
        }

        public RelatedItem setType(MetaType type) {
            this.type = type;
            return this;
        }

        public Long getRelatedId() {
            return relatedId;
        }

        public RelatedItem setRelatedId(Long relatedId) {
            this.relatedId = relatedId;
            return this;
        }
    }
}
