package com.ctrip.zeus.model.model;

public class GroupMeta {
    private Long m_groupId;

    private Integer m_memberCount;

    private Integer m_groupServerCount;

    private Double m_qps;

    public GroupMeta() {
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
        if (obj instanceof GroupMeta) {
            GroupMeta _o = (GroupMeta) obj;

            if (!equals(m_groupId, _o.getGroupId())) {
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

    public Long getGroupId() {
        return m_groupId;
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

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_groupId == null ? 0 : m_groupId.hashCode());
        hash = hash * 31 + (m_memberCount == null ? 0 : m_memberCount.hashCode());
        hash = hash * 31 + (m_groupServerCount == null ? 0 : m_groupServerCount.hashCode());
        hash = hash * 31 + (m_qps == null ? 0 : m_qps.hashCode());

        return hash;
    }



    public GroupMeta setGroupId(Long groupId) {
        m_groupId = groupId;
        return this;
    }

    public GroupMeta setGroupServerCount(Integer groupServerCount) {
        m_groupServerCount = groupServerCount;
        return this;
    }

    public GroupMeta setMemberCount(Integer memberCount) {
        m_memberCount = memberCount;
        return this;
    }

    public GroupMeta setQps(Double qps) {
        m_qps = qps;
        return this;
    }

}
