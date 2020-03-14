package com.ctrip.zeus.model.tag;

public class Property {
    private String m_name;

    private String m_value;

    public Property() {
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
        if (obj instanceof Property) {
            Property _o = (Property) obj;

            if (!equals(m_name, _o.getName())) {
                return false;
            }

            if (!equals(m_value, _o.getValue())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getName() {
        return m_name;
    }

    public String getValue() {
        return m_value;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_name == null ? 0 : m_name.hashCode());
        hash = hash * 31 + (m_value == null ? 0 : m_value.hashCode());

        return hash;
    }



    public Property setName(String name) {
        m_name = name;
        return this;
    }

    public Property setValue(String value) {
        m_value = value;
        return this;
    }

}
