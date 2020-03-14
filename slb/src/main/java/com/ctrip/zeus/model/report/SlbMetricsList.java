package com.ctrip.zeus.model.report;

import java.util.ArrayList;
import java.util.List;

public class SlbMetricsList {
    private Integer m_total;

    private List<SlbMetrics> m_slbMetricses = new ArrayList<SlbMetrics>();

    public SlbMetricsList() {
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



    public SlbMetricsList addSlbMetrics(SlbMetrics slbMetrics) {
        m_slbMetricses.add(slbMetrics);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SlbMetricsList) {
            SlbMetricsList _o = (SlbMetricsList) obj;

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_slbMetricses, _o.getSlbMetricses())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<SlbMetrics> getSlbMetricses() {
        return m_slbMetricses;
    }

    public Integer getTotal() {
        return m_total;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_slbMetricses == null ? 0 : m_slbMetricses.hashCode());

        return hash;
    }



    public SlbMetricsList setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
