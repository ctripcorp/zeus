package com.ctrip.zeus.model.restful.response;

public class ErrorMessage {
    private String m_code;

    private String m_message;

    private String m_stackTrace;

    public ErrorMessage() {
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
        if (obj instanceof ErrorMessage) {
            ErrorMessage _o = (ErrorMessage) obj;

            if (!equals(m_code, _o.getCode())) {
                return false;
            }

            if (!equals(m_message, _o.getMessage())) {
                return false;
            }

            if (!equals(m_stackTrace, _o.getStackTrace())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getCode() {
        return m_code;
    }

    public String getMessage() {
        return m_message;
    }

    public String getStackTrace() {
        return m_stackTrace;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_code == null ? 0 : m_code.hashCode());
        hash = hash * 31 + (m_message == null ? 0 : m_message.hashCode());
        hash = hash * 31 + (m_stackTrace == null ? 0 : m_stackTrace.hashCode());

        return hash;
    }



    public ErrorMessage setCode(String code) {
        m_code = code;
        return this;
    }

    public ErrorMessage setMessage(String message) {
        m_message = message;
        return this;
    }

    public ErrorMessage setStackTrace(String stackTrace) {
        m_stackTrace = stackTrace;
        return this;
    }

}
