package com.ctrip.zeus.model.report;

import java.util.ArrayList;
import java.util.List;

public class SlbMetrics {
    private Integer m_total;

    private String m_name;

    private List<Point> m_points = new ArrayList<Point>();

    public SlbMetrics() {
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



    public SlbMetrics addPoint(Point point) {
        m_points.add(point);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SlbMetrics) {
            SlbMetrics _o = (SlbMetrics) obj;

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_name, _o.getName())) {
                return false;
            }

            if (!equals(m_points, _o.getPoints())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getName() {
        return m_name;
    }

    public List<Point> getPoints() {
        return m_points;
    }

    public Integer getTotal() {
        return m_total;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_name == null ? 0 : m_name.hashCode());
        hash = hash * 31 + (m_points == null ? 0 : m_points.hashCode());

        return hash;
    }



    public SlbMetrics setName(String name) {
        m_name = name;
        return this;
    }

    public SlbMetrics setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
