package com.ctrip.zeus.model.status;

public class GroupServerStatus {
    private String m_ip;

    private String m_hostName;

    private Integer m_port;

    private Boolean m_member;

    private Boolean m_server;

    private Boolean m_pull;

    private Boolean m_healthy;

    private Integer m_weight;

    private Boolean m_up;

    private Boolean m_online;

    private String m_nextStatus;

    public GroupServerStatus() {
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
        if (obj instanceof GroupServerStatus) {
            GroupServerStatus _o = (GroupServerStatus) obj;

            if (!equals(m_ip, _o.getIp())) {
                return false;
            }

            if (!equals(m_hostName, _o.getHostName())) {
                return false;
            }

            if (!equals(m_port, _o.getPort())) {
                return false;
            }

            if (!equals(m_member, _o.getMember())) {
                return false;
            }

            if (!equals(m_server, _o.getServer())) {
                return false;
            }

            if (!equals(m_pull, _o.getPull())) {
                return false;
            }

            if (!equals(m_healthy, _o.getHealthy())) {
                return false;
            }

            if (!equals(m_weight, _o.getWeight())) {
                return false;
            }

            if (!equals(m_up, _o.getUp())) {
                return false;
            }

            if (!equals(m_online, _o.getOnline())) {
                return false;
            }

            if (!equals(m_nextStatus, _o.getNextStatus())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Boolean getHealthy() {
        return m_healthy;
    }

    public String getHostName() {
        return m_hostName;
    }

    public String getIp() {
        return m_ip;
    }

    public Boolean getMember() {
        return m_member;
    }

    public String getNextStatus() {
        return m_nextStatus;
    }

    public Boolean getOnline() {
        return m_online;
    }

    public Integer getPort() {
        return m_port;
    }

    public Boolean getPull() {
        return m_pull;
    }

    public Boolean getServer() {
        return m_server;
    }

    public Boolean getUp() {
        return m_up;
    }

    public Integer getWeight() {
        return m_weight;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_ip == null ? 0 : m_ip.hashCode());
        hash = hash * 31 + (m_hostName == null ? 0 : m_hostName.hashCode());
        hash = hash * 31 + (m_port == null ? 0 : m_port.hashCode());
        hash = hash * 31 + (m_member == null ? 0 : m_member.hashCode());
        hash = hash * 31 + (m_server == null ? 0 : m_server.hashCode());
        hash = hash * 31 + (m_pull == null ? 0 : m_pull.hashCode());
        hash = hash * 31 + (m_healthy == null ? 0 : m_healthy.hashCode());
        hash = hash * 31 + (m_weight == null ? 0 : m_weight.hashCode());
        hash = hash * 31 + (m_up == null ? 0 : m_up.hashCode());
        hash = hash * 31 + (m_online == null ? 0 : m_online.hashCode());
        hash = hash * 31 + (m_nextStatus == null ? 0 : m_nextStatus.hashCode());

        return hash;
    }

    public boolean isHealthy() {
        return m_healthy != null && m_healthy.booleanValue();
    }

    public boolean isMember() {
        return m_member != null && m_member.booleanValue();
    }

    public boolean isOnline() {
        return m_online != null && m_online.booleanValue();
    }

    public boolean isPull() {
        return m_pull != null && m_pull.booleanValue();
    }

    public boolean isServer() {
        return m_server != null && m_server.booleanValue();
    }

    public boolean isUp() {
        return m_up != null && m_up.booleanValue();
    }



    public GroupServerStatus setHealthy(Boolean healthy) {
        m_healthy = healthy;
        return this;
    }

    public GroupServerStatus setHostName(String hostName) {
        m_hostName = hostName;
        return this;
    }

    public GroupServerStatus setIp(String ip) {
        m_ip = ip;
        return this;
    }

    public GroupServerStatus setMember(Boolean member) {
        m_member = member;
        return this;
    }

    public GroupServerStatus setNextStatus(String nextStatus) {
        m_nextStatus = nextStatus;
        return this;
    }

    public GroupServerStatus setOnline(Boolean online) {
        m_online = online;
        return this;
    }

    public GroupServerStatus setPort(Integer port) {
        m_port = port;
        return this;
    }

    public GroupServerStatus setPull(Boolean pull) {
        m_pull = pull;
        return this;
    }

    public GroupServerStatus setServer(Boolean server) {
        m_server = server;
        return this;
    }

    public GroupServerStatus setUp(Boolean up) {
        m_up = up;
        return this;
    }

    public GroupServerStatus setWeight(Integer weight) {
        m_weight = weight;
        return this;
    }

}
