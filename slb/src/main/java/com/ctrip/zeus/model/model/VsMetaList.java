package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class VsMetaList {
    private Integer m_total;

    private List<VsMeta> m_vsMetas = new ArrayList<VsMeta>();

    public VsMetaList() {
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



    public VsMetaList addVsMeta(VsMeta vsMeta) {
        m_vsMetas.add(vsMeta);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VsMetaList) {
            VsMetaList _o = (VsMetaList) obj;

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_vsMetas, _o.getVsMetas())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Integer getTotal() {
        return m_total;
    }

    public List<VsMeta> getVsMetas() {
        return m_vsMetas;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_vsMetas == null ? 0 : m_vsMetas.hashCode());

        return hash;
    }



    public VsMetaList setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
