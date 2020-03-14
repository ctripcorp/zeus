package com.ctrip.zeus.model.lock;

public class Model {
    private LockStatus m_lockStatus;

    private LockList m_lockList;

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

            if (!equals(m_lockStatus, _o.getLockStatus())) {
                return false;
            }

            if (!equals(m_lockList, _o.getLockList())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public LockList getLockList() {
        return m_lockList;
    }

    public LockStatus getLockStatus() {
        return m_lockStatus;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_lockStatus == null ? 0 : m_lockStatus.hashCode());
        hash = hash * 31 + (m_lockList == null ? 0 : m_lockList.hashCode());

        return hash;
    }



    public Model setLockList(LockList lockList) {
        m_lockList = lockList;
        return this;
    }

    public Model setLockStatus(LockStatus lockStatus) {
        m_lockStatus = lockStatus;
        return this;
    }

}
