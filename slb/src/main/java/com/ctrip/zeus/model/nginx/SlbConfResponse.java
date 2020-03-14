package com.ctrip.zeus.model.nginx;

public class SlbConfResponse {
    private Long m_slbId;

    private Integer m_version;

    private String m_nginxConf;

    private Vhosts m_vhosts;

    private Upstreams m_upstreams;

    public SlbConfResponse() {
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
        if (obj instanceof SlbConfResponse) {
            SlbConfResponse _o = (SlbConfResponse) obj;

            if (!equals(m_slbId, _o.getSlbId())) {
                return false;
            }

            if (!equals(m_version, _o.getVersion())) {
                return false;
            }

            if (!equals(m_nginxConf, _o.getNginxConf())) {
                return false;
            }

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

    public String getNginxConf() {
        return m_nginxConf;
    }

    public Long getSlbId() {
        return m_slbId;
    }

    public Upstreams getUpstreams() {
        return m_upstreams;
    }

    public Integer getVersion() {
        return m_version;
    }

    public Vhosts getVhosts() {
        return m_vhosts;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_slbId == null ? 0 : m_slbId.hashCode());
        hash = hash * 31 + (m_version == null ? 0 : m_version.hashCode());
        hash = hash * 31 + (m_nginxConf == null ? 0 : m_nginxConf.hashCode());
        hash = hash * 31 + (m_vhosts == null ? 0 : m_vhosts.hashCode());
        hash = hash * 31 + (m_upstreams == null ? 0 : m_upstreams.hashCode());

        return hash;
    }



    public SlbConfResponse setNginxConf(String nginxConf) {
        m_nginxConf = nginxConf;
        return this;
    }

    public SlbConfResponse setSlbId(Long slbId) {
        m_slbId = slbId;
        return this;
    }

    public SlbConfResponse setUpstreams(Upstreams upstreams) {
        m_upstreams = upstreams;
        return this;
    }

    public SlbConfResponse setVersion(Integer version) {
        m_version = version;
        return this;
    }

    public SlbConfResponse setVhosts(Vhosts vhosts) {
        m_vhosts = vhosts;
        return this;
    }

}
