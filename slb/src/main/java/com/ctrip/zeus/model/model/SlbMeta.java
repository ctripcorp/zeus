package com.ctrip.zeus.model.model;

public class SlbMeta {
    private Long m_slbId;

    private Integer m_appCount;

    private Integer m_vsCount;

    private Integer m_groupCount;

    private Integer m_memberCount;

    private Integer m_groupServerCount;

    private Double m_qps;

    public SlbMeta() {
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
        if (obj instanceof SlbMeta) {
            SlbMeta _o = (SlbMeta) obj;

            if (!equals(m_slbId, _o.getSlbId())) {
                return false;
            }

            if (!equals(m_appCount, _o.getAppCount())) {
                return false;
            }

            if (!equals(m_vsCount, _o.getVsCount())) {
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

    public Long getSlbId() {
        return m_slbId;
    }

    public Integer getVsCount() {
        return m_vsCount;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_slbId == null ? 0 : m_slbId.hashCode());
        hash = hash * 31 + (m_appCount == null ? 0 : m_appCount.hashCode());
        hash = hash * 31 + (m_vsCount == null ? 0 : m_vsCount.hashCode());
        hash = hash * 31 + (m_groupCount == null ? 0 : m_groupCount.hashCode());
        hash = hash * 31 + (m_memberCount == null ? 0 : m_memberCount.hashCode());
        hash = hash * 31 + (m_groupServerCount == null ? 0 : m_groupServerCount.hashCode());
        hash = hash * 31 + (m_qps == null ? 0 : m_qps.hashCode());

        return hash;
    }



    public SlbMeta setAppCount(Integer appCount) {
        m_appCount = appCount;
        return this;
    }

    public SlbMeta setGroupCount(Integer groupCount) {
        m_groupCount = groupCount;
        return this;
    }

    public SlbMeta setGroupServerCount(Integer groupServerCount) {
        m_groupServerCount = groupServerCount;
        return this;
    }

    public SlbMeta setMemberCount(Integer memberCount) {
        m_memberCount = memberCount;
        return this;
    }

    public SlbMeta setQps(Double qps) {
        m_qps = qps;
        return this;
    }

    public SlbMeta setSlbId(Long slbId) {
        m_slbId = slbId;
        return this;
    }

    public SlbMeta setVsCount(Integer vsCount) {
        m_vsCount = vsCount;
        return this;
    }

}
