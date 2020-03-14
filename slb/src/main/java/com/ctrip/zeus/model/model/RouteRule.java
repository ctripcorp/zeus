package com.ctrip.zeus.model.model;

public class RouteRule {
    private String m_type;

    private String m_op;

    private String m_key1;

    private String m_value1;

    private Boolean m_flag1;

    private String m_key2;

    private String m_value2;

    private Boolean m_flag2;

    private String m_key3;

    private String m_value3;

    private Boolean m_flag3;

    private String m_key4;

    private String m_value4;

    private Boolean m_flag4;

    public RouteRule() {
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
        if (obj instanceof RouteRule) {
            RouteRule _o = (RouteRule) obj;

            if (!equals(m_type, _o.getType())) {
                return false;
            }

            if (!equals(m_op, _o.getOp())) {
                return false;
            }

            if (!equals(m_key1, _o.getKey1())) {
                return false;
            }

            if (!equals(m_value1, _o.getValue1())) {
                return false;
            }

            if (!equals(m_flag1, _o.getFlag1())) {
                return false;
            }

            if (!equals(m_key2, _o.getKey2())) {
                return false;
            }

            if (!equals(m_value2, _o.getValue2())) {
                return false;
            }

            if (!equals(m_flag2, _o.getFlag2())) {
                return false;
            }

            if (!equals(m_key3, _o.getKey3())) {
                return false;
            }

            if (!equals(m_value3, _o.getValue3())) {
                return false;
            }

            if (!equals(m_flag3, _o.getFlag3())) {
                return false;
            }

            if (!equals(m_key4, _o.getKey4())) {
                return false;
            }

            if (!equals(m_value4, _o.getValue4())) {
                return false;
            }

            if (!equals(m_flag4, _o.getFlag4())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Boolean getFlag1() {
        return m_flag1;
    }

    public Boolean getFlag2() {
        return m_flag2;
    }

    public Boolean getFlag3() {
        return m_flag3;
    }

    public Boolean getFlag4() {
        return m_flag4;
    }

    public String getKey1() {
        return m_key1;
    }

    public String getKey2() {
        return m_key2;
    }

    public String getKey3() {
        return m_key3;
    }

    public String getKey4() {
        return m_key4;
    }

    public String getOp() {
        return m_op;
    }

    public String getType() {
        return m_type;
    }

    public String getValue1() {
        return m_value1;
    }

    public String getValue2() {
        return m_value2;
    }

    public String getValue3() {
        return m_value3;
    }

    public String getValue4() {
        return m_value4;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_type == null ? 0 : m_type.hashCode());
        hash = hash * 31 + (m_op == null ? 0 : m_op.hashCode());
        hash = hash * 31 + (m_key1 == null ? 0 : m_key1.hashCode());
        hash = hash * 31 + (m_value1 == null ? 0 : m_value1.hashCode());
        hash = hash * 31 + (m_flag1 == null ? 0 : m_flag1.hashCode());
        hash = hash * 31 + (m_key2 == null ? 0 : m_key2.hashCode());
        hash = hash * 31 + (m_value2 == null ? 0 : m_value2.hashCode());
        hash = hash * 31 + (m_flag2 == null ? 0 : m_flag2.hashCode());
        hash = hash * 31 + (m_key3 == null ? 0 : m_key3.hashCode());
        hash = hash * 31 + (m_value3 == null ? 0 : m_value3.hashCode());
        hash = hash * 31 + (m_flag3 == null ? 0 : m_flag3.hashCode());
        hash = hash * 31 + (m_key4 == null ? 0 : m_key4.hashCode());
        hash = hash * 31 + (m_value4 == null ? 0 : m_value4.hashCode());
        hash = hash * 31 + (m_flag4 == null ? 0 : m_flag4.hashCode());

        return hash;
    }

    public boolean isFlag1() {
        return m_flag1 != null && m_flag1.booleanValue();
    }

    public boolean isFlag2() {
        return m_flag2 != null && m_flag2.booleanValue();
    }

    public boolean isFlag3() {
        return m_flag3 != null && m_flag3.booleanValue();
    }

    public boolean isFlag4() {
        return m_flag4 != null && m_flag4.booleanValue();
    }

    public RouteRule setFlag1(Boolean flag1) {
        m_flag1 = flag1;
        return this;
    }

    public RouteRule setFlag2(Boolean flag2) {
        m_flag2 = flag2;
        return this;
    }

    public RouteRule setFlag3(Boolean flag3) {
        m_flag3 = flag3;
        return this;
    }

    public RouteRule setFlag4(Boolean flag4) {
        m_flag4 = flag4;
        return this;
    }

    public RouteRule setKey1(String key1) {
        m_key1 = key1;
        return this;
    }

    public RouteRule setKey2(String key2) {
        m_key2 = key2;
        return this;
    }

    public RouteRule setKey3(String key3) {
        m_key3 = key3;
        return this;
    }

    public RouteRule setKey4(String key4) {
        m_key4 = key4;
        return this;
    }

    public RouteRule setOp(String op) {
        m_op = op;
        return this;
    }

    public RouteRule setType(String type) {
        m_type = type;
        return this;
    }

    public RouteRule setValue1(String value1) {
        m_value1 = value1;
        return this;
    }

    public RouteRule setValue2(String value2) {
        m_value2 = value2;
        return this;
    }

    public RouteRule setValue3(String value3) {
        m_value3 = value3;
        return this;
    }

    public RouteRule setValue4(String value4) {
        m_value4 = value4;
        return this;
    }

}
