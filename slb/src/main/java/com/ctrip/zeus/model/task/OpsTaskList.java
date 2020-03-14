package com.ctrip.zeus.model.task;

import java.util.ArrayList;
import java.util.List;

public class OpsTaskList {
    private List<OpsTask> m_opsTasks = new ArrayList<OpsTask>();

    public OpsTaskList() {
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



    public OpsTaskList addOpsTask(OpsTask opsTask) {
        m_opsTasks.add(opsTask);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OpsTaskList) {
            OpsTaskList _o = (OpsTaskList) obj;

            if (!equals(m_opsTasks, _o.getOpsTasks())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<OpsTask> getOpsTasks() {
        return m_opsTasks;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_opsTasks == null ? 0 : m_opsTasks.hashCode());

        return hash;
    }



}
