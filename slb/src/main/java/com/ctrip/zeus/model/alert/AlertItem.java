package com.ctrip.zeus.model.alert;

public class AlertItem {
    private Long m_id;

    private Boolean m_status;

    private Long m_target;

    private String m_type;

    private String m_performer;

    private String m_versions;

    private String m_alertType;

    private java.util.Date m_appearTime;

    private java.util.Date m_lastNoticeTime;

    private java.util.Date m_solvedTime;

    private java.util.Date m_lastChangeTime;

    public AlertItem() {
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AlertItem) {
            AlertItem _o = (AlertItem) obj;

            if (!equals(m_id, _o.getId())) {
                return false;
            }

            if (!equals(m_status, _o.getStatus())) {
                return false;
            }

            if (!equals(m_target, _o.getTarget())) {
                return false;
            }

            if (!equals(m_type, _o.getType())) {
                return false;
            }

            if (!equals(m_performer, _o.getPerformer())) {
                return false;
            }

            if (!equals(m_versions, _o.getVersions())) {
                return false;
            }

            if (!equals(m_alertType, _o.getAlertType())) {
                return false;
            }

            if (!equals(m_appearTime, _o.getAppearTime())) {
                return false;
            }

            if (!equals(m_lastNoticeTime, _o.getLastNoticeTime())) {
                return false;
            }

            if (!equals(m_solvedTime, _o.getSolvedTime())) {
                return false;
            }

            if (!equals(m_lastChangeTime, _o.getLastChangeTime())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getAlertType() {
        return m_alertType;
    }

    public java.util.Date getAppearTime() {
        return m_appearTime;
    }

    public Long getId() {
        return m_id;
    }

    public java.util.Date getLastChangeTime() {
        return m_lastChangeTime;
    }

    public java.util.Date getLastNoticeTime() {
        return m_lastNoticeTime;
    }

    public String getPerformer() {
        return m_performer;
    }

    public java.util.Date getSolvedTime() {
        return m_solvedTime;
    }

    public Boolean getStatus() {
        return m_status;
    }

    public Long getTarget() {
        return m_target;
    }

    public String getType() {
        return m_type;
    }

    public String getVersions() {
        return m_versions;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());
        hash = hash * 31 + (m_status == null ? 0 : m_status.hashCode());
        hash = hash * 31 + (m_target == null ? 0 : m_target.hashCode());
        hash = hash * 31 + (m_type == null ? 0 : m_type.hashCode());
        hash = hash * 31 + (m_performer == null ? 0 : m_performer.hashCode());
        hash = hash * 31 + (m_versions == null ? 0 : m_versions.hashCode());
        hash = hash * 31 + (m_alertType == null ? 0 : m_alertType.hashCode());
        hash = hash * 31 + (m_appearTime == null ? 0 : m_appearTime.hashCode());
        hash = hash * 31 + (m_lastNoticeTime == null ? 0 : m_lastNoticeTime.hashCode());
        hash = hash * 31 + (m_solvedTime == null ? 0 : m_solvedTime.hashCode());
        hash = hash * 31 + (m_lastChangeTime == null ? 0 : m_lastChangeTime.hashCode());

        return hash;
    }

    public boolean isStatus() {
        return m_status != null && m_status.booleanValue();
    }

    public AlertItem setAlertType(String alertType) {
        m_alertType = alertType;
        return this;
    }

    public AlertItem setAppearTime(java.util.Date appearTime) {
        m_appearTime = appearTime;
        return this;
    }

    public AlertItem setId(Long id) {
        m_id = id;
        return this;
    }

    public AlertItem setLastChangeTime(java.util.Date lastChangeTime) {
        m_lastChangeTime = lastChangeTime;
        return this;
    }

    public AlertItem setLastNoticeTime(java.util.Date lastNoticeTime) {
        m_lastNoticeTime = lastNoticeTime;
        return this;
    }

    public AlertItem setPerformer(String performer) {
        m_performer = performer;
        return this;
    }

    public AlertItem setSolvedTime(java.util.Date solvedTime) {
        m_solvedTime = solvedTime;
        return this;
    }

    public AlertItem setStatus(Boolean status) {
        m_status = status;
        return this;
    }

    public AlertItem setTarget(Long target) {
        m_target = target;
        return this;
    }

    public AlertItem setType(String type) {
        m_type = type;
        return this;
    }

    public AlertItem setVersions(String versions) {
        m_versions = versions;
        return this;
    }

}
