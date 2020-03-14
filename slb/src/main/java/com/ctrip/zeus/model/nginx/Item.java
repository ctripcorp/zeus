package com.ctrip.zeus.model.nginx;

public class Item {
    private Integer m_index;

    private String m_upstream;

    private String m_name;

    private String m_status;

    private Integer m_rise;

    private Integer m_fall;

    private String m_type;

    private Integer m_port;

    public Item() {
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
        if (obj instanceof Item) {
            Item _o = (Item) obj;

            if (!equals(m_index, _o.getIndex())) {
                return false;
            }

            if (!equals(m_upstream, _o.getUpstream())) {
                return false;
            }

            if (!equals(m_name, _o.getName())) {
                return false;
            }

            if (!equals(m_status, _o.getStatus())) {
                return false;
            }

            if (!equals(m_rise, _o.getRise())) {
                return false;
            }

            if (!equals(m_fall, _o.getFall())) {
                return false;
            }

            if (!equals(m_type, _o.getType())) {
                return false;
            }

            if (!equals(m_port, _o.getPort())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Integer getFall() {
        return m_fall;
    }

    public Integer getIndex() {
        return m_index;
    }

    public String getName() {
        return m_name;
    }

    public Integer getPort() {
        return m_port;
    }

    public Integer getRise() {
        return m_rise;
    }

    public String getStatus() {
        return m_status;
    }

    public String getType() {
        return m_type;
    }

    public String getUpstream() {
        return m_upstream;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_index == null ? 0 : m_index.hashCode());
        hash = hash * 31 + (m_upstream == null ? 0 : m_upstream.hashCode());
        hash = hash * 31 + (m_name == null ? 0 : m_name.hashCode());
        hash = hash * 31 + (m_status == null ? 0 : m_status.hashCode());
        hash = hash * 31 + (m_rise == null ? 0 : m_rise.hashCode());
        hash = hash * 31 + (m_fall == null ? 0 : m_fall.hashCode());
        hash = hash * 31 + (m_type == null ? 0 : m_type.hashCode());
        hash = hash * 31 + (m_port == null ? 0 : m_port.hashCode());

        return hash;
    }



    public Item setFall(Integer fall) {
        m_fall = fall;
        return this;
    }

    public Item setIndex(Integer index) {
        m_index = index;
        return this;
    }

    public Item setName(String name) {
        m_name = name;
        return this;
    }

    public Item setPort(Integer port) {
        m_port = port;
        return this;
    }

    public Item setRise(Integer rise) {
        m_rise = rise;
        return this;
    }

    public Item setStatus(String status) {
        m_status = status;
        return this;
    }

    public Item setType(String type) {
        m_type = type;
        return this;
    }

    public Item setUpstream(String upstream) {
        m_upstream = upstream;
        return this;
    }

}
