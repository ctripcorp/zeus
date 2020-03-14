package com.ctrip.zeus.model.operationlog;

import java.util.ArrayList;
import java.util.List;

public class OperationLogDataList {
    private Integer m_total;

    private List<OperationLogData> m_operationLogDatas = new ArrayList<OperationLogData>();

    public OperationLogDataList() {
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



    public OperationLogDataList addOperationLogData(OperationLogData operationLogData) {
        m_operationLogDatas.add(operationLogData);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OperationLogDataList) {
            OperationLogDataList _o = (OperationLogDataList) obj;

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_operationLogDatas, _o.getOperationLogDatas())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<OperationLogData> getOperationLogDatas() {
        return m_operationLogDatas;
    }

    public Integer getTotal() {
        return m_total;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_operationLogDatas == null ? 0 : m_operationLogDatas.hashCode());

        return hash;
    }



    public OperationLogDataList setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
