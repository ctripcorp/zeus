package com.ctrip.zeus.model.status;

import java.util.ArrayList;
import java.util.List;

public class UpdateStatusItem {
    private Long m_slbId;

    private Long m_vsId;

    private Long m_groupId;

    private List<String> m_ipses = new ArrayList<String>();

    private Integer m_offset;

    private Boolean m_up;

    public UpdateStatusItem() {
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



    public UpdateStatusItem addIps(String ips) {
        m_ipses.add(ips);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UpdateStatusItem) {
            UpdateStatusItem _o = (UpdateStatusItem) obj;

            if (!equals(m_slbId, _o.getSlbId())) {
                return false;
            }

            if (!equals(m_vsId, _o.getVsId())) {
                return false;
            }

            if (!equals(m_groupId, _o.getGroupId())) {
                return false;
            }

            if (!equals(m_ipses, _o.getIpses())) {
                return false;
            }

            if (!equals(m_offset, _o.getOffset())) {
                return false;
            }

            if (!equals(m_up, _o.getUp())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Long getGroupId() {
        return m_groupId;
    }

    public List<String> getIpses() {
        return m_ipses;
    }

    public Integer getOffset() {
        return m_offset;
    }

    public Long getSlbId() {
        return m_slbId;
    }

    public Boolean getUp() {
        return m_up;
    }

    public Long getVsId() {
        return m_vsId;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_slbId == null ? 0 : m_slbId.hashCode());
        hash = hash * 31 + (m_vsId == null ? 0 : m_vsId.hashCode());
        hash = hash * 31 + (m_groupId == null ? 0 : m_groupId.hashCode());
        hash = hash * 31 + (m_ipses == null ? 0 : m_ipses.hashCode());
        hash = hash * 31 + (m_offset == null ? 0 : m_offset.hashCode());
        hash = hash * 31 + (m_up == null ? 0 : m_up.hashCode());

        return hash;
    }

    public boolean isUp() {
        return m_up != null && m_up.booleanValue();
    }



    public UpdateStatusItem setGroupId(Long groupId) {
        m_groupId = groupId;
        return this;
    }

    public UpdateStatusItem setOffset(Integer offset) {
        m_offset = offset;
        return this;
    }

    public UpdateStatusItem setSlbId(Long slbId) {
        m_slbId = slbId;
        return this;
    }

    public UpdateStatusItem setUp(Boolean up) {
        m_up = up;
        return this;
    }

    public UpdateStatusItem setVsId(Long vsId) {
        m_vsId = vsId;
        return this;
    }

}
