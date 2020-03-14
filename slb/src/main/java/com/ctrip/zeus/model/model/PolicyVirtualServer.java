package com.ctrip.zeus.model.model;

public class PolicyVirtualServer {
    private String m_path;

    private Integer m_priority;

    private VirtualServer m_virtualServer;

    public PolicyVirtualServer() {
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



    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PolicyVirtualServer) {
            PolicyVirtualServer _o = (PolicyVirtualServer) obj;

            if (!equals(m_path, _o.getPath())) {
                return false;
            }

            if (!equals(m_priority, _o.getPriority())) {
                return false;
            }

            if (!equals(m_virtualServer, _o.getVirtualServer())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getPath() {
        return m_path;
    }

    public Integer getPriority() {
        return m_priority;
    }

    public VirtualServer getVirtualServer() {
        return m_virtualServer;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_path == null ? 0 : m_path.hashCode());
        hash = hash * 31 + (m_priority == null ? 0 : m_priority.hashCode());
        hash = hash * 31 + (m_virtualServer == null ? 0 : m_virtualServer.hashCode());

        return hash;
    }



    public PolicyVirtualServer setPath(String path) {
        m_path = path;
        return this;
    }

    public PolicyVirtualServer setPriority(Integer priority) {
        m_priority = priority;
        return this;
    }

    public PolicyVirtualServer setVirtualServer(VirtualServer virtualServer) {
        m_virtualServer = virtualServer;
        return this;
    }

}
