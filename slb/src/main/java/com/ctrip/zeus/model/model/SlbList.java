package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class SlbList {
    private Integer m_total;

    private List<Slb> m_slbs = new ArrayList<Slb>();

    public SlbList() {
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



    public SlbList addSlb(Slb slb) {
        m_slbs.add(slb);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SlbList) {
            SlbList _o = (SlbList) obj;

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_slbs, _o.getSlbs())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<Slb> getSlbs() {
        return m_slbs;
    }

    public Integer getTotal() {
        return m_total;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_slbs == null ? 0 : m_slbs.hashCode());

        return hash;
    }



    public SlbList setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
