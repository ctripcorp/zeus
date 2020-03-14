package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class SlbGroupCheckFailureEntity {
    private Long m_slbId;

    private List<Integer> m_failureCounts = new ArrayList<Integer>();

    public SlbGroupCheckFailureEntity() {
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



    public SlbGroupCheckFailureEntity addFailureCount(Integer failureCount) {
        m_failureCounts.add(failureCount);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SlbGroupCheckFailureEntity) {
            SlbGroupCheckFailureEntity _o = (SlbGroupCheckFailureEntity) obj;

            if (!equals(m_slbId, _o.getSlbId())) {
                return false;
            }

            if (!equals(m_failureCounts, _o.getFailureCounts())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<Integer> getFailureCounts() {
        return m_failureCounts;
    }

    public Long getSlbId() {
        return m_slbId;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_slbId == null ? 0 : m_slbId.hashCode());
        hash = hash * 31 + (m_failureCounts == null ? 0 : m_failureCounts.hashCode());

        return hash;
    }



    public SlbGroupCheckFailureEntity setSlbId(Long slbId) {
        m_slbId = slbId;
        return this;
    }

}
