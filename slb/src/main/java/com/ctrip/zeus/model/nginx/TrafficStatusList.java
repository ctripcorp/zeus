package com.ctrip.zeus.model.nginx;

import java.util.ArrayList;
import java.util.List;

public class TrafficStatusList {
    private Integer m_total;

    private List<ReqStatus> m_statuses = new ArrayList<ReqStatus>();

    public TrafficStatusList() {
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



    public TrafficStatusList addReqStatus(ReqStatus reqStatus) {
        m_statuses.add(reqStatus);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TrafficStatusList) {
            TrafficStatusList _o = (TrafficStatusList) obj;

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_statuses, _o.getStatuses())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<ReqStatus> getStatuses() {
        return m_statuses;
    }

    public Integer getTotal() {
        return m_total;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_statuses == null ? 0 : m_statuses.hashCode());

        return hash;
    }



    public TrafficStatusList setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
