package com.ctrip.zeus.model.lock;

import java.util.ArrayList;
import java.util.List;

public class LockList {
    private Integer m_total;

    private List<LockStatus> m_locks = new ArrayList<LockStatus>();

    public LockList() {
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



    public LockList addLockStatus(LockStatus lockStatus) {
        m_locks.add(lockStatus);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LockList) {
            LockList _o = (LockList) obj;

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_locks, _o.getLocks())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<LockStatus> getLocks() {
        return m_locks;
    }

    public Integer getTotal() {
        return m_total;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_locks == null ? 0 : m_locks.hashCode());

        return hash;
    }



    public LockList setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
