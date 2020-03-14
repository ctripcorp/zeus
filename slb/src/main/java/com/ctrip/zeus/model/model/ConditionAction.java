package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class ConditionAction {
    private String m_type;

    private Integer m_code;

    private String m_message;

    private String m_target;

    private List<ConditionHeader> m_headers = new ArrayList<ConditionHeader>();

    public ConditionAction() {
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



    public ConditionAction addConditionHeader(ConditionHeader conditionHeader) {
        m_headers.add(conditionHeader);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConditionAction) {
            ConditionAction _o = (ConditionAction) obj;

            if (!equals(m_type, _o.getType())) {
                return false;
            }

            if (!equals(m_code, _o.getCode())) {
                return false;
            }

            if (!equals(m_message, _o.getMessage())) {
                return false;
            }

            if (!equals(m_target, _o.getTarget())) {
                return false;
            }

            if (!equals(m_headers, _o.getHeaders())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Integer getCode() {
        return m_code;
    }

    public List<ConditionHeader> getHeaders() {
        return m_headers;
    }

    public String getMessage() {
        return m_message;
    }

    public String getTarget() {
        return m_target;
    }

    public String getType() {
        return m_type;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_type == null ? 0 : m_type.hashCode());
        hash = hash * 31 + (m_code == null ? 0 : m_code.hashCode());
        hash = hash * 31 + (m_message == null ? 0 : m_message.hashCode());
        hash = hash * 31 + (m_target == null ? 0 : m_target.hashCode());
        hash = hash * 31 + (m_headers == null ? 0 : m_headers.hashCode());

        return hash;
    }



    public ConditionAction setCode(Integer code) {
        m_code = code;
        return this;
    }

    public ConditionAction setMessage(String message) {
        m_message = message;
        return this;
    }

    public ConditionAction setTarget(String target) {
        m_target = target;
        return this;
    }

    public ConditionAction setType(String type) {
        m_type = type;
        return this;
    }

}
