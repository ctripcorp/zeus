package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class VirtualServerList {
    private Integer m_total;

    private List<VirtualServer> m_virtualServers = new ArrayList<VirtualServer>();

    public VirtualServerList() {
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



    public VirtualServerList addVirtualServer(VirtualServer virtualServer) {
        m_virtualServers.add(virtualServer);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VirtualServerList) {
            VirtualServerList _o = (VirtualServerList) obj;

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_virtualServers, _o.getVirtualServers())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Integer getTotal() {
        return m_total;
    }

    public List<VirtualServer> getVirtualServers() {
        return m_virtualServers;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_virtualServers == null ? 0 : m_virtualServers.hashCode());

        return hash;
    }



    public VirtualServerList setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
