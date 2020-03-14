package com.ctrip.zeus.model.tools;

import java.util.ArrayList;
import java.util.List;

public class VsPingList {
    private List<VsPing> m_vses = new ArrayList<VsPing>();

    public VsPingList() {
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



    public VsPingList addVsPing(VsPing vsPing) {
        m_vses.add(vsPing);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VsPingList) {
            VsPingList _o = (VsPingList) obj;

            if (!equals(m_vses, _o.getVses())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<VsPing> getVses() {
        return m_vses;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_vses == null ? 0 : m_vses.hashCode());

        return hash;
    }



}
