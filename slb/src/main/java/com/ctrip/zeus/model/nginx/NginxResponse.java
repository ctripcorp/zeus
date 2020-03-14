package com.ctrip.zeus.model.nginx;

public class NginxResponse {
    private Boolean m_succeed;

    private String m_serverIp;

    private String m_errMsg;

    private String m_outMsg;

    public NginxResponse() {
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
        if (obj instanceof NginxResponse) {
            NginxResponse _o = (NginxResponse) obj;

            if (!equals(m_succeed, _o.getSucceed())) {
                return false;
            }

            if (!equals(m_serverIp, _o.getServerIp())) {
                return false;
            }

            if (!equals(m_errMsg, _o.getErrMsg())) {
                return false;
            }

            if (!equals(m_outMsg, _o.getOutMsg())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getErrMsg() {
        return m_errMsg;
    }

    public String getOutMsg() {
        return m_outMsg;
    }

    public String getServerIp() {
        return m_serverIp;
    }

    public Boolean getSucceed() {
        return m_succeed;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_succeed == null ? 0 : m_succeed.hashCode());
        hash = hash * 31 + (m_serverIp == null ? 0 : m_serverIp.hashCode());
        hash = hash * 31 + (m_errMsg == null ? 0 : m_errMsg.hashCode());
        hash = hash * 31 + (m_outMsg == null ? 0 : m_outMsg.hashCode());

        return hash;
    }

    public boolean isSucceed() {
        return m_succeed != null && m_succeed.booleanValue();
    }



    public NginxResponse setErrMsg(String errMsg) {
        m_errMsg = errMsg;
        return this;
    }

    public NginxResponse setOutMsg(String outMsg) {
        m_outMsg = outMsg;
        return this;
    }

    public NginxResponse setServerIp(String serverIp) {
        m_serverIp = serverIp;
        return this;
    }

    public NginxResponse setSucceed(Boolean succeed) {
        m_succeed = succeed;
        return this;
    }

}
