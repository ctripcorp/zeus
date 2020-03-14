package com.ctrip.zeus.model.tools;

public class CheckResponse {
    private Integer m_code;

    private Long m_time;

    private String m_status;

    private String m_group;

    private String m_env;

    private String m_hostIp;

    public CheckResponse() {
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
        if (obj instanceof CheckResponse) {
            CheckResponse _o = (CheckResponse) obj;

            if (!equals(m_code, _o.getCode())) {
                return false;
            }

            if (!equals(m_time, _o.getTime())) {
                return false;
            }

            if (!equals(m_status, _o.getStatus())) {
                return false;
            }

            if (!equals(m_group, _o.getGroup())) {
                return false;
            }

            if (!equals(m_env, _o.getEnv())) {
                return false;
            }

            if (!equals(m_hostIp, _o.getHostIp())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Integer getCode() {
        return m_code;
    }

    public String getEnv() {
        return m_env;
    }

    public String getGroup() {
        return m_group;
    }

    public String getHostIp() {
        return m_hostIp;
    }

    public String getStatus() {
        return m_status;
    }

    public Long getTime() {
        return m_time;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_code == null ? 0 : m_code.hashCode());
        hash = hash * 31 + (m_time == null ? 0 : m_time.hashCode());
        hash = hash * 31 + (m_status == null ? 0 : m_status.hashCode());
        hash = hash * 31 + (m_group == null ? 0 : m_group.hashCode());
        hash = hash * 31 + (m_env == null ? 0 : m_env.hashCode());
        hash = hash * 31 + (m_hostIp == null ? 0 : m_hostIp.hashCode());

        return hash;
    }



    public CheckResponse setCode(Integer code) {
        m_code = code;
        return this;
    }

    public CheckResponse setEnv(String env) {
        m_env = env;
        return this;
    }

    public CheckResponse setGroup(String group) {
        m_group = group;
        return this;
    }

    public CheckResponse setHostIp(String hostIp) {
        m_hostIp = hostIp;
        return this;
    }

    public CheckResponse setStatus(String status) {
        m_status = status;
        return this;
    }

    public CheckResponse setTime(Long time) {
        m_time = time;
        return this;
    }

}
