package com.ctrip.zeus.model.status;

import java.util.ArrayList;
import java.util.List;

public class ServerStatus {
    private String m_ip;

    private Boolean m_up;

    private List<String> m_groupNames = new ArrayList<String>();

    public ServerStatus() {
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



    public ServerStatus addGroupName(String groupName) {
        m_groupNames.add(groupName);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ServerStatus) {
            ServerStatus _o = (ServerStatus) obj;

            if (!equals(m_ip, _o.getIp())) {
                return false;
            }

            if (!equals(m_up, _o.getUp())) {
                return false;
            }

            if (!equals(m_groupNames, _o.getGroupNames())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<String> getGroupNames() {
        return m_groupNames;
    }

    public String getIp() {
        return m_ip;
    }

    public Boolean getUp() {
        return m_up;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_ip == null ? 0 : m_ip.hashCode());
        hash = hash * 31 + (m_up == null ? 0 : m_up.hashCode());
        hash = hash * 31 + (m_groupNames == null ? 0 : m_groupNames.hashCode());

        return hash;
    }

    public boolean isUp() {
        return m_up != null && m_up.booleanValue();
    }



    public ServerStatus setIp(String ip) {
        m_ip = ip;
        return this;
    }

    public ServerStatus setUp(Boolean up) {
        m_up = up;
        return this;
    }

}
