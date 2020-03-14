package com.ctrip.zeus.model.restful.response;

public class Model {
    private ErrorMessage m_errorMessage;

    private SuccessMessage m_successMessage;

    public Model() {
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
        if (obj instanceof Model) {
            Model _o = (Model) obj;

            if (!equals(m_errorMessage, _o.getErrorMessage())) {
                return false;
            }

            if (!equals(m_successMessage, _o.getSuccessMessage())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public ErrorMessage getErrorMessage() {
        return m_errorMessage;
    }

    public SuccessMessage getSuccessMessage() {
        return m_successMessage;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_errorMessage == null ? 0 : m_errorMessage.hashCode());
        hash = hash * 31 + (m_successMessage == null ? 0 : m_successMessage.hashCode());

        return hash;
    }



    public Model setErrorMessage(ErrorMessage errorMessage) {
        m_errorMessage = errorMessage;
        return this;
    }

    public Model setSuccessMessage(SuccessMessage successMessage) {
        m_successMessage = successMessage;
        return this;
    }

}
