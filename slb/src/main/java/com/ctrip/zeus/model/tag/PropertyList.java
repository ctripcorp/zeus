package com.ctrip.zeus.model.tag;

import java.util.ArrayList;
import java.util.List;

public class PropertyList {
    private Integer m_total;

    private List<Property> m_properties = new ArrayList<Property>();

    public PropertyList() {
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



    public PropertyList addProperty(Property property) {
        m_properties.add(property);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PropertyList) {
            PropertyList _o = (PropertyList) obj;

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_properties, _o.getProperties())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<Property> getProperties() {
        return m_properties;
    }

    public Integer getTotal() {
        return m_total;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_properties == null ? 0 : m_properties.hashCode());

        return hash;
    }



    public PropertyList setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
