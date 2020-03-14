package com.ctrip.zeus.model.model;

public class SlbServer {
    private Long m_slbId;

    private String m_ip;

    private String m_hostName;

    public SlbServer() {
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
        if (obj instanceof SlbServer) {
            SlbServer _o = (SlbServer) obj;

            if (!equals(m_slbId, _o.getSlbId())) {
                return false;
            }

            if (!equals(m_ip, _o.getIp())) {
                return false;
            }

            if (!equals(m_hostName, _o.getHostName())) {
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

    public Long getSlbId() {
        return m_slbId;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_slbId == null ? 0 : m_slbId.hashCode());
        hash = hash * 31 + (m_ip == null ? 0 : m_ip.hashCode());
        hash = hash * 31 + (m_hostName == null ? 0 : m_hostName.hashCode());

        return hash;
    }

    public SlbServer setHostName(String hostName) {
        m_hostName = hostName;
        return this;
    }

    public SlbServer setIp(String ip) {
        m_ip = ip;
        return this;
    }

    public SlbServer setSlbId(Long slbId) {
        m_slbId = slbId;
        return this;
    }

}
