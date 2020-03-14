package com.ctrip.zeus.model.restful.response;

public class SuccessMessage {
    private String m_message;

    public SuccessMessage() {
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
        if (obj instanceof SuccessMessage) {
            SuccessMessage _o = (SuccessMessage) obj;

            if (!equals(m_message, _o.getMessage())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getMessage() {
        return m_message;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_message == null ? 0 : m_message.hashCode());

        return hash;
    }



    public SuccessMessage setMessage(String message) {
        m_message = message;
        return this;
    }

}
