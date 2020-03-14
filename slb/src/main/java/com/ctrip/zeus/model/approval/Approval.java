package com.ctrip.zeus.model.approval;

import java.util.ArrayList;
import java.util.List;

public class Approval {
    private Long m_id;

    private Boolean m_approved;

    private String m_applyBy;

    private java.util.Date m_applyTime;

    private String m_applyType;

    private List<Long> m_applyTargets = new ArrayList<Long>();

    private List<String> m_applyOps = new ArrayList<String>();

    private java.util.Date m_approvedTime;

    private String m_approvedBy;

    private java.util.Date m_lastChangeTime;

    private Approval m_approval;

    public Approval() {
    }

    protected boolean equals(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        } else if (o2 == null) {
            return false;
        } else {
            return o1.equals(o2);
        }
    }




    public Approval addApplyOp(String applyOp) {
        m_applyOps.add(applyOp);
        return this;
    }

    public Approval addApplyTarget(Long applyTarget) {
        m_applyTargets.add(applyTarget);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Approval) {
            Approval _o = (Approval) obj;

            if (!equals(m_id, _o.getId())) {
                return false;
            }

            if (!equals(m_approved, _o.getApproved())) {
                return false;
            }

            if (!equals(m_applyBy, _o.getApplyBy())) {
                return false;
            }

            if (!equals(m_applyTime, _o.getApplyTime())) {
                return false;
            }

            if (!equals(m_applyType, _o.getApplyType())) {
                return false;
            }

            if (!equals(m_applyTargets, _o.getApplyTargets())) {
                return false;
            }

            if (!equals(m_applyOps, _o.getApplyOps())) {
                return false;
            }

            if (!equals(m_approvedTime, _o.getApprovedTime())) {
                return false;
            }

            if (!equals(m_approvedBy, _o.getApprovedBy())) {
                return false;
            }

            if (!equals(m_lastChangeTime, _o.getLastChangeTime())) {
                return false;
            }

            if (!equals(m_approval, _o.getApproval())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getApplyBy() {
        return m_applyBy;
    }

    public List<String> getApplyOps() {
        return m_applyOps;
    }

    public List<Long> getApplyTargets() {
        return m_applyTargets;
    }

    public java.util.Date getApplyTime() {
        return m_applyTime;
    }

    public String getApplyType() {
        return m_applyType;
    }

    public Approval getApproval() {
        return m_approval;
    }

    public Boolean getApproved() {
        return m_approved;
    }

    public String getApprovedBy() {
        return m_approvedBy;
    }

    public java.util.Date getApprovedTime() {
        return m_approvedTime;
    }

    public Long getId() {
        return m_id;
    }

    public java.util.Date getLastChangeTime() {
        return m_lastChangeTime;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());
        hash = hash * 31 + (m_approved == null ? 0 : m_approved.hashCode());
        hash = hash * 31 + (m_applyBy == null ? 0 : m_applyBy.hashCode());
        hash = hash * 31 + (m_applyTime == null ? 0 : m_applyTime.hashCode());
        hash = hash * 31 + (m_applyType == null ? 0 : m_applyType.hashCode());
        hash = hash * 31 + (m_applyTargets == null ? 0 : m_applyTargets.hashCode());
        hash = hash * 31 + (m_applyOps == null ? 0 : m_applyOps.hashCode());
        hash = hash * 31 + (m_approvedTime == null ? 0 : m_approvedTime.hashCode());
        hash = hash * 31 + (m_approvedBy == null ? 0 : m_approvedBy.hashCode());
        hash = hash * 31 + (m_lastChangeTime == null ? 0 : m_lastChangeTime.hashCode());
        hash = hash * 31 + (m_approval == null ? 0 : m_approval.hashCode());

        return hash;
    }

    public boolean isApproved() {
        return m_approved != null && m_approved.booleanValue();
    }



    public Approval setApplyBy(String applyBy) {
        m_applyBy = applyBy;
        return this;
    }

    public Approval setApplyTime(java.util.Date applyTime) {
        m_applyTime = applyTime;
        return this;
    }

    public Approval setApplyType(String applyType) {
        m_applyType = applyType;
        return this;
    }

    public Approval setApproval(Approval approval) {
        m_approval = approval;
        return this;
    }

    public Approval setApproved(Boolean approved) {
        m_approved = approved;
        return this;
    }

    public Approval setApprovedBy(String approvedBy) {
        m_approvedBy = approvedBy;
        return this;
    }

    public Approval setApprovedTime(java.util.Date approvedTime) {
        m_approvedTime = approvedTime;
        return this;
    }

    public Approval setId(Long id) {
        m_id = id;
        return this;
    }

    public Approval setLastChangeTime(java.util.Date lastChangeTime) {
        m_lastChangeTime = lastChangeTime;
        return this;
    }

}
