package com.ctrip.zeus.model.model;

public class SoaDefaultConfig {
    private Integer m_maxBodySize;

    private Integer m_minBodySize;

    private Integer m_maxReadTimeout;

    private Integer m_minReadTimeout;

    public SoaDefaultConfig() {
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
        if (obj instanceof SoaDefaultConfig) {
            SoaDefaultConfig _o = (SoaDefaultConfig) obj;

            if (!equals(m_maxBodySize, _o.getMaxBodySize())) {
                return false;
            }

            if (!equals(m_minBodySize, _o.getMinBodySize())) {
                return false;
            }

            if (!equals(m_maxReadTimeout, _o.getMaxReadTimeout())) {
                return false;
            }

            if (!equals(m_minReadTimeout, _o.getMinReadTimeout())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Integer getMaxBodySize() {
        return m_maxBodySize;
    }

    public Integer getMaxReadTimeout() {
        return m_maxReadTimeout;
    }

    public Integer getMinBodySize() {
        return m_minBodySize;
    }

    public Integer getMinReadTimeout() {
        return m_minReadTimeout;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_maxBodySize == null ? 0 : m_maxBodySize.hashCode());
        hash = hash * 31 + (m_minBodySize == null ? 0 : m_minBodySize.hashCode());
        hash = hash * 31 + (m_maxReadTimeout == null ? 0 : m_maxReadTimeout.hashCode());
        hash = hash * 31 + (m_minReadTimeout == null ? 0 : m_minReadTimeout.hashCode());

        return hash;
    }



    public SoaDefaultConfig setMaxBodySize(Integer maxBodySize) {
        m_maxBodySize = maxBodySize;
        return this;
    }

    public SoaDefaultConfig setMaxReadTimeout(Integer maxReadTimeout) {
        m_maxReadTimeout = maxReadTimeout;
        return this;
    }

    public SoaDefaultConfig setMinBodySize(Integer minBodySize) {
        m_minBodySize = minBodySize;
        return this;
    }

    public SoaDefaultConfig setMinReadTimeout(Integer minReadTimeout) {
        m_minReadTimeout = minReadTimeout;
        return this;
    }

}
