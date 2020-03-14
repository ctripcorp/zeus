package com.ctrip.zeus.model.model;

public class SlbServerReleaseInfo {
    private String m_ip;

    private String m_hostName;

    private Long m_version;

    public SlbServerReleaseInfo() {
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
        if (obj instanceof SlbServerReleaseInfo) {
            SlbServerReleaseInfo _o = (SlbServerReleaseInfo) obj;

            if (!equals(m_ip, _o.getIp())) {
                return false;
            }

            if (!equals(m_hostName, _o.getHostName())) {
                return false;
            }

            if (!equals(m_version, _o.getVersion())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getHostName() {
        return m_hostName;
    }

    public String getIp() {
        return m_ip;
    }

    public Long getVersion() {
        return m_version;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_ip == null ? 0 : m_ip.hashCode());
        hash = hash * 31 + (m_hostName == null ? 0 : m_hostName.hashCode());
        hash = hash * 31 + (m_version == null ? 0 : m_version.hashCode());

        return hash;
    }

    public SlbServerReleaseInfo setHostName(String hostName) {
        m_hostName = hostName;
        return this;
    }

    public SlbServerReleaseInfo setIp(String ip) {
        m_ip = ip;
        return this;
    }

    public SlbServerReleaseInfo setVersion(Long version) {
        m_version = version;
        return this;
    }

}
