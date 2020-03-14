package com.ctrip.zeus.model.model;

public class LoadBalancingMethod {
    private String m_type;

    private String m_value;

    public LoadBalancingMethod() {
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
        if (obj instanceof LoadBalancingMethod) {
            LoadBalancingMethod _o = (LoadBalancingMethod) obj;

            if (!equals(m_type, _o.getType())) {
                return false;
            }

            if (!equals(m_value, _o.getValue())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getType() {
        return m_type;
    }

    public String getValue() {
        return m_value;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_type == null ? 0 : m_type.hashCode());
        hash = hash * 31 + (m_value == null ? 0 : m_value.hashCode());

        return hash;
    }


    public LoadBalancingMethod setType(String type) {
        m_type = type;
        return this;
    }

    public LoadBalancingMethod setValue(String value) {
        m_value = value;
        return this;
    }

}
