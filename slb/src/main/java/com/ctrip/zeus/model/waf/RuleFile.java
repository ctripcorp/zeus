package com.ctrip.zeus.model.waf;

public class RuleFile {
    private String m_name;

    private Integer m_version;

    private String m_content;

    public RuleFile() {
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
        if (obj instanceof RuleFile) {
            RuleFile _o = (RuleFile) obj;

            if (!equals(m_name, _o.getName())) {
                return false;
            }

            if (!equals(m_version, _o.getVersion())) {
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

    public String getName() {
        return m_name;
    }

    public Integer getVersion() {
        return m_version;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_name == null ? 0 : m_name.hashCode());
        hash = hash * 31 + (m_version == null ? 0 : m_version.hashCode());
        hash = hash * 31 + (m_content == null ? 0 : m_content.hashCode());

        return hash;
    }



    public RuleFile setContent(String content) {
        m_content = content;
        return this;
    }

    public RuleFile setName(String name) {
        m_name = name;
        return this;
    }

    public RuleFile setVersion(Integer version) {
        m_version = version;
        return this;
    }

}
