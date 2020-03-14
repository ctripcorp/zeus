package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class SbuMetaList {
    private Integer m_total;

    private List<SbuMeta> m_sbuMetas = new ArrayList<SbuMeta>();

    public SbuMetaList() {
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



    public SbuMetaList addSbuMeta(SbuMeta sbuMeta) {
        m_sbuMetas.add(sbuMeta);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SbuMetaList) {
            SbuMetaList _o = (SbuMetaList) obj;

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_sbuMetas, _o.getSbuMetas())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<SbuMeta> getSbuMetas() {
        return m_sbuMetas;
    }

    public Integer getTotal() {
        return m_total;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_sbuMetas == null ? 0 : m_sbuMetas.hashCode());

        return hash;
    }



    public SbuMetaList setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
