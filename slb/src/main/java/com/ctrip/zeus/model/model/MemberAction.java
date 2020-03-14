package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class MemberAction {
    private String m_groupName;

    private List<String> m_ips = new ArrayList<String>();

    public MemberAction() {
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



    public MemberAction addIp(String ip) {
        m_ips.add(ip);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MemberAction) {
            MemberAction _o = (MemberAction) obj;

            if (!equals(m_groupName, _o.getGroupName())) {
                return false;
            }

            if (!equals(m_ips, _o.getIps())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getGroupName() {
        return m_groupName;
    }

    public List<String> getIps() {
        return m_ips;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_groupName == null ? 0 : m_groupName.hashCode());
        hash = hash * 31 + (m_ips == null ? 0 : m_ips.hashCode());

        return hash;
    }



    public MemberAction setGroupName(String groupName) {
        m_groupName = groupName;
        return this;
    }

}
