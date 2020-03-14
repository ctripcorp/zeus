package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class Dr {
    private Long m_id;

    private String m_name;

    private Integer m_version;

    private java.util.Date m_createdTime;

    private List<DrTraffic> m_drTraffics = new ArrayList<DrTraffic>();

    public Dr() {
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



    public Dr addDrTraffic(DrTraffic drTraffic) {
        m_drTraffics.add(drTraffic);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Dr) {
            Dr _o = (Dr) obj;

            if (!equals(m_id, _o.getId())) {
                return false;
            }

            if (!equals(m_name, _o.getName())) {
                return false;
            }

            if (!equals(m_version, _o.getVersion())) {
                return false;
            }

            if (!equals(m_createdTime, _o.getCreatedTime())) {
                return false;
            }

            if (!equals(m_drTraffics, _o.getDrTraffics())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public java.util.Date getCreatedTime() {
        return m_createdTime;
    }

    public List<DrTraffic> getDrTraffics() {
        return m_drTraffics;
    }

    public Long getId() {
        return m_id;
    }

    public String getName() {
        return m_name;
    }

    public Integer getVersion() {
        return m_version;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());
        hash = hash * 31 + (m_name == null ? 0 : m_name.hashCode());
        hash = hash * 31 + (m_version == null ? 0 : m_version.hashCode());
        hash = hash * 31 + (m_createdTime == null ? 0 : m_createdTime.hashCode());
        hash = hash * 31 + (m_drTraffics == null ? 0 : m_drTraffics.hashCode());

        return hash;
    }


    public Dr setCreatedTime(java.util.Date createdTime) {
        m_createdTime = createdTime;
        return this;
    }

    public Dr setId(Long id) {
        m_id = id;
        return this;
    }

    public Dr setName(String name) {
        m_name = name;
        return this;
    }

    public Dr setVersion(Integer version) {
        m_version = version;
        return this;
    }

}
