package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class IdcMetaList {
    private Integer m_total;

    private List<IdcMeta> m_idcMetas = new ArrayList<IdcMeta>();

    public IdcMetaList() {
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



    public IdcMetaList addIdcMeta(IdcMeta idcMeta) {
        m_idcMetas.add(idcMeta);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IdcMetaList) {
            IdcMetaList _o = (IdcMetaList) obj;

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_idcMetas, _o.getIdcMetas())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<IdcMeta> getIdcMetas() {
        return m_idcMetas;
    }

    public Integer getTotal() {
        return m_total;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_idcMetas == null ? 0 : m_idcMetas.hashCode());

        return hash;
    }



    public IdcMetaList setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
