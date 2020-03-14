package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class Rules {
    private String m_targetId;

    private String m_targetType;

    private List<Rule> m_rules = new ArrayList<Rule>();

    public Rules() {
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



    public Rules addRule(Rule rule) {
        m_rules.add(rule);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Rules) {
            Rules _o = (Rules) obj;

            if (!equals(m_targetId, _o.getTargetId())) {
                return false;
            }

            if (!equals(m_targetType, _o.getTargetType())) {
                return false;
            }

            if (!equals(m_rules, _o.getRules())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<Rule> getRules() {
        return m_rules;
    }

    public String getTargetId() {
        return m_targetId;
    }

    public String getTargetType() {
        return m_targetType;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_targetId == null ? 0 : m_targetId.hashCode());
        hash = hash * 31 + (m_targetType == null ? 0 : m_targetType.hashCode());
        hash = hash * 31 + (m_rules == null ? 0 : m_rules.hashCode());

        return hash;
    }



    public Rules setTargetId(String targetId) {
        m_targetId = targetId;
        return this;
    }

    public Rules setTargetType(String targetType) {
        m_targetType = targetType;
        return this;
    }

}
