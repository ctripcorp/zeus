package com.ctrip.zeus.model.model;

public class SlbValidateResponse {
    private Boolean m_succeed;

    private String m_msg;

    private Long m_slbId;

    private String m_ip;

    public SlbValidateResponse() {
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
        if (obj instanceof SlbValidateResponse) {
            SlbValidateResponse _o = (SlbValidateResponse) obj;

            if (!equals(m_succeed, _o.getSucceed())) {
                return false;
            }

            if (!equals(m_msg, _o.getMsg())) {
                return false;
            }

            if (!equals(m_slbId, _o.getSlbId())) {
                return false;
            }

            if (!equals(m_ip, _o.getIp())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getIp() {
        return m_ip;
    }

    public String getMsg() {
        return m_msg;
    }

    public Long getSlbId() {
        return m_slbId;
    }

    public Boolean getSucceed() {
        return m_succeed;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_succeed == null ? 0 : m_succeed.hashCode());
        hash = hash * 31 + (m_msg == null ? 0 : m_msg.hashCode());
        hash = hash * 31 + (m_slbId == null ? 0 : m_slbId.hashCode());
        hash = hash * 31 + (m_ip == null ? 0 : m_ip.hashCode());

        return hash;
    }

    public boolean isSucceed() {
        return m_succeed != null && m_succeed.booleanValue();
    }



    public SlbValidateResponse setIp(String ip) {
        m_ip = ip;
        return this;
    }

    public SlbValidateResponse setMsg(String msg) {
        m_msg = msg;
        return this;
    }

    public SlbValidateResponse setSlbId(Long slbId) {
        m_slbId = slbId;
        return this;
    }

    public SlbValidateResponse setSucceed(Boolean succeed) {
        m_succeed = succeed;
        return this;
    }

}
