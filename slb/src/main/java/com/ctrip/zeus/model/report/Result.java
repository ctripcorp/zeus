package com.ctrip.zeus.model.report;

public class Result {
    private Long m_groupId;

    public Result() {
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
        if (obj instanceof Result) {
            Result _o = (Result) obj;

            if (!equals(m_groupId, _o.getGroupId())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Long getGroupId() {
        return m_groupId;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_groupId == null ? 0 : m_groupId.hashCode());

        return hash;
    }



    public Result setGroupId(Long groupId) {
        m_groupId = groupId;
        return this;
    }

}
