package com.ctrip.zeus.model.model;

public class ConfSlbName {
    private String m_slbname;

    public ConfSlbName() {
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



    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConfSlbName) {
            ConfSlbName _o = (ConfSlbName) obj;

            if (!equals(m_slbname, _o.getSlbname())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getSlbname() {
        return m_slbname;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_slbname == null ? 0 : m_slbname.hashCode());

        return hash;
    }


    public ConfSlbName setSlbname(String slbname) {
        m_slbname = slbname;
        return this;
    }

}
