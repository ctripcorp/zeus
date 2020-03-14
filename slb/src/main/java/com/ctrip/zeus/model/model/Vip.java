package com.ctrip.zeus.model.model;

public class Vip {
    private String m_ip;

    public Vip() {
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
        if (obj instanceof Vip) {
            Vip _o = (Vip) obj;

            if (!equals(m_ip, _o.getIp())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getIp() {
        return m_ip;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_ip == null ? 0 : m_ip.hashCode());

        return hash;
    }


    public Vip setIp(String ip) {
        m_ip = ip;
        return this;
    }

}
