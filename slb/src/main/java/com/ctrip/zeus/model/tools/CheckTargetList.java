package com.ctrip.zeus.model.tools;

import java.util.ArrayList;
import java.util.List;

public class CheckTargetList {
    private Integer m_total;

    private List<CheckTarget> m_targets = new ArrayList<CheckTarget>();

    public CheckTargetList() {
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



    public CheckTargetList addCheckTarget(CheckTarget checkTarget) {
        m_targets.add(checkTarget);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CheckTargetList) {
            CheckTargetList _o = (CheckTargetList) obj;

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_targets, _o.getTargets())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<CheckTarget> getTargets() {
        return m_targets;
    }

    public Integer getTotal() {
        return m_total;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_targets == null ? 0 : m_targets.hashCode());

        return hash;
    }



    public CheckTargetList setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
