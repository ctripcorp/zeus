package com.ctrip.zeus.model.nginx;

public class VsConfData {
    private String m_upstreamConf;

    private String m_vhostConf;

    public VsConfData() {
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
        if (obj instanceof VsConfData) {
            VsConfData _o = (VsConfData) obj;

            if (!equals(m_upstreamConf, _o.getUpstreamConf())) {
                return false;
            }

            if (!equals(m_vhostConf, _o.getVhostConf())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getUpstreamConf() {
        return m_upstreamConf;
    }

    public String getVhostConf() {
        return m_vhostConf;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_upstreamConf == null ? 0 : m_upstreamConf.hashCode());
        hash = hash * 31 + (m_vhostConf == null ? 0 : m_vhostConf.hashCode());

        return hash;
    }



    public VsConfData setUpstreamConf(String upstreamConf) {
        m_upstreamConf = upstreamConf;
        return this;
    }

    public VsConfData setVhostConf(String vhostConf) {
        m_vhostConf = vhostConf;
        return this;
    }

}
