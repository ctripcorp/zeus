package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class GroupServerList {
    private Long m_groupId;

    private Integer m_version;

    private Integer m_total;

    private List<GroupServer> m_groupServers = new ArrayList<GroupServer>();

    public GroupServerList() {
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



    public GroupServerList addGroupServer(GroupServer groupServer) {
        m_groupServers.add(groupServer);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GroupServerList) {
            GroupServerList _o = (GroupServerList) obj;

            if (!equals(m_groupId, _o.getGroupId())) {
                return false;
            }

            if (!equals(m_version, _o.getVersion())) {
                return false;
            }

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_groupServers, _o.getGroupServers())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Long getGroupId() {
        return m_groupId;
    }

    public List<GroupServer> getGroupServers() {
        return m_groupServers;
    }

    public Integer getTotal() {
        return m_total;
    }

    public Integer getVersion() {
        return m_version;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_groupId == null ? 0 : m_groupId.hashCode());
        hash = hash * 31 + (m_version == null ? 0 : m_version.hashCode());
        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_groupServers == null ? 0 : m_groupServers.hashCode());

        return hash;
    }



    public GroupServerList setGroupId(Long groupId) {
        m_groupId = groupId;
        return this;
    }

    public GroupServerList setTotal(Integer total) {
        m_total = total;
        return this;
    }

    public GroupServerList setVersion(Integer version) {
        m_version = version;
        return this;
    }

}
