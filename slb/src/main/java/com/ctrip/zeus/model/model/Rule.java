package com.ctrip.zeus.model.model;

public class Rule {
    private Long m_id;

    private String m_name;

    private Integer m_type;

    private Integer m_version;

    private String m_data;

    private Integer m_phaseId;

    private String m_phase;

    private String m_ruleType;

    private String m_attributes;

    private String m_targetType;

    private String m_targetId;

    private java.util.Date m_createdTime;

    public Rule() {
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
        if (obj instanceof Rule) {
            Rule _o = (Rule) obj;

            if (!equals(m_id, _o.getId())) {
                return false;
            }

            if (!equals(m_name, _o.getName())) {
                return false;
            }

            if (!equals(m_type, _o.getType())) {
                return false;
            }

            if (!equals(m_version, _o.getVersion())) {
                return false;
            }

            if (!equals(m_data, _o.getData())) {
                return false;
            }

            if (!equals(m_phaseId, _o.getPhaseId())) {
                return false;
            }

            if (!equals(m_phase, _o.getPhase())) {
                return false;
            }

            if (!equals(m_ruleType, _o.getRuleType())) {
                return false;
            }

            if (!equals(m_attributes, _o.getAttributes())) {
                return false;
            }

            if (!equals(m_targetType, _o.getTargetType())) {
                return false;
            }

            if (!equals(m_targetId, _o.getTargetId())) {
                return false;
            }

            if (!equals(m_createdTime, _o.getCreatedTime())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getAttributes() {
        return m_attributes;
    }

    public java.util.Date getCreatedTime() {
        return m_createdTime;
    }

    public String getData() {
        return m_data;
    }

    public Long getId() {
        return m_id;
    }

    public String getName() {
        return m_name;
    }

    public String getPhase() {
        return m_phase;
    }

    public Integer getPhaseId() {
        return m_phaseId;
    }

    public String getRuleType() {
        return m_ruleType;
    }

    public String getTargetId() {
        return m_targetId;
    }

    public String getTargetType() {
        return m_targetType;
    }

    public Integer getType() {
        return m_type;
    }

    public Integer getVersion() {
        return m_version;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());
        hash = hash * 31 + (m_name == null ? 0 : m_name.hashCode());
        hash = hash * 31 + (m_type == null ? 0 : m_type.hashCode());
        hash = hash * 31 + (m_version == null ? 0 : m_version.hashCode());
        hash = hash * 31 + (m_data == null ? 0 : m_data.hashCode());
        hash = hash * 31 + (m_phaseId == null ? 0 : m_phaseId.hashCode());
        hash = hash * 31 + (m_phase == null ? 0 : m_phase.hashCode());
        hash = hash * 31 + (m_ruleType == null ? 0 : m_ruleType.hashCode());
        hash = hash * 31 + (m_attributes == null ? 0 : m_attributes.hashCode());
        hash = hash * 31 + (m_targetType == null ? 0 : m_targetType.hashCode());
        hash = hash * 31 + (m_targetId == null ? 0 : m_targetId.hashCode());
        hash = hash * 31 + (m_createdTime == null ? 0 : m_createdTime.hashCode());

        return hash;
    }



    public Rule setAttributes(String attributes) {
        m_attributes = attributes;
        return this;
    }

    public Rule setCreatedTime(java.util.Date createdTime) {
        m_createdTime = createdTime;
        return this;
    }

    public Rule setData(String data) {
        m_data = data;
        return this;
    }

    public Rule setId(Long id) {
        m_id = id;
        return this;
    }

    public Rule setName(String name) {
        m_name = name;
        return this;
    }

    public Rule setPhase(String phase) {
        m_phase = phase;
        return this;
    }

    public Rule setPhaseId(Integer phaseId) {
        m_phaseId = phaseId;
        return this;
    }

    public Rule setRuleType(String ruleType) {
        m_ruleType = ruleType;
        return this;
    }

    public Rule setTargetId(String targetId) {
        m_targetId = targetId;
        return this;
    }

    public Rule setTargetType(String targetType) {
        m_targetType = targetType;
        return this;
    }

    public Rule setType(Integer type) {
        m_type = type;
        return this;
    }

    public Rule setVersion(Integer version) {
        m_version = version;
        return this;
    }

}
