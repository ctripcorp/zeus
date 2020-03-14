package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class TrafficPolicy {
    private Long m_id;

    private Integer m_version;

    private String m_name;

    private java.util.Date m_createdTime;

    private List<PolicyVirtualServer> m_policyVirtualServers = new ArrayList<PolicyVirtualServer>();

    private List<TrafficControl> m_controls = new ArrayList<TrafficControl>();

    private List<Rule> m_ruleSet = new ArrayList<Rule>();

    public TrafficPolicy() {
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



    public TrafficPolicy addPolicyVirtualServer(PolicyVirtualServer policyVirtualServer) {
        m_policyVirtualServers.add(policyVirtualServer);
        return this;
    }

    public TrafficPolicy addRule(Rule rule) {
        m_ruleSet.add(rule);
        return this;
    }

    public TrafficPolicy addTrafficControl(TrafficControl trafficControl) {
        m_controls.add(trafficControl);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TrafficPolicy) {
            TrafficPolicy _o = (TrafficPolicy) obj;

            if (!equals(m_id, _o.getId())) {
                return false;
            }

            if (!equals(m_version, _o.getVersion())) {
                return false;
            }

            if (!equals(m_name, _o.getName())) {
                return false;
            }

            if (!equals(m_createdTime, _o.getCreatedTime())) {
                return false;
            }

            if (!equals(m_policyVirtualServers, _o.getPolicyVirtualServers())) {
                return false;
            }

            if (!equals(m_controls, _o.getControls())) {
                return false;
            }

            if (!equals(m_ruleSet, _o.getRuleSet())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<TrafficControl> getControls() {
        return m_controls;
    }

    public java.util.Date getCreatedTime() {
        return m_createdTime;
    }

    public Long getId() {
        return m_id;
    }

    public String getName() {
        return m_name;
    }

    public List<PolicyVirtualServer> getPolicyVirtualServers() {
        return m_policyVirtualServers;
    }

    public List<Rule> getRuleSet() {
        return m_ruleSet;
    }

    public Integer getVersion() {
        return m_version;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());
        hash = hash * 31 + (m_version == null ? 0 : m_version.hashCode());
        hash = hash * 31 + (m_name == null ? 0 : m_name.hashCode());
        hash = hash * 31 + (m_createdTime == null ? 0 : m_createdTime.hashCode());
        hash = hash * 31 + (m_policyVirtualServers == null ? 0 : m_policyVirtualServers.hashCode());
        hash = hash * 31 + (m_controls == null ? 0 : m_controls.hashCode());
        hash = hash * 31 + (m_ruleSet == null ? 0 : m_ruleSet.hashCode());

        return hash;
    }



    public TrafficPolicy setCreatedTime(java.util.Date createdTime) {
        m_createdTime = createdTime;
        return this;
    }

    public TrafficPolicy setId(Long id) {
        m_id = id;
        return this;
    }

    public TrafficPolicy setName(String name) {
        m_name = name;
        return this;
    }

    public TrafficPolicy setVersion(Integer version) {
        m_version = version;
        return this;
    }

}
