package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class Condition {
    private String m_type;

    private String m_target;

    private String m_alias;

    private String m_function;

    private String m_value;

    private List<Condition> m_composit = new ArrayList<Condition>();

    public Condition() {
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



    public Condition addCondition(Condition condition) {
        m_composit.add(condition);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Condition) {
            Condition _o = (Condition) obj;

            if (!equals(m_type, _o.getType())) {
                return false;
            }

            if (!equals(m_target, _o.getTarget())) {
                return false;
            }

            if (!equals(m_alias, _o.getAlias())) {
                return false;
            }

            if (!equals(m_function, _o.getFunction())) {
                return false;
            }

            if (!equals(m_value, _o.getValue())) {
                return false;
            }

            if (!equals(m_composit, _o.getComposit())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getAlias() {
        return m_alias;
    }

    public List<Condition> getComposit() {
        return m_composit;
    }

    public String getFunction() {
        return m_function;
    }

    public String getTarget() {
        return m_target;
    }

    public String getType() {
        return m_type;
    }

    public String getValue() {
        return m_value;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_type == null ? 0 : m_type.hashCode());
        hash = hash * 31 + (m_target == null ? 0 : m_target.hashCode());
        hash = hash * 31 + (m_alias == null ? 0 : m_alias.hashCode());
        hash = hash * 31 + (m_function == null ? 0 : m_function.hashCode());
        hash = hash * 31 + (m_value == null ? 0 : m_value.hashCode());
        hash = hash * 31 + (m_composit == null ? 0 : m_composit.hashCode());

        return hash;
    }



    public Condition setAlias(String alias) {
        m_alias = alias;
        return this;
    }

    public Condition setFunction(String function) {
        m_function = function;
        return this;
    }

    public Condition setTarget(String target) {
        m_target = target;
        return this;
    }

    public Condition setType(String type) {
        m_type = type;
        return this;
    }

    public Condition setValue(String value) {
        m_value = value;
        return this;
    }

}
