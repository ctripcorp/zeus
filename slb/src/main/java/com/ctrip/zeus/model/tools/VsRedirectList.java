package com.ctrip.zeus.model.tools;

import java.util.ArrayList;
import java.util.List;

public class VsRedirectList {
    private Integer m_total;

    private List<VsRedirect> m_redirects = new ArrayList<VsRedirect>();

    public VsRedirectList() {
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


    public VsRedirectList addVsRedirect(VsRedirect vsMigration) {
        m_redirects.add(vsMigration);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VsRedirectList) {
            VsRedirectList _o = (VsRedirectList) obj;

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_redirects, _o.getVsRedirect())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<VsRedirect> getVsRedirect() {
        return m_redirects;
    }

    public Integer getTotal() {
        return m_total;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_redirects == null ? 0 : m_redirects.hashCode());

        return hash;
    }



    public VsRedirectList setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
