package com.ctrip.zeus.model.model;

public class SoaConfig {
    private Integer m_clientMaxBodySize;

    private Integer m_clientBodyBufferSize;

    private Integer m_proxyReadTimeout;

    public SoaConfig() {
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
        if (obj instanceof SoaConfig) {
            SoaConfig _o = (SoaConfig) obj;

            if (!equals(m_clientMaxBodySize, _o.getClientMaxBodySize())) {
                return false;
            }

            if (!equals(m_clientBodyBufferSize, _o.getClientBodyBufferSize())) {
                return false;
            }

            if (!equals(m_proxyReadTimeout, _o.getProxyReadTimeout())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Integer getClientBodyBufferSize() {
        return m_clientBodyBufferSize;
    }

    public Integer getClientMaxBodySize() {
        return m_clientMaxBodySize;
    }

    public Integer getProxyReadTimeout() {
        return m_proxyReadTimeout;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_clientMaxBodySize == null ? 0 : m_clientMaxBodySize.hashCode());
        hash = hash * 31 + (m_clientBodyBufferSize == null ? 0 : m_clientBodyBufferSize.hashCode());
        hash = hash * 31 + (m_proxyReadTimeout == null ? 0 : m_proxyReadTimeout.hashCode());

        return hash;
    }



    public SoaConfig setClientBodyBufferSize(Integer clientBodyBufferSize) {
        m_clientBodyBufferSize = clientBodyBufferSize;
        return this;
    }

    public SoaConfig setClientMaxBodySize(Integer clientMaxBodySize) {
        m_clientMaxBodySize = clientMaxBodySize;
        return this;
    }

    public SoaConfig setProxyReadTimeout(Integer proxyReadTimeout) {
        m_proxyReadTimeout = proxyReadTimeout;
        return this;
    }

}
