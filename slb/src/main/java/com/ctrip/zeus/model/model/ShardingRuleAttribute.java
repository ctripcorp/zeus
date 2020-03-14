package com.ctrip.zeus.model.model;

public class ShardingRuleAttribute {
    private Condition m_condition;

    private Double m_percent;

    private Boolean m_enable;

    public ShardingRuleAttribute() {
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
        if (obj instanceof ShardingRuleAttribute) {
            ShardingRuleAttribute _o = (ShardingRuleAttribute) obj;

            if (!equals(m_condition, _o.getCondition())) {
                return false;
            }

            if (!equals(m_percent, _o.getPercent())) {
                return false;
            }
            if (!equals(m_enable, _o.getEnable())) {
                return false;
            }
            return true;
        }

        return false;
    }

    public Condition getCondition() {
        return m_condition;
    }


    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_condition == null ? 0 : m_condition.hashCode());
        hash = hash * 31 + (m_percent == null ? 0 : m_percent.hashCode());
        hash = hash * 31 + (m_enable == null ? 0 : m_enable.hashCode());

        return hash;
    }


    public ShardingRuleAttribute setCondition(Condition condition) {
        m_condition = condition;
        return this;
    }

    public Double getPercent() {
        return m_percent;
    }

    public ShardingRuleAttribute setPercent(Double m_percent) {
        this.m_percent = m_percent;
        return this;
    }

    public Boolean getEnable() {
        return m_enable;
    }

    public ShardingRuleAttribute setEnable(Boolean enable) {
        this.m_enable = enable;
        return this;
    }
}
