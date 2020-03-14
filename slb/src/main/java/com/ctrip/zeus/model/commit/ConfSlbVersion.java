package com.ctrip.zeus.model.commit;

public class ConfSlbVersion {
    private Long m_id;

    private Long m_slbId;

    private Long m_previousVersion;

    private Long m_currentVersion;

    private java.util.Date m_dataChangeLastTime;

    public ConfSlbVersion() {
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
        if (obj instanceof ConfSlbVersion) {
            ConfSlbVersion _o = (ConfSlbVersion) obj;

            if (!equals(m_id, _o.getId())) {
                return false;
            }

            if (!equals(m_slbId, _o.getSlbId())) {
                return false;
            }

            if (!equals(m_previousVersion, _o.getPreviousVersion())) {
                return false;
            }

            if (!equals(m_currentVersion, _o.getCurrentVersion())) {
                return false;
            }

            if (!equals(m_dataChangeLastTime, _o.getDataChangeLastTime())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Long getCurrentVersion() {
        return m_currentVersion;
    }

    public java.util.Date getDataChangeLastTime() {
        return m_dataChangeLastTime;
    }

    public Long getId() {
        return m_id;
    }

    public Long getPreviousVersion() {
        return m_previousVersion;
    }

    public Long getSlbId() {
        return m_slbId;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());
        hash = hash * 31 + (m_slbId == null ? 0 : m_slbId.hashCode());
        hash = hash * 31 + (m_previousVersion == null ? 0 : m_previousVersion.hashCode());
        hash = hash * 31 + (m_currentVersion == null ? 0 : m_currentVersion.hashCode());
        hash = hash * 31 + (m_dataChangeLastTime == null ? 0 : m_dataChangeLastTime.hashCode());

        return hash;
    }



    public ConfSlbVersion setCurrentVersion(Long currentVersion) {
        m_currentVersion = currentVersion;
        return this;
    }

    public ConfSlbVersion setDataChangeLastTime(java.util.Date dataChangeLastTime) {
        m_dataChangeLastTime = dataChangeLastTime;
        return this;
    }

    public ConfSlbVersion setId(Long id) {
        m_id = id;
        return this;
    }

    public ConfSlbVersion setPreviousVersion(Long previousVersion) {
        m_previousVersion = previousVersion;
        return this;
    }

    public ConfSlbVersion setSlbId(Long slbId) {
        m_slbId = slbId;
        return this;
    }

}
