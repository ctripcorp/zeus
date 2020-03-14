package com.ctrip.zeus.model.tools;

public class Check {
    private String m_ip;

    private Integer m_port;

    private Integer m_code;

    private String m_message;

    private Long m_time;

    private Long m_groupId;

    private Long m_vsId;

    public Check() {
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
        if (obj instanceof Check) {
            Check _o = (Check) obj;

            if (!equals(m_ip, _o.getIp())) {
                return false;
            }

            if (!equals(m_port, _o.getPort())) {
                return false;
            }

            if (!equals(m_code, _o.getCode())) {
                return false;
            }

            if (!equals(m_message, _o.getMessage())) {
                return false;
            }

            if (!equals(m_time, _o.getTime())) {
                return false;
            }

            if (!equals(m_groupId, _o.getGroupId())) {
                return false;
            }

            if (!equals(m_vsId, _o.getVsId())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Integer getCode() {
        return m_code;
    }

    public Long getGroupId() {
        return m_groupId;
    }

    public String getIp() {
        return m_ip;
    }

    public String getMessage() {
        return m_message;
    }

    public Integer getPort() {
        return m_port;
    }

    public Long getTime() {
        return m_time;
    }

    public Long getVsId() {
        return m_vsId;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_ip == null ? 0 : m_ip.hashCode());
        hash = hash * 31 + (m_port == null ? 0 : m_port.hashCode());
        hash = hash * 31 + (m_code == null ? 0 : m_code.hashCode());
        hash = hash * 31 + (m_message == null ? 0 : m_message.hashCode());
        hash = hash * 31 + (m_time == null ? 0 : m_time.hashCode());
        hash = hash * 31 + (m_groupId == null ? 0 : m_groupId.hashCode());
        hash = hash * 31 + (m_vsId == null ? 0 : m_vsId.hashCode());

        return hash;
    }



    public Check setCode(Integer code) {
        m_code = code;
        return this;
    }

    public Check setGroupId(Long groupId) {
        m_groupId = groupId;
        return this;
    }

    public Check setIp(String ip) {
        m_ip = ip;
        return this;
    }

    public Check setMessage(String message) {
        m_message = message;
        return this;
    }

    public Check setPort(Integer port) {
        m_port = port;
        return this;
    }

    public Check setTime(Long time) {
        m_time = time;
        return this;
    }

    public Check setVsId(Long vsId) {
        m_vsId = vsId;
        return this;
    }

}
