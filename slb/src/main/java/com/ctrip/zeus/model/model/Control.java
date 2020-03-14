package com.ctrip.zeus.model.model;

public class Control {
    private String m_groupName;

    private Long m_groupId;

    private String m_appId;

    private Integer m_weight;

    public Control() {
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
        if (obj instanceof Control) {
            Control _o = (Control) obj;

            if (!equals(m_groupName, _o.getGroupName())) {
                return false;
            }

            if (!equals(m_groupId, _o.getGroupId())) {
                return false;
            }

            if (!equals(m_appId, _o.getAppId())) {
                return false;
            }

            if (!equals(m_weight, _o.getWeight())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getAppId() {
        return m_appId;
    }

    public Long getGroupId() {
        return m_groupId;
    }

    public String getGroupName() {
        return m_groupName;
    }

    public Integer getWeight() {
        return m_weight;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_groupName == null ? 0 : m_groupName.hashCode());
        hash = hash * 31 + (m_groupId == null ? 0 : m_groupId.hashCode());
        hash = hash * 31 + (m_appId == null ? 0 : m_appId.hashCode());
        hash = hash * 31 + (m_weight == null ? 0 : m_weight.hashCode());

        return hash;
    }



    public Control setAppId(String appId) {
        m_appId = appId;
        return this;
    }

    public Control setGroupId(Long groupId) {
        m_groupId = groupId;
        return this;
    }

    public Control setGroupName(String groupName) {
        m_groupName = groupName;
        return this;
    }

    public Control setWeight(Integer weight) {
        m_weight = weight;
        return this;
    }

}
