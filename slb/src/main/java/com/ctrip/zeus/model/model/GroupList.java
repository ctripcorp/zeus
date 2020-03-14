package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class GroupList {
    private Integer m_total;

    private List<Group> m_groups = new ArrayList<Group>();

    public GroupList() {
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



    public GroupList addGroup(Group group) {
        m_groups.add(group);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GroupList) {
            GroupList _o = (GroupList) obj;

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_groups, _o.getGroups())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<Group> getGroups() {
        return m_groups;
    }

    public Integer getTotal() {
        return m_total;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_groups == null ? 0 : m_groups.hashCode());

        return hash;
    }



    public GroupList setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
