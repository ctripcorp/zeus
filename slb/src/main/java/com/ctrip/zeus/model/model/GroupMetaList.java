package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class GroupMetaList {
    private Integer m_total;

    private List<GroupMeta> m_groupMetas = new ArrayList<GroupMeta>();

    public GroupMetaList() {
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



    public GroupMetaList addGroupMeta(GroupMeta groupMeta) {
        m_groupMetas.add(groupMeta);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GroupMetaList) {
            GroupMetaList _o = (GroupMetaList) obj;

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_groupMetas, _o.getGroupMetas())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<GroupMeta> getGroupMetas() {
        return m_groupMetas;
    }

    public Integer getTotal() {
        return m_total;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_groupMetas == null ? 0 : m_groupMetas.hashCode());

        return hash;
    }



    public GroupMetaList setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
