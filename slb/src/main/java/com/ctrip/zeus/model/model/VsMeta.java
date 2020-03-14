package com.ctrip.zeus.model.model;

public class VsMeta {
    private Long m_vsId;

    private Integer m_appCount;

    private Integer m_groupCount;

    private Integer m_memberCount;

    private Integer m_groupServerCount;

    private Double m_qps;

    public VsMeta() {
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
        if (obj instanceof VsMeta) {
            VsMeta _o = (VsMeta) obj;

            if (!equals(m_vsId, _o.getVsId())) {
                return false;
            }

            if (!equals(m_appCount, _o.getAppCount())) {
                return false;
            }

            if (!equals(m_groupCount, _o.getGroupCount())) {
                return false;
            }

            if (!equals(m_memberCount, _o.getMemberCount())) {
                return false;
            }

            if (!equals(m_groupServerCount, _o.getGroupServerCount())) {
                return false;
            }

            if (!equals(m_qps, _o.getQps())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Integer getAppCount() {
        return m_appCount;
    }

    public Integer getGroupCount() {
        return m_groupCount;
    }

    public Integer getGroupServerCount() {
        return m_groupServerCount;
    }

    public Integer getMemberCount() {
        return m_memberCount;
    }

    public Double getQps() {
        return m_qps;
    }

    public Long getVsId() {
        return m_vsId;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_vsId == null ? 0 : m_vsId.hashCode());
        hash = hash * 31 + (m_appCount == null ? 0 : m_appCount.hashCode());
        hash = hash * 31 + (m_groupCount == null ? 0 : m_groupCount.hashCode());
        hash = hash * 31 + (m_memberCount == null ? 0 : m_memberCount.hashCode());
        hash = hash * 31 + (m_groupServerCount == null ? 0 : m_groupServerCount.hashCode());
        hash = hash * 31 + (m_qps == null ? 0 : m_qps.hashCode());

        return hash;
    }



    public VsMeta setAppCount(Integer appCount) {
        m_appCount = appCount;
        return this;
    }

    public VsMeta setGroupCount(Integer groupCount) {
        m_groupCount = groupCount;
        return this;
    }

    public VsMeta setGroupServerCount(Integer groupServerCount) {
        m_groupServerCount = groupServerCount;
        return this;
    }

    public VsMeta setMemberCount(Integer memberCount) {
        m_memberCount = memberCount;
        return this;
    }

    public VsMeta setQps(Double qps) {
        m_qps = qps;
        return this;
    }

    public VsMeta setVsId(Long vsId) {
        m_vsId = vsId;
        return this;
    }

}
