package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private Long m_id;

    private String m_name;

    private String m_type;

    private String m_appId;

    private Integer m_version;

    private Boolean m_ssl;

    private Boolean m_virtual;

    private java.util.Date m_createdTime;

    private List<GroupVirtualServer> m_groupVirtualServers = new ArrayList<GroupVirtualServer>();

    private HealthCheck m_healthCheck;

    private LoadBalancingMethod m_loadBalancingMethod;

    private List<GroupServer> m_groupServers = new ArrayList<GroupServer>();

    private List<Rule> m_ruleSet = new ArrayList<Rule>();

    public Group() {
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



    public Group addGroupServer(GroupServer groupServer) {
        m_groupServers.add(groupServer);
        return this;
    }

    public Group addGroupVirtualServer(GroupVirtualServer groupVirtualServer) {
        m_groupVirtualServers.add(groupVirtualServer);
        return this;
    }

    public Group addRule(Rule rule) {
        m_ruleSet.add(rule);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Group) {
            Group _o = (Group) obj;

            if (!equals(m_id, _o.getId())) {
                return false;
            }

            if (!equals(m_name, _o.getName())) {
                return false;
            }

            if (!equals(m_type, _o.getType())) {
                return false;
            }

            if (!equals(m_appId, _o.getAppId())) {
                return false;
            }

            if (!equals(m_version, _o.getVersion())) {
                return false;
            }

            if (!equals(m_ssl, _o.getSsl())) {
                return false;
            }

            if (!equals(m_virtual, _o.getVirtual())) {
                return false;
            }

            if (!equals(m_createdTime, _o.getCreatedTime())) {
                return false;
            }

            if (!equals(m_groupVirtualServers, _o.getGroupVirtualServers())) {
                return false;
            }

            if (!equals(m_healthCheck, _o.getHealthCheck())) {
                return false;
            }

            if (!equals(m_loadBalancingMethod, _o.getLoadBalancingMethod())) {
                return false;
            }

            if (!equals(m_groupServers, _o.getGroupServers())) {
                return false;
            }

            if (!equals(m_ruleSet, _o.getRuleSet())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getAppId() {
        return m_appId;
    }

    public java.util.Date getCreatedTime() {
        return m_createdTime;
    }

    public List<GroupServer> getGroupServers() {
        return m_groupServers;
    }

    public List<GroupVirtualServer> getGroupVirtualServers() {
        return m_groupVirtualServers;
    }

    public HealthCheck getHealthCheck() {
        return m_healthCheck;
    }

    public Long getId() {
        return m_id;
    }

    public LoadBalancingMethod getLoadBalancingMethod() {
        return m_loadBalancingMethod;
    }

    public String getName() {
        return m_name;
    }

    public List<Rule> getRuleSet() {
        return m_ruleSet;
    }

    public Boolean getSsl() {
        return m_ssl;
    }

    public String getType() {
        return m_type;
    }

    public Integer getVersion() {
        return m_version;
    }

    public Boolean getVirtual() {
        return m_virtual;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());
        hash = hash * 31 + (m_name == null ? 0 : m_name.hashCode());
        hash = hash * 31 + (m_type == null ? 0 : m_type.hashCode());
        hash = hash * 31 + (m_appId == null ? 0 : m_appId.hashCode());
        hash = hash * 31 + (m_version == null ? 0 : m_version.hashCode());
        hash = hash * 31 + (m_ssl == null ? 0 : m_ssl.hashCode());
        hash = hash * 31 + (m_virtual == null ? 0 : m_virtual.hashCode());
        hash = hash * 31 + (m_createdTime == null ? 0 : m_createdTime.hashCode());
        hash = hash * 31 + (m_groupVirtualServers == null ? 0 : m_groupVirtualServers.hashCode());
        hash = hash * 31 + (m_healthCheck == null ? 0 : m_healthCheck.hashCode());
        hash = hash * 31 + (m_loadBalancingMethod == null ? 0 : m_loadBalancingMethod.hashCode());
        hash = hash * 31 + (m_groupServers == null ? 0 : m_groupServers.hashCode());
        hash = hash * 31 + (m_ruleSet == null ? 0 : m_ruleSet.hashCode());

        return hash;
    }

    public boolean isSsl() {
        return m_ssl != null && m_ssl.booleanValue();
    }

    public boolean isVirtual() {
        return m_virtual != null && m_virtual.booleanValue();
    }


    public Group setAppId(String appId) {
        m_appId = appId;
        return this;
    }

    public Group setCreatedTime(java.util.Date createdTime) {
        m_createdTime = createdTime;
        return this;
    }

    public Group setHealthCheck(HealthCheck healthCheck) {
        m_healthCheck = healthCheck;
        return this;
    }

    public Group setId(Long id) {
        m_id = id;
        return this;
    }

    public Group setLoadBalancingMethod(LoadBalancingMethod loadBalancingMethod) {
        m_loadBalancingMethod = loadBalancingMethod;
        return this;
    }

    public Group setName(String name) {
        m_name = name;
        return this;
    }

    public Group setSsl(Boolean ssl) {
        m_ssl = ssl;
        return this;
    }

    public Group setType(String type) {
        m_type = type;
        return this;
    }

    public Group setVersion(Integer version) {
        m_version = version;
        return this;
    }

    public Group setVirtual(Boolean virtual) {
        m_virtual = virtual;
        return this;
    }

}
