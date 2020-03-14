package com.ctrip.zeus.model.task;

import java.util.ArrayList;
import java.util.List;

public class TaskResultList {
    private Integer m_total;

    private List<TaskResult> m_taskResults = new ArrayList<TaskResult>();

    public TaskResultList() {
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



    public TaskResultList addTaskResult(TaskResult taskResult) {
        m_taskResults.add(taskResult);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TaskResultList) {
            TaskResultList _o = (TaskResultList) obj;

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_taskResults, _o.getTaskResults())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<TaskResult> getTaskResults() {
        return m_taskResults;
    }

    public Integer getTotal() {
        return m_total;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_taskResults == null ? 0 : m_taskResults.hashCode());

        return hash;
    }



    public TaskResultList setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
