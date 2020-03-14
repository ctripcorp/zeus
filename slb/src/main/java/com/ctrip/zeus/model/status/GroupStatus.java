package com.ctrip.zeus.model.status;

import java.util.ArrayList;
import java.util.List;

public class GroupStatus {
    private String m_groupName;

    private String m_slbName;

    private Long m_groupId;

    private Long m_slbId;

    private Boolean m_activated;

    private List<GroupServerStatus> m_groupServerStatuses = new ArrayList<GroupServerStatus>();

    public GroupStatus() {
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



    public GroupStatus addGroupServerStatus(GroupServerStatus groupServerStatus) {
        m_groupServerStatuses.add(groupServerStatus);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GroupStatus) {
            GroupStatus _o = (GroupStatus) obj;

            if (!equals(m_groupName, _o.getGroupName())) {
                return false;
            }

            if (!equals(m_slbName, _o.getSlbName())) {
                return false;
            }

            if (!equals(m_groupId, _o.getGroupId())) {
                return false;
            }

            if (!equals(m_slbId, _o.getSlbId())) {
                return false;
            }

            if (!equals(m_activated, _o.getActivated())) {
                return false;
            }

            if (!equals(m_groupServerStatuses, _o.getGroupServerStatuses())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Boolean getActivated() {
        return m_activated;
    }

    public Long getGroupId() {
        return m_groupId;
    }

    public String getGroupName() {
        return m_groupName;
    }

    public List<GroupServerStatus> getGroupServerStatuses() {
        return m_groupServerStatuses;
    }

    public Long getSlbId() {
        return m_slbId;
    }

    public String getSlbName() {
        return m_slbName;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_groupName == null ? 0 : m_groupName.hashCode());
        hash = hash * 31 + (m_slbName == null ? 0 : m_slbName.hashCode());
        hash = hash * 31 + (m_groupId == null ? 0 : m_groupId.hashCode());
        hash = hash * 31 + (m_slbId == null ? 0 : m_slbId.hashCode());
        hash = hash * 31 + (m_activated == null ? 0 : m_activated.hashCode());
        hash = hash * 31 + (m_groupServerStatuses == null ? 0 : m_groupServerStatuses.hashCode());

        return hash;
    }

    public boolean isActivated() {
        return m_activated != null && m_activated.booleanValue();
    }



    public GroupStatus setActivated(Boolean activated) {
        m_activated = activated;
        return this;
    }

    public GroupStatus setGroupId(Long groupId) {
        m_groupId = groupId;
        return this;
    }

    public GroupStatus setGroupName(String groupName) {
        m_groupName = groupName;
        return this;
    }

    public GroupStatus setSlbId(Long slbId) {
        m_slbId = slbId;
        return this;
    }

    public GroupStatus setSlbName(String slbName) {
        m_slbName = slbName;
        return this;
    }

}
