package com.ctrip.zeus.model.nginx;

public class UpstreamStatus {
    private Servers m_servers;

    public UpstreamStatus() {
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
        if (obj instanceof UpstreamStatus) {
            UpstreamStatus _o = (UpstreamStatus) obj;

            if (!equals(m_servers, _o.getServers())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Servers getServers() {
        return m_servers;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_servers == null ? 0 : m_servers.hashCode());

        return hash;
    }



    public UpstreamStatus setServers(Servers servers) {
        m_servers = servers;
        return this;
    }

}
