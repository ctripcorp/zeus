package com.ctrip.zeus.model.operationlog;

public class Operationlog {
    private OperationLogDataList m_operationLogDataList;

    public Operationlog() {
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
        if (obj instanceof Operationlog) {
            Operationlog _o = (Operationlog) obj;

            if (!equals(m_operationLogDataList, _o.getOperationLogDataList())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public OperationLogDataList getOperationLogDataList() {
        return m_operationLogDataList;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_operationLogDataList == null ? 0 : m_operationLogDataList.hashCode());

        return hash;
    }



    public Operationlog setOperationLogDataList(OperationLogDataList operationLogDataList) {
        m_operationLogDataList = operationLogDataList;
        return this;
    }

}
