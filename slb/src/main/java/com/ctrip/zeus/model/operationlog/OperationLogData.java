package com.ctrip.zeus.model.operationlog;

public class OperationLogData {
    private String m_type;

    private String m_targetId;

    private String m_operation;

    private String m_userName;

    private String m_data;

    private String m_clientIp;

    private Boolean m_success;

    private String m_errMsg;

    private java.util.Date m_dateTime;

    public OperationLogData() {
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
        if (obj instanceof OperationLogData) {
            OperationLogData _o = (OperationLogData) obj;

            if (!equals(m_type, _o.getType())) {
                return false;
            }

            if (!equals(m_targetId, _o.getTargetId())) {
                return false;
            }

            if (!equals(m_operation, _o.getOperation())) {
                return false;
            }

            if (!equals(m_userName, _o.getUserName())) {
                return false;
            }

            if (!equals(m_data, _o.getData())) {
                return false;
            }

            if (!equals(m_clientIp, _o.getClientIp())) {
                return false;
            }

            if (!equals(m_success, _o.getSuccess())) {
                return false;
            }

            if (!equals(m_errMsg, _o.getErrMsg())) {
                return false;
            }

            if (!equals(m_dateTime, _o.getDateTime())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getClientIp() {
        return m_clientIp;
    }

    public String getData() {
        return m_data;
    }

    public java.util.Date getDateTime() {
        return m_dateTime;
    }

    public String getErrMsg() {
        return m_errMsg;
    }

    public String getOperation() {
        return m_operation;
    }

    public Boolean getSuccess() {
        return m_success;
    }

    public String getTargetId() {
        return m_targetId;
    }

    public String getType() {
        return m_type;
    }

    public String getUserName() {
        return m_userName;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_type == null ? 0 : m_type.hashCode());
        hash = hash * 31 + (m_targetId == null ? 0 : m_targetId.hashCode());
        hash = hash * 31 + (m_operation == null ? 0 : m_operation.hashCode());
        hash = hash * 31 + (m_userName == null ? 0 : m_userName.hashCode());
        hash = hash * 31 + (m_data == null ? 0 : m_data.hashCode());
        hash = hash * 31 + (m_clientIp == null ? 0 : m_clientIp.hashCode());
        hash = hash * 31 + (m_success == null ? 0 : m_success.hashCode());
        hash = hash * 31 + (m_errMsg == null ? 0 : m_errMsg.hashCode());
        hash = hash * 31 + (m_dateTime == null ? 0 : m_dateTime.hashCode());

        return hash;
    }

    public boolean isSuccess() {
        return m_success != null && m_success.booleanValue();
    }



    public OperationLogData setClientIp(String clientIp) {
        m_clientIp = clientIp;
        return this;
    }

    public OperationLogData setData(String data) {
        m_data = data;
        return this;
    }

    public OperationLogData setDateTime(java.util.Date dateTime) {
        m_dateTime = dateTime;
        return this;
    }

    public OperationLogData setErrMsg(String errMsg) {
        m_errMsg = errMsg;
        return this;
    }

    public OperationLogData setOperation(String operation) {
        m_operation = operation;
        return this;
    }

    public OperationLogData setSuccess(Boolean success) {
        m_success = success;
        return this;
    }

    public OperationLogData setTargetId(String targetId) {
        m_targetId = targetId;
        return this;
    }

    public OperationLogData setType(String type) {
        m_type = type;
        return this;
    }

    public OperationLogData setUserName(String userName) {
        m_userName = userName;
        return this;
    }

}
