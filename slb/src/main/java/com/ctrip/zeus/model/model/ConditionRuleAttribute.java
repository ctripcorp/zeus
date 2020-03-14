package com.ctrip.zeus.model.model;

public class ConditionRuleAttribute {
    private Condition m_condition;

    private ConditionAction m_conditionAction;

    public ConditionRuleAttribute() {
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
        if (obj instanceof ConditionRuleAttribute) {
            ConditionRuleAttribute _o = (ConditionRuleAttribute) obj;

            if (!equals(m_condition, _o.getCondition())) {
                return false;
            }

            if (!equals(m_conditionAction, _o.getConditionAction())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Condition getCondition() {
        return m_condition;
    }

    public ConditionAction getConditionAction() {
        return m_conditionAction;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_condition == null ? 0 : m_condition.hashCode());
        hash = hash * 31 + (m_conditionAction == null ? 0 : m_conditionAction.hashCode());

        return hash;
    }



    public ConditionRuleAttribute setCondition(Condition condition) {
        m_condition = condition;
        return this;
    }

    public ConditionRuleAttribute setConditionAction(ConditionAction conditionAction) {
        m_conditionAction = conditionAction;
        return this;
    }

}
