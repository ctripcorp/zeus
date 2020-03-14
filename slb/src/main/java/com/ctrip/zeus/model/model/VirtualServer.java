package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class VirtualServer {
    private String m_name;

    private Long m_id;

    private Boolean m_ssl;

    private Integer m_version;

    private List<Long> m_slbIds = new ArrayList<Long>();

    private Long m_slbId;

    private String m_port;

    private java.util.Date m_createdTime;

    private List<Domain> m_domains = new ArrayList<Domain>();

    private List<Rule> m_ruleSet = new ArrayList<Rule>();

    public VirtualServer() {
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



    public VirtualServer addDomain(Domain domain) {
        m_domains.add(domain);
        return this;
    }

    public VirtualServer addRule(Rule rule) {
        m_ruleSet.add(rule);
        return this;
    }

    public VirtualServer addValue(Long value) {
        m_slbIds.add(value);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VirtualServer) {
            VirtualServer _o = (VirtualServer) obj;

            if (!equals(m_name, _o.getName())) {
                return false;
            }

            if (!equals(m_id, _o.getId())) {
                return false;
            }

            if (!equals(m_ssl, _o.getSsl())) {
                return false;
            }

            if (!equals(m_version, _o.getVersion())) {
                return false;
            }

            if (!equals(m_slbIds, _o.getSlbIds())) {
                return false;
            }

            if (!equals(m_slbId, _o.getSlbId())) {
                return false;
            }

            if (!equals(m_port, _o.getPort())) {
                return false;
            }

            if (!equals(m_createdTime, _o.getCreatedTime())) {
                return false;
            }

            if (!equals(m_domains, _o.getDomains())) {
                return false;
            }

            if (!equals(m_ruleSet, _o.getRuleSet())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public java.util.Date getCreatedTime() {
        return m_createdTime;
    }

    public List<Domain> getDomains() {
        return m_domains;
    }

    public Long getId() {
        return m_id;
    }

    public String getName() {
        return m_name;
    }

    public String getPort() {
        return m_port;
    }

    public List<Rule> getRuleSet() {
        return m_ruleSet;
    }

    public Long getSlbId() {
        return m_slbId;
    }

    public List<Long> getSlbIds() {
        return m_slbIds;
    }

    public Boolean getSsl() {
        return m_ssl;
    }

    public Integer getVersion() {
        return m_version;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_name == null ? 0 : m_name.hashCode());
        hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());
        hash = hash * 31 + (m_ssl == null ? 0 : m_ssl.hashCode());
        hash = hash * 31 + (m_version == null ? 0 : m_version.hashCode());
        hash = hash * 31 + (m_slbIds == null ? 0 : m_slbIds.hashCode());
        hash = hash * 31 + (m_slbId == null ? 0 : m_slbId.hashCode());
        hash = hash * 31 + (m_port == null ? 0 : m_port.hashCode());
        hash = hash * 31 + (m_createdTime == null ? 0 : m_createdTime.hashCode());
        hash = hash * 31 + (m_domains == null ? 0 : m_domains.hashCode());
        hash = hash * 31 + (m_ruleSet == null ? 0 : m_ruleSet.hashCode());

        return hash;
    }

    public boolean isSsl() {
        return m_ssl != null && m_ssl.booleanValue();
    }


    public VirtualServer setCreatedTime(java.util.Date createdTime) {
        m_createdTime = createdTime;
        return this;
    }

    public VirtualServer setId(Long id) {
        m_id = id;
        return this;
    }

    public VirtualServer setName(String name) {
        m_name = name;
        return this;
    }

    public VirtualServer setPort(String port) {
        m_port = port;
        return this;
    }

    public VirtualServer setSlbId(Long slbId) {
        m_slbId = slbId;
        return this;
    }

    public VirtualServer setSsl(Boolean ssl) {
        m_ssl = ssl;
        return this;
    }

    public VirtualServer setVersion(Integer version) {
        m_version = version;
        return this;
    }

}
