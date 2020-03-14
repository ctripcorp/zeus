package com.ctrip.zeus.model.nginx;

public class NginxServerStatus {
    private String m_serverIp;

    private String m_status;

    private Long m_activeConnections;

    public NginxServerStatus() {
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
        if (obj instanceof NginxServerStatus) {
            NginxServerStatus _o = (NginxServerStatus) obj;

            if (!equals(m_serverIp, _o.getServerIp())) {
                return false;
            }

            if (!equals(m_status, _o.getStatus())) {
                return false;
            }

            if (!equals(m_activeConnections, _o.getActiveConnections())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Long getActiveConnections() {
        return m_activeConnections;
    }

    public String getServerIp() {
        return m_serverIp;
    }

    public String getStatus() {
        return m_status;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_serverIp == null ? 0 : m_serverIp.hashCode());
        hash = hash * 31 + (m_status == null ? 0 : m_status.hashCode());
        hash = hash * 31 + (m_activeConnections == null ? 0 : m_activeConnections.hashCode());

        return hash;
    }



    public NginxServerStatus setActiveConnections(Long activeConnections) {
        m_activeConnections = activeConnections;
        return this;
    }

    public NginxServerStatus setServerIp(String serverIp) {
        m_serverIp = serverIp;
        return this;
    }

    public NginxServerStatus setStatus(String status) {
        m_status = status;
        return this;
    }

}
