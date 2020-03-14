package com.ctrip.zeus.model.status;

public class IpAddresses {
    private String m_ipAddr;

    public IpAddresses() {
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
        if (obj instanceof IpAddresses) {
            IpAddresses _o = (IpAddresses) obj;

            if (!equals(m_ipAddr, _o.getIpAddr())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getIpAddr() {
        return m_ipAddr;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_ipAddr == null ? 0 : m_ipAddr.hashCode());

        return hash;
    }



    public IpAddresses setIpAddr(String ipAddr) {
        m_ipAddr = ipAddr;
        return this;
    }

}
