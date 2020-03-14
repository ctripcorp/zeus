package com.ctrip.zeus.model.queue;

public class RuleData {
    private Long m_id;

    private String m_ruleType;

    public RuleData() {
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
        if (obj instanceof RuleData) {
            RuleData _o = (RuleData) obj;

            if (!equals(m_id, _o.getId())) {
                return false;
            }

            if (!equals(m_ruleType, _o.getRuleType())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Long getId() {
        return m_id;
    }

    public String getRuleType() {
        return m_ruleType;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());
        hash = hash * 31 + (m_ruleType == null ? 0 : m_ruleType.hashCode());

        return hash;
    }



    public RuleData setId(Long id) {
        m_id = id;
        return this;
    }

    public RuleData setRuleType(String ruleType) {
        m_ruleType = ruleType;
        return this;
    }

}
