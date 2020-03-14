package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class GroupVsBoundList {
    private Long m_groupId;

    private List<GroupVsBound> m_bounds = new ArrayList<GroupVsBound>();

    public GroupVsBoundList() {
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



    public GroupVsBoundList addGroupVsBound(GroupVsBound groupVsBound) {
        m_bounds.add(groupVsBound);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GroupVsBoundList) {
            GroupVsBoundList _o = (GroupVsBoundList) obj;

            if (!equals(m_groupId, _o.getGroupId())) {
                return false;
            }

            if (!equals(m_bounds, _o.getBounds())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<GroupVsBound> getBounds() {
        return m_bounds;
    }

    public Long getGroupId() {
        return m_groupId;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_groupId == null ? 0 : m_groupId.hashCode());
        hash = hash * 31 + (m_bounds == null ? 0 : m_bounds.hashCode());

        return hash;
    }



    public GroupVsBoundList setGroupId(Long groupId) {
        m_groupId = groupId;
        return this;
    }

}
