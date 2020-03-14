package com.ctrip.zeus.model.model;

public class Domain {
    private String m_name;

    public Domain() {
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
        if (obj instanceof Domain) {
            Domain _o = (Domain) obj;

            if (!equals(m_name, _o.getName())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getName() {
        return m_name;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_name == null ? 0 : m_name.hashCode());

        return hash;
    }


    public Domain setName(String name) {
        m_name = name;
        return this;
    }

}
