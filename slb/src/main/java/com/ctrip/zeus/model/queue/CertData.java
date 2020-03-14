package com.ctrip.zeus.model.queue;

public class CertData {
    private String m_cid;

    private String m_domain;

    private Long m_certId;

    public CertData() {
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
        if (obj instanceof CertData) {
            CertData _o = (CertData) obj;

            if (!equals(m_cid, _o.getCid())) {
                return false;
            }

            if (!equals(m_domain, _o.getDomain())) {
                return false;
            }

            if (!equals(m_certId, _o.getCertId())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Long getCertId() {
        return m_certId;
    }

    public String getCid() {
        return m_cid;
    }

    public String getDomain() {
        return m_domain;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_cid == null ? 0 : m_cid.hashCode());
        hash = hash * 31 + (m_domain == null ? 0 : m_domain.hashCode());
        hash = hash * 31 + (m_certId == null ? 0 : m_certId.hashCode());

        return hash;
    }



    public CertData setCertId(Long certId) {
        m_certId = certId;
        return this;
    }

    public CertData setCid(String cid) {
        m_cid = cid;
        return this;
    }

    public CertData setDomain(String domain) {
        m_domain = domain;
        return this;
    }

}
