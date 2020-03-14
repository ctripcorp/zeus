package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class SlbReleaseInfo {
    private Long m_id;

    private String m_name;

    private Long m_version;

    private List<SlbServerReleaseInfo> m_slbServersInfo = new ArrayList<SlbServerReleaseInfo>();

    public SlbReleaseInfo() {
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



    public SlbReleaseInfo addSlbServerReleaseInfo(SlbServerReleaseInfo slbServerReleaseInfo) {
        m_slbServersInfo.add(slbServerReleaseInfo);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SlbReleaseInfo) {
            SlbReleaseInfo _o = (SlbReleaseInfo) obj;

            if (!equals(m_id, _o.getId())) {
                return false;
            }

            if (!equals(m_name, _o.getName())) {
                return false;
            }

            if (!equals(m_version, _o.getVersion())) {
                return false;
            }

            if (!equals(m_slbServersInfo, _o.getSlbServersInfo())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Long getId() {
        return m_id;
    }

    public String getName() {
        return m_name;
    }

    public List<SlbServerReleaseInfo> getSlbServersInfo() {
        return m_slbServersInfo;
    }

    public Long getVersion() {
        return m_version;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());
        hash = hash * 31 + (m_name == null ? 0 : m_name.hashCode());
        hash = hash * 31 + (m_version == null ? 0 : m_version.hashCode());
        hash = hash * 31 + (m_slbServersInfo == null ? 0 : m_slbServersInfo.hashCode());

        return hash;
    }


    public SlbReleaseInfo setId(Long id) {
        m_id = id;
        return this;
    }

    public SlbReleaseInfo setName(String name) {
        m_name = name;
        return this;
    }

    public SlbReleaseInfo setVersion(Long version) {
        m_version = version;
        return this;
    }

}
