package com.ctrip.zeus.model.nginx;

public class VirtualServerConfResponse {
    private Long m_virtualServerId;

    private Integer m_version;

    private String m_serverConf;

    private String m_upstreamConf;

    public VirtualServerConfResponse() {
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
        if (obj instanceof VirtualServerConfResponse) {
            VirtualServerConfResponse _o = (VirtualServerConfResponse) obj;

            if (!equals(m_virtualServerId, _o.getVirtualServerId())) {
                return false;
            }

            if (!equals(m_version, _o.getVersion())) {
                return false;
            }

            if (!equals(m_serverConf, _o.getServerConf())) {
                return false;
            }

            if (!equals(m_upstreamConf, _o.getUpstreamConf())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getServerConf() {
        return m_serverConf;
    }

    public String getUpstreamConf() {
        return m_upstreamConf;
    }

    public Integer getVersion() {
        return m_version;
    }

    public Long getVirtualServerId() {
        return m_virtualServerId;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_virtualServerId == null ? 0 : m_virtualServerId.hashCode());
        hash = hash * 31 + (m_version == null ? 0 : m_version.hashCode());
        hash = hash * 31 + (m_serverConf == null ? 0 : m_serverConf.hashCode());
        hash = hash * 31 + (m_upstreamConf == null ? 0 : m_upstreamConf.hashCode());

        return hash;
    }



    public VirtualServerConfResponse setServerConf(String serverConf) {
        m_serverConf = serverConf;
        return this;
    }

    public VirtualServerConfResponse setUpstreamConf(String upstreamConf) {
        m_upstreamConf = upstreamConf;
        return this;
    }

    public VirtualServerConfResponse setVersion(Integer version) {
        m_version = version;
        return this;
    }

    public VirtualServerConfResponse setVirtualServerId(Long virtualServerId) {
        m_virtualServerId = virtualServerId;
        return this;
    }

}
