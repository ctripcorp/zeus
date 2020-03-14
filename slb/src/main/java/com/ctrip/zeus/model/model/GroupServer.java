package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class GroupServer {
    private Integer m_port;

    private Integer m_weight;

    private Integer m_maxFails;

    private Integer m_failTimeout;

    private Integer m_maxConns;

    private String m_hostName;

    private String m_ip;

    private List<Rule> m_ruleSet = new ArrayList<Rule>();

    public GroupServer() {
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



    public GroupServer addRule(Rule rule) {
        m_ruleSet.add(rule);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GroupServer) {
            GroupServer _o = (GroupServer) obj;

            if (!equals(m_port, _o.getPort())) {
                return false;
            }

            if (!equals(m_weight, _o.getWeight())) {
                return false;
            }

            if (!equals(m_maxFails, _o.getMaxFails())) {
                return false;
            }

            if (!equals(m_failTimeout, _o.getFailTimeout())) {
                return false;
            }

            if (!equals(m_maxConns, _o.getMaxConns())) {
                return false;
            }

            if (!equals(m_hostName, _o.getHostName())) {
                return false;
            }

            if (!equals(m_ip, _o.getIp())) {
                return false;
            }

            if (!equals(m_ruleSet, _o.getRuleSet())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Integer getFailTimeout() {
        return m_failTimeout;
    }

    public String getHostName() {
        return m_hostName;
    }

    public String getIp() {
        return m_ip;
    }

    public Integer getMaxConns() {
        return m_maxConns;
    }

    public Integer getMaxFails() {
        return m_maxFails;
    }

    public Integer getPort() {
        return m_port;
    }

    public List<Rule> getRuleSet() {
        return m_ruleSet;
    }

    public Integer getWeight() {
        return m_weight;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_port == null ? 0 : m_port.hashCode());
        hash = hash * 31 + (m_weight == null ? 0 : m_weight.hashCode());
        hash = hash * 31 + (m_maxFails == null ? 0 : m_maxFails.hashCode());
        hash = hash * 31 + (m_failTimeout == null ? 0 : m_failTimeout.hashCode());
        hash = hash * 31 + (m_maxConns == null ? 0 : m_maxConns.hashCode());
        hash = hash * 31 + (m_hostName == null ? 0 : m_hostName.hashCode());
        hash = hash * 31 + (m_ip == null ? 0 : m_ip.hashCode());
        hash = hash * 31 + (m_ruleSet == null ? 0 : m_ruleSet.hashCode());

        return hash;
    }



    public GroupServer setFailTimeout(Integer failTimeout) {
        m_failTimeout = failTimeout;
        return this;
    }

    public GroupServer setHostName(String hostName) {
        m_hostName = hostName;
        return this;
    }

    public GroupServer setIp(String ip) {
        m_ip = ip;
        return this;
    }

    public GroupServer setMaxConns(Integer maxConns) {
        m_maxConns = maxConns;
        return this;
    }

    public GroupServer setMaxFails(Integer maxFails) {
        m_maxFails = maxFails;
        return this;
    }

    public GroupServer setPort(Integer port) {
        m_port = port;
        return this;
    }

    public GroupServer setWeight(Integer weight) {
        m_weight = weight;
        return this;
    }

}
