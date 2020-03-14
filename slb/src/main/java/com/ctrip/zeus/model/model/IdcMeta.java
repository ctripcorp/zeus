package com.ctrip.zeus.model.model;

public class IdcMeta {
    private String m_idc;

    private Integer m_slbCount;

    private Integer m_appCount;

    private Integer m_slbServerCount;

    private Integer m_vsCount;

    private Integer m_groupCount;

    private Integer m_memberCount;

    private Integer m_groupServerCount;

    private Double m_qps;

    public IdcMeta() {
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
        if (obj instanceof IdcMeta) {
            IdcMeta _o = (IdcMeta) obj;

            if (!equals(m_idc, _o.getIdc())) {
                return false;
            }

            if (!equals(m_slbCount, _o.getSlbCount())) {
                return false;
            }

            if (!equals(m_appCount, _o.getAppCount())) {
                return false;
            }

            if (!equals(m_slbServerCount, _o.getSlbServerCount())) {
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

    public String getIdc() {
        return m_idc;
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

    public Integer getSlbServerCount() {
        return m_slbServerCount;
    }

    public Integer getVsCount() {
        return m_vsCount;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_idc == null ? 0 : m_idc.hashCode());
        hash = hash * 31 + (m_slbCount == null ? 0 : m_slbCount.hashCode());
        hash = hash * 31 + (m_appCount == null ? 0 : m_appCount.hashCode());
        hash = hash * 31 + (m_slbServerCount == null ? 0 : m_slbServerCount.hashCode());
        hash = hash * 31 + (m_vsCount == null ? 0 : m_vsCount.hashCode());
        hash = hash * 31 + (m_groupCount == null ? 0 : m_groupCount.hashCode());
        hash = hash * 31 + (m_memberCount == null ? 0 : m_memberCount.hashCode());
        hash = hash * 31 + (m_groupServerCount == null ? 0 : m_groupServerCount.hashCode());
        hash = hash * 31 + (m_qps == null ? 0 : m_qps.hashCode());

        return hash;
    }



    public IdcMeta setAppCount(Integer appCount) {
        m_appCount = appCount;
        return this;
    }

    public IdcMeta setGroupCount(Integer groupCount) {
        m_groupCount = groupCount;
        return this;
    }

    public IdcMeta setGroupServerCount(Integer groupServerCount) {
        m_groupServerCount = groupServerCount;
        return this;
    }

    public IdcMeta setIdc(String idc) {
        m_idc = idc;
        return this;
    }

    public IdcMeta setMemberCount(Integer memberCount) {
        m_memberCount = memberCount;
        return this;
    }

    public IdcMeta setQps(Double qps) {
        m_qps = qps;
        return this;
    }

    public IdcMeta setSlbCount(Integer slbCount) {
        m_slbCount = slbCount;
        return this;
    }

    public IdcMeta setSlbServerCount(Integer slbServerCount) {
        m_slbServerCount = slbServerCount;
        return this;
    }

    public IdcMeta setVsCount(Integer vsCount) {
        m_vsCount = vsCount;
        return this;
    }

}
