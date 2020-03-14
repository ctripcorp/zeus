package com.ctrip.zeus.model.model;

public class HealthCheck {
    private Integer m_intervals;

    private Integer m_fails;

    private Integer m_passes;

    private Integer m_timeout;

    private String m_uri;

    public HealthCheck() {
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
        if (obj instanceof HealthCheck) {
            HealthCheck _o = (HealthCheck) obj;

            if (!equals(m_intervals, _o.getIntervals())) {
                return false;
            }

            if (!equals(m_fails, _o.getFails())) {
                return false;
            }

            if (!equals(m_passes, _o.getPasses())) {
                return false;
            }

            if (!equals(m_timeout, _o.getTimeout())) {
                return false;
            }

            if (!equals(m_uri, _o.getUri())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Integer getFails() {
        return m_fails;
    }

    public Integer getIntervals() {
        return m_intervals;
    }

    public Integer getPasses() {
        return m_passes;
    }

    public Integer getTimeout() {
        return m_timeout;
    }

    public String getUri() {
        return m_uri;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_intervals == null ? 0 : m_intervals.hashCode());
        hash = hash * 31 + (m_fails == null ? 0 : m_fails.hashCode());
        hash = hash * 31 + (m_passes == null ? 0 : m_passes.hashCode());
        hash = hash * 31 + (m_timeout == null ? 0 : m_timeout.hashCode());
        hash = hash * 31 + (m_uri == null ? 0 : m_uri.hashCode());

        return hash;
    }


    public HealthCheck setFails(Integer fails) {
        m_fails = fails;
        return this;
    }

    public HealthCheck setIntervals(Integer intervals) {
        m_intervals = intervals;
        return this;
    }

    public HealthCheck setPasses(Integer passes) {
        m_passes = passes;
        return this;
    }

    public HealthCheck setTimeout(Integer timeout) {
        m_timeout = timeout;
        return this;
    }

    public HealthCheck setUri(String uri) {
        m_uri = uri;
        return this;
    }

}
