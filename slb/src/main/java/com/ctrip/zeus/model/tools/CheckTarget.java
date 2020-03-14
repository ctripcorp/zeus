package com.ctrip.zeus.model.tools;

import java.util.ArrayList;
import java.util.List;

public class CheckTarget {
    private String m_protocol;

    private String m_ip;

    private Integer m_port;

    private String m_host;

    private String m_agent;

    private String m_uri;

    private Long m_groupId;

    private Long m_vsId;

    private List<Header> m_headers = new ArrayList<Header>();

    public CheckTarget() {
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



    public CheckTarget addHeader(Header header) {
        m_headers.add(header);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CheckTarget) {
            CheckTarget _o = (CheckTarget) obj;

            if (!equals(m_protocol, _o.getProtocol())) {
                return false;
            }

            if (!equals(m_ip, _o.getIp())) {
                return false;
            }

            if (!equals(m_port, _o.getPort())) {
                return false;
            }

            if (!equals(m_host, _o.getHost())) {
                return false;
            }

            if (!equals(m_agent, _o.getAgent())) {
                return false;
            }

            if (!equals(m_uri, _o.getUri())) {
                return false;
            }

            if (!equals(m_groupId, _o.getGroupId())) {
                return false;
            }

            if (!equals(m_vsId, _o.getVsId())) {
                return false;
            }

            if (!equals(m_headers, _o.getHeaders())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getAgent() {
        return m_agent;
    }

    public Long getGroupId() {
        return m_groupId;
    }

    public List<Header> getHeaders() {
        return m_headers;
    }

    public String getHost() {
        return m_host;
    }

    public String getIp() {
        return m_ip;
    }

    public Integer getPort() {
        return m_port;
    }

    public String getProtocol() {
        return m_protocol;
    }

    public String getUri() {
        return m_uri;
    }

    public Long getVsId() {
        return m_vsId;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_protocol == null ? 0 : m_protocol.hashCode());
        hash = hash * 31 + (m_ip == null ? 0 : m_ip.hashCode());
        hash = hash * 31 + (m_port == null ? 0 : m_port.hashCode());
        hash = hash * 31 + (m_host == null ? 0 : m_host.hashCode());
        hash = hash * 31 + (m_agent == null ? 0 : m_agent.hashCode());
        hash = hash * 31 + (m_uri == null ? 0 : m_uri.hashCode());
        hash = hash * 31 + (m_groupId == null ? 0 : m_groupId.hashCode());
        hash = hash * 31 + (m_vsId == null ? 0 : m_vsId.hashCode());
        hash = hash * 31 + (m_headers == null ? 0 : m_headers.hashCode());

        return hash;
    }



    public CheckTarget setAgent(String agent) {
        m_agent = agent;
        return this;
    }

    public CheckTarget setGroupId(Long groupId) {
        m_groupId = groupId;
        return this;
    }

    public CheckTarget setHost(String host) {
        m_host = host;
        return this;
    }

    public CheckTarget setIp(String ip) {
        m_ip = ip;
        return this;
    }

    public CheckTarget setPort(Integer port) {
        m_port = port;
        return this;
    }

    public CheckTarget setProtocol(String protocol) {
        m_protocol = protocol;
        return this;
    }

    public CheckTarget setUri(String uri) {
        m_uri = uri;
        return this;
    }

    public CheckTarget setVsId(Long vsId) {
        m_vsId = vsId;
        return this;
    }

}
