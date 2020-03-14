package com.ctrip.zeus.model.commit;

import java.util.ArrayList;
import java.util.List;

public class Commit {
    private Long m_id;

    private Long m_version;

    private Long m_slbId;

    private List<Long> m_vsIds = new ArrayList<Long>();

    private List<Long> m_groupIds = new ArrayList<Long>();

    private List<Long> m_taskIds = new ArrayList<Long>();

    private List<Long> m_cleanvsIds = new ArrayList<Long>();

    private String m_type;

    private java.util.Date m_dataChangeLastTime;

    public Commit() {
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



    public Commit addCleanvsId(Long cleanvsId) {
        m_cleanvsIds.add(cleanvsId);
        return this;
    }

    public Commit addGroupId(Long groupId) {
        m_groupIds.add(groupId);
        return this;
    }

    public Commit addTaskId(Long taskId) {
        m_taskIds.add(taskId);
        return this;
    }

    public Commit addVsId(Long vsId) {
        m_vsIds.add(vsId);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Commit) {
            Commit _o = (Commit) obj;

            if (!equals(m_id, _o.getId())) {
                return false;
            }

            if (!equals(m_version, _o.getVersion())) {
                return false;
            }

            if (!equals(m_slbId, _o.getSlbId())) {
                return false;
            }

            if (!equals(m_vsIds, _o.getVsIds())) {
                return false;
            }

            if (!equals(m_groupIds, _o.getGroupIds())) {
                return false;
            }

            if (!equals(m_taskIds, _o.getTaskIds())) {
                return false;
            }

            if (!equals(m_cleanvsIds, _o.getCleanvsIds())) {
                return false;
            }

            if (!equals(m_type, _o.getType())) {
                return false;
            }

            if (!equals(m_dataChangeLastTime, _o.getDataChangeLastTime())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<Long> getCleanvsIds() {
        return m_cleanvsIds;
    }

    public java.util.Date getDataChangeLastTime() {
        return m_dataChangeLastTime;
    }

    public List<Long> getGroupIds() {
        return m_groupIds;
    }

    public Long getId() {
        return m_id;
    }

    public Long getSlbId() {
        return m_slbId;
    }

    public List<Long> getTaskIds() {
        return m_taskIds;
    }

    public String getType() {
        return m_type;
    }

    public Long getVersion() {
        return m_version;
    }

    public List<Long> getVsIds() {
        return m_vsIds;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());
        hash = hash * 31 + (m_version == null ? 0 : m_version.hashCode());
        hash = hash * 31 + (m_slbId == null ? 0 : m_slbId.hashCode());
        hash = hash * 31 + (m_vsIds == null ? 0 : m_vsIds.hashCode());
        hash = hash * 31 + (m_groupIds == null ? 0 : m_groupIds.hashCode());
        hash = hash * 31 + (m_taskIds == null ? 0 : m_taskIds.hashCode());
        hash = hash * 31 + (m_cleanvsIds == null ? 0 : m_cleanvsIds.hashCode());
        hash = hash * 31 + (m_type == null ? 0 : m_type.hashCode());
        hash = hash * 31 + (m_dataChangeLastTime == null ? 0 : m_dataChangeLastTime.hashCode());

        return hash;
    }



    public Commit setDataChangeLastTime(java.util.Date dataChangeLastTime) {
        m_dataChangeLastTime = dataChangeLastTime;
        return this;
    }

    public Commit setId(Long id) {
        m_id = id;
        return this;
    }

    public Commit setSlbId(Long slbId) {
        m_slbId = slbId;
        return this;
    }

    public Commit setType(String type) {
        m_type = type;
        return this;
    }

    public Commit setVersion(Long version) {
        m_version = version;
        return this;
    }

}
