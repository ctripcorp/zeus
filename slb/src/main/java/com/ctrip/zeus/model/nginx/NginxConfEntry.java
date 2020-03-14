package com.ctrip.zeus.model.nginx;

public class NginxConfEntry {
    private Vhosts m_vhosts;

    private Upstreams m_upstreams;

    public NginxConfEntry() {
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
        if (obj instanceof NginxConfEntry) {
            NginxConfEntry _o = (NginxConfEntry) obj;

            if (!equals(m_vhosts, _o.getVhosts())) {
                return false;
            }

            if (!equals(m_upstreams, _o.getUpstreams())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Upstreams getUpstreams() {
        return m_upstreams;
    }

    public Vhosts getVhosts() {
        return m_vhosts;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_vhosts == null ? 0 : m_vhosts.hashCode());
        hash = hash * 31 + (m_upstreams == null ? 0 : m_upstreams.hashCode());

        return hash;
    }



    public NginxConfEntry setUpstreams(Upstreams upstreams) {
        m_upstreams = upstreams;
        return this;
    }

    public NginxConfEntry setVhosts(Vhosts vhosts) {
        m_vhosts = vhosts;
        return this;
    }

}
