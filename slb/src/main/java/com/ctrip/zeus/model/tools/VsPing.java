package com.ctrip.zeus.model.tools;

import java.util.ArrayList;
import java.util.List;

public class VsPing {
    private Long m_vsId;

    private List<Domain> m_domains = new ArrayList<Domain>();

    public VsPing() {
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



    public VsPing addDomain(Domain domain) {
        m_domains.add(domain);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VsPing) {
            VsPing _o = (VsPing) obj;

            if (!equals(m_vsId, _o.getVsId())) {
                return false;
            }

            if (!equals(m_domains, _o.getDomains())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<Domain> getDomains() {
        return m_domains;
    }

    public Long getVsId() {
        return m_vsId;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_vsId == null ? 0 : m_vsId.hashCode());
        hash = hash * 31 + (m_domains == null ? 0 : m_domains.hashCode());

        return hash;
    }



    public VsPing setVsId(Long vsId) {
        m_vsId = vsId;
        return this;
    }

}
