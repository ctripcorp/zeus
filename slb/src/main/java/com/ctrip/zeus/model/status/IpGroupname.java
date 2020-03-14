package com.ctrip.zeus.model.status;

public class IpGroupname {
    private String m_memberIp;

    private String m_memberGroupname;

    public IpGroupname() {
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
        if (obj instanceof IpGroupname) {
            IpGroupname _o = (IpGroupname) obj;

            if (!equals(m_memberIp, _o.getMemberIp())) {
                return false;
            }

            if (!equals(m_memberGroupname, _o.getMemberGroupname())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getMemberGroupname() {
        return m_memberGroupname;
    }

    public String getMemberIp() {
        return m_memberIp;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_memberIp == null ? 0 : m_memberIp.hashCode());
        hash = hash * 31 + (m_memberGroupname == null ? 0 : m_memberGroupname.hashCode());

        return hash;
    }



    public IpGroupname setMemberGroupname(String memberGroupname) {
        m_memberGroupname = memberGroupname;
        return this;
    }

    public IpGroupname setMemberIp(String memberIp) {
        m_memberIp = memberIp;
        return this;
    }

}
