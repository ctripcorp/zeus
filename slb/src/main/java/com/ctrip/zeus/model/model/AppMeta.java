package com.ctrip.zeus.model.model;

public class AppMeta {
    private String m_appId;

    private Integer m_slbCount;

    private Integer m_vsCount;

    private Integer m_groupCount;

    private Integer m_memberCount;

    private Integer m_groupServerCount;

    private Double m_qps;

    public AppMeta() {
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
        if (obj instanceof AppMeta) {
            AppMeta _o = (AppMeta) obj;

            if (!equals(m_appId, _o.getAppId())) {
                return false;
            }

            if (!equals(m_slbCount, _o.getSlbCount())) {
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

    public String getAppId() {
        return m_appId;
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

    public Integer getSlbCount() {
        return m_slbCount;
    }

    public Integer getVsCount() {
        return m_vsCount;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_appId == null ? 0 : m_appId.hashCode());
        hash = hash * 31 + (m_slbCount == null ? 0 : m_slbCount.hashCode());
        hash = hash * 31 + (m_vsCount == null ? 0 : m_vsCount.hashCode());
        hash = hash * 31 + (m_groupCount == null ? 0 : m_groupCount.hashCode());
        hash = hash * 31 + (m_memberCount == null ? 0 : m_memberCount.hashCode());
        hash = hash * 31 + (m_groupServerCount == null ? 0 : m_groupServerCount.hashCode());
        hash = hash * 31 + (m_qps == null ? 0 : m_qps.hashCode());

        return hash;
    }



    public AppMeta setAppId(String appId) {
        m_appId = appId;
        return this;
    }

    public AppMeta setGroupCount(Integer groupCount) {
        m_groupCount = groupCount;
        return this;
    }

    public AppMeta setGroupServerCount(Integer groupServerCount) {
        m_groupServerCount = groupServerCount;
        return this;
    }

    public AppMeta setMemberCount(Integer memberCount) {
        m_memberCount = memberCount;
        return this;
    }

    public AppMeta setQps(Double qps) {
        m_qps = qps;
        return this;
    }

    public AppMeta setSlbCount(Integer slbCount) {
        m_slbCount = slbCount;
        return this;
    }

    public AppMeta setVsCount(Integer vsCount) {
        m_vsCount = vsCount;
        return this;
    }

}
