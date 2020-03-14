package com.ctrip.zeus.model.status;

import java.util.ArrayList;
import java.util.List;

public class GroupStatusList {
    private Integer m_total;

    private List<GroupStatus> m_groupStatuses = new ArrayList<GroupStatus>();

    public GroupStatusList() {
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



    public GroupStatusList addGroupStatus(GroupStatus groupStatus) {
        m_groupStatuses.add(groupStatus);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GroupStatusList) {
            GroupStatusList _o = (GroupStatusList) obj;

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_groupStatuses, _o.getGroupStatuses())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<GroupStatus> getGroupStatuses() {
        return m_groupStatuses;
    }

    public Integer getTotal() {
        return m_total;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_groupStatuses == null ? 0 : m_groupStatuses.hashCode());

        return hash;
    }



    public GroupStatusList setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
