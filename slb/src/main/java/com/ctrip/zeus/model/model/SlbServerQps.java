package com.ctrip.zeus.model.model;

public class SlbServerQps {
    private String m_ip;

    private Double m_qps;

    public SlbServerQps() {
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
        if (obj instanceof SlbServerQps) {
            SlbServerQps _o = (SlbServerQps) obj;

            if (!equals(m_ip, _o.getIp())) {
                return false;
            }

            if (!equals(m_qps, _o.getQps())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getIp() {
        return m_ip;
    }

    public Double getQps() {
        return m_qps;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_ip == null ? 0 : m_ip.hashCode());
        hash = hash * 31 + (m_qps == null ? 0 : m_qps.hashCode());

        return hash;
    }



    public SlbServerQps setIp(String ip) {
        m_ip = ip;
        return this;
    }

    public SlbServerQps setQps(Double qps) {
        m_qps = qps;
        return this;
    }

}
