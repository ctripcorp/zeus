package com.ctrip.zeus.model.waf;

public class WafResponse {
    private Integer m_status;

    private String m_response;

    private String m_errorMsg;

    public WafResponse() {
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
        if (obj instanceof WafResponse) {
            WafResponse _o = (WafResponse) obj;

            if (!equals(m_status, _o.getStatus())) {
                return false;
            }

            if (!equals(m_response, _o.getResponse())) {
                return false;
            }

            if (!equals(m_errorMsg, _o.getErrorMsg())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getErrorMsg() {
        return m_errorMsg;
    }

    public String getResponse() {
        return m_response;
    }

    public Integer getStatus() {
        return m_status;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_status == null ? 0 : m_status.hashCode());
        hash = hash * 31 + (m_response == null ? 0 : m_response.hashCode());
        hash = hash * 31 + (m_errorMsg == null ? 0 : m_errorMsg.hashCode());

        return hash;
    }



    public WafResponse setErrorMsg(String errorMsg) {
        m_errorMsg = errorMsg;
        return this;
    }

    public WafResponse setResponse(String response) {
        m_response = response;
        return this;
    }

    public WafResponse setStatus(Integer status) {
        m_status = status;
        return this;
    }

}
