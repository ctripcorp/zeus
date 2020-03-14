package com.ctrip.zeus.model.task;

public class TaskResult {
    private Boolean m_success;

    private String m_failCause;

    private String m_status;

    private java.util.Date m_dateTime;

    private OpsTask m_opsTask;

    public TaskResult() {
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
        if (obj instanceof TaskResult) {
            TaskResult _o = (TaskResult) obj;

            if (!equals(m_success, _o.getSuccess())) {
                return false;
            }

            if (!equals(m_failCause, _o.getFailCause())) {
                return false;
            }

            if (!equals(m_status, _o.getStatus())) {
                return false;
            }

            if (!equals(m_dateTime, _o.getDateTime())) {
                return false;
            }

            if (!equals(m_opsTask, _o.getOpsTask())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public java.util.Date getDateTime() {
        return m_dateTime;
    }

    public String getFailCause() {
        return m_failCause;
    }

    public OpsTask getOpsTask() {
        return m_opsTask;
    }

    public String getStatus() {
        return m_status;
    }

    public Boolean getSuccess() {
        return m_success;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_success == null ? 0 : m_success.hashCode());
        hash = hash * 31 + (m_failCause == null ? 0 : m_failCause.hashCode());
        hash = hash * 31 + (m_status == null ? 0 : m_status.hashCode());
        hash = hash * 31 + (m_dateTime == null ? 0 : m_dateTime.hashCode());
        hash = hash * 31 + (m_opsTask == null ? 0 : m_opsTask.hashCode());

        return hash;
    }

    public boolean isSuccess() {
        return m_success != null && m_success.booleanValue();
    }



    public TaskResult setDateTime(java.util.Date dateTime) {
        m_dateTime = dateTime;
        return this;
    }

    public TaskResult setFailCause(String failCause) {
        m_failCause = failCause;
        return this;
    }

    public TaskResult setOpsTask(OpsTask opsTask) {
        m_opsTask = opsTask;
        return this;
    }

    public TaskResult setStatus(String status) {
        m_status = status;
        return this;
    }

    public TaskResult setSuccess(Boolean success) {
        m_success = success;
        return this;
    }

}
