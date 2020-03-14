package com.ctrip.zeus.model.model;

public class TrafficControl {
    private Integer m_weight;

    private Group m_group;

    public TrafficControl() {
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
        if (obj instanceof TrafficControl) {
            TrafficControl _o = (TrafficControl) obj;

            if (!equals(m_weight, _o.getWeight())) {
                return false;
            }

            if (!equals(m_group, _o.getGroup())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Group getGroup() {
        return m_group;
    }

    public Integer getWeight() {
        return m_weight;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_weight == null ? 0 : m_weight.hashCode());
        hash = hash * 31 + (m_group == null ? 0 : m_group.hashCode());

        return hash;
    }



    public TrafficControl setGroup(Group group) {
        m_group = group;
        return this;
    }

    public TrafficControl setWeight(Integer weight) {
        m_weight = weight;
        return this;
    }

}
