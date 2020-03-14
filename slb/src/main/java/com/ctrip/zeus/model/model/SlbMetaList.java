package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class SlbMetaList {
    private Integer m_total;

    private List<SlbMeta> m_slbMetas = new ArrayList<SlbMeta>();

    public SlbMetaList() {
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



    public SlbMetaList addSlbMeta(SlbMeta slbMeta) {
        m_slbMetas.add(slbMeta);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SlbMetaList) {
            SlbMetaList _o = (SlbMetaList) obj;

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_slbMetas, _o.getSlbMetas())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<SlbMeta> getSlbMetas() {
        return m_slbMetas;
    }

    public Integer getTotal() {
        return m_total;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_slbMetas == null ? 0 : m_slbMetas.hashCode());

        return hash;
    }



    public SlbMetaList setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
