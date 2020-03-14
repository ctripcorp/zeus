package com.ctrip.zeus.model.model;

public class ConfGroupName {
    private String m_groupname;

    public ConfGroupName() {
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
        if (obj instanceof ConfGroupName) {
            ConfGroupName _o = (ConfGroupName) obj;

            if (!equals(m_groupname, _o.getGroupname())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getGroupname() {
        return m_groupname;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_groupname == null ? 0 : m_groupname.hashCode());

        return hash;
    }



    public ConfGroupName setGroupname(String groupname) {
        m_groupname = groupname;
        return this;
    }

}
