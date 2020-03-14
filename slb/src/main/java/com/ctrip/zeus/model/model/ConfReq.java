package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class ConfReq {
    private List<ConfSlbName> m_confSlbNames = new ArrayList<ConfSlbName>();

    private List<ConfGroupName> m_confGroupNames = new ArrayList<ConfGroupName>();

    public ConfReq() {
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



    public ConfReq addConfGroupName(ConfGroupName confGroupName) {
        m_confGroupNames.add(confGroupName);
        return this;
    }

    public ConfReq addConfSlbName(ConfSlbName confSlbName) {
        m_confSlbNames.add(confSlbName);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConfReq) {
            ConfReq _o = (ConfReq) obj;

            if (!equals(m_confSlbNames, _o.getConfSlbNames())) {
                return false;
            }

            if (!equals(m_confGroupNames, _o.getConfGroupNames())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<ConfGroupName> getConfGroupNames() {
        return m_confGroupNames;
    }

    public List<ConfSlbName> getConfSlbNames() {
        return m_confSlbNames;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_confSlbNames == null ? 0 : m_confSlbNames.hashCode());
        hash = hash * 31 + (m_confGroupNames == null ? 0 : m_confGroupNames.hashCode());

        return hash;
    }



}
