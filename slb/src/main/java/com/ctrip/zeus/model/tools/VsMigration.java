package com.ctrip.zeus.model.tools;

public class VsMigration {
    private Long m_id;

    private Boolean m_status;

    private String m_name;

    private String m_content;

    public VsMigration() {
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
        if (obj instanceof VsMigration) {
            VsMigration _o = (VsMigration) obj;

            if (!equals(m_id, _o.getId())) {
                return false;
            }

            if (!equals(m_status, _o.getStatus())) {
                return false;
            }

            if (!equals(m_name, _o.getName())) {
                return false;
            }

            if (!equals(m_content, _o.getContent())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getContent() {
        return m_content;
    }

    public Long getId() {
        return m_id;
    }

    public String getName() {
        return m_name;
    }

    public Boolean getStatus() {
        return m_status;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());
        hash = hash * 31 + (m_status == null ? 0 : m_status.hashCode());
        hash = hash * 31 + (m_name == null ? 0 : m_name.hashCode());
        hash = hash * 31 + (m_content == null ? 0 : m_content.hashCode());

        return hash;
    }

    public boolean isStatus() {
        return m_status != null && m_status.booleanValue();
    }



    public VsMigration setContent(String content) {
        m_content = content;
        return this;
    }

    public VsMigration setId(Long id) {
        m_id = id;
        return this;
    }

    public VsMigration setName(String name) {
        m_name = name;
        return this;
    }

    public VsMigration setStatus(Boolean status) {
        m_status = status;
        return this;
    }

}
