package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class ServerAction {
    private String m_name;

    private List<String> m_ips = new ArrayList<String>();

    public ServerAction() {
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



    public ServerAction addIp(String ip) {
        m_ips.add(ip);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ServerAction) {
            ServerAction _o = (ServerAction) obj;

            if (!equals(m_name, _o.getName())) {
                return false;
            }

            if (!equals(m_ips, _o.getIps())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<String> getIps() {
        return m_ips;
    }

    public String getName() {
        return m_name;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_name == null ? 0 : m_name.hashCode());
        hash = hash * 31 + (m_ips == null ? 0 : m_ips.hashCode());

        return hash;
    }



    public ServerAction setName(String name) {
        m_name = name;
        return this;
    }

}
