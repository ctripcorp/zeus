package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class SlbServerQpsList {
    private Integer m_total;

    private List<SlbServerQps> m_slbServerQpses = new ArrayList<SlbServerQps>();

    public SlbServerQpsList() {
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



    public SlbServerQpsList addSlbServerQps(SlbServerQps slbServerQps) {
        m_slbServerQpses.add(slbServerQps);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SlbServerQpsList) {
            SlbServerQpsList _o = (SlbServerQpsList) obj;

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_slbServerQpses, _o.getSlbServerQpses())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<SlbServerQps> getSlbServerQpses() {
        return m_slbServerQpses;
    }

    public Integer getTotal() {
        return m_total;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_slbServerQpses == null ? 0 : m_slbServerQpses.hashCode());

        return hash;
    }



    public SlbServerQpsList setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
