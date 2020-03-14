package com.ctrip.zeus.model.page;

public class DefaultFile {
    private String m_name;

    private Long m_version;

    private String m_file;

    public DefaultFile() {
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
        if (obj instanceof DefaultFile) {
            DefaultFile _o = (DefaultFile) obj;

            if (!equals(m_name, _o.getName())) {
                return false;
            }

            if (!equals(m_version, _o.getVersion())) {
                return false;
            }

            if (!equals(m_file, _o.getFile())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getFile() {
        return m_file;
    }

    public String getName() {
        return m_name;
    }

    public Long getVersion() {
        return m_version;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_name == null ? 0 : m_name.hashCode());
        hash = hash * 31 + (m_version == null ? 0 : m_version.hashCode());
        hash = hash * 31 + (m_file == null ? 0 : m_file.hashCode());

        return hash;
    }



    public DefaultFile setFile(String file) {
        m_file = file;
        return this;
    }

    public DefaultFile setName(String name) {
        m_name = name;
        return this;
    }

    public DefaultFile setVersion(Long version) {
        m_version = version;
        return this;
    }

}
