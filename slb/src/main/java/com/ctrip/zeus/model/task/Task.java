package com.ctrip.zeus.model.task;

public class Task {
    private OpsTask m_opsTask;

    private OpsTaskList m_opsTaskList;

    private TaskResult m_taskResult;

    private TaskResultList m_taskResultList;

    public Task() {
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
        if (obj instanceof Task) {
            Task _o = (Task) obj;

            if (!equals(m_opsTask, _o.getOpsTask())) {
                return false;
            }

            if (!equals(m_opsTaskList, _o.getOpsTaskList())) {
                return false;
            }

            if (!equals(m_taskResult, _o.getTaskResult())) {
                return false;
            }

            if (!equals(m_taskResultList, _o.getTaskResultList())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public OpsTask getOpsTask() {
        return m_opsTask;
    }

    public OpsTaskList getOpsTaskList() {
        return m_opsTaskList;
    }

    public TaskResult getTaskResult() {
        return m_taskResult;
    }

    public TaskResultList getTaskResultList() {
        return m_taskResultList;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_opsTask == null ? 0 : m_opsTask.hashCode());
        hash = hash * 31 + (m_opsTaskList == null ? 0 : m_opsTaskList.hashCode());
        hash = hash * 31 + (m_taskResult == null ? 0 : m_taskResult.hashCode());
        hash = hash * 31 + (m_taskResultList == null ? 0 : m_taskResultList.hashCode());

        return hash;
    }



    public Task setOpsTask(OpsTask opsTask) {
        m_opsTask = opsTask;
        return this;
    }

    public Task setOpsTaskList(OpsTaskList opsTaskList) {
        m_opsTaskList = opsTaskList;
        return this;
    }

    public Task setTaskResult(TaskResult taskResult) {
        m_taskResult = taskResult;
        return this;
    }

    public Task setTaskResultList(TaskResultList taskResultList) {
        m_taskResultList = taskResultList;
        return this;
    }

}
