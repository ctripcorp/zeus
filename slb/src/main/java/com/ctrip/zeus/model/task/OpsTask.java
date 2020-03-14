package com.ctrip.zeus.model.task;

public class OpsTask {
    private Long m_id;

    private Long m_slbId;

    private Long m_groupId;

    private Long m_drId;

    private Long m_policyId;

    private String m_opsType;

    private String m_ipList;

    private Boolean m_up;

    private String m_status;

    private Long m_targetSlbId;

    private Integer m_version;

    private Boolean m_skipValidate;

    private Long m_slbVirtualServerId;

    private String m_resources;

    private String m_failCause;

    private String m_taskList;

    private java.util.Date m_createTime;

    public OpsTask() {
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
        if (obj instanceof OpsTask) {
            OpsTask _o = (OpsTask) obj;

            if (!equals(m_id, _o.getId())) {
                return false;
            }

            if (!equals(m_slbId, _o.getSlbId())) {
                return false;
            }

            if (!equals(m_groupId, _o.getGroupId())) {
                return false;
            }

            if (!equals(m_drId, _o.getDrId())) {
                return false;
            }

            if (!equals(m_policyId, _o.getPolicyId())) {
                return false;
            }

            if (!equals(m_opsType, _o.getOpsType())) {
                return false;
            }

            if (!equals(m_ipList, _o.getIpList())) {
                return false;
            }

            if (!equals(m_up, _o.getUp())) {
                return false;
            }

            if (!equals(m_status, _o.getStatus())) {
                return false;
            }

            if (!equals(m_targetSlbId, _o.getTargetSlbId())) {
                return false;
            }

            if (!equals(m_version, _o.getVersion())) {
                return false;
            }

            if (!equals(m_skipValidate, _o.getSkipValidate())) {
                return false;
            }

            if (!equals(m_slbVirtualServerId, _o.getSlbVirtualServerId())) {
                return false;
            }

            if (!equals(m_resources, _o.getResources())) {
                return false;
            }

            if (!equals(m_failCause, _o.getFailCause())) {
                return false;
            }

            if (!equals(m_taskList, _o.getTaskList())) {
                return false;
            }

            if (!equals(m_createTime, _o.getCreateTime())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public java.util.Date getCreateTime() {
        return m_createTime;
    }

    public Long getDrId() {
        return m_drId;
    }

    public String getFailCause() {
        return m_failCause;
    }

    public Long getGroupId() {
        return m_groupId;
    }

    public Long getId() {
        return m_id;
    }

    public String getIpList() {
        return m_ipList;
    }

    public String getOpsType() {
        return m_opsType;
    }

    public Long getPolicyId() {
        return m_policyId;
    }

    public String getResources() {
        return m_resources;
    }

    public Boolean getSkipValidate() {
        return m_skipValidate;
    }

    public Long getSlbId() {
        return m_slbId;
    }

    public Long getSlbVirtualServerId() {
        return m_slbVirtualServerId;
    }

    public String getStatus() {
        return m_status;
    }

    public Long getTargetSlbId() {
        return m_targetSlbId;
    }

    public String getTaskList() {
        return m_taskList;
    }

    public Boolean getUp() {
        return m_up;
    }

    public Integer getVersion() {
        return m_version;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());
        hash = hash * 31 + (m_slbId == null ? 0 : m_slbId.hashCode());
        hash = hash * 31 + (m_groupId == null ? 0 : m_groupId.hashCode());
        hash = hash * 31 + (m_drId == null ? 0 : m_drId.hashCode());
        hash = hash * 31 + (m_policyId == null ? 0 : m_policyId.hashCode());
        hash = hash * 31 + (m_opsType == null ? 0 : m_opsType.hashCode());
        hash = hash * 31 + (m_ipList == null ? 0 : m_ipList.hashCode());
        hash = hash * 31 + (m_up == null ? 0 : m_up.hashCode());
        hash = hash * 31 + (m_status == null ? 0 : m_status.hashCode());
        hash = hash * 31 + (m_targetSlbId == null ? 0 : m_targetSlbId.hashCode());
        hash = hash * 31 + (m_version == null ? 0 : m_version.hashCode());
        hash = hash * 31 + (m_skipValidate == null ? 0 : m_skipValidate.hashCode());
        hash = hash * 31 + (m_slbVirtualServerId == null ? 0 : m_slbVirtualServerId.hashCode());
        hash = hash * 31 + (m_resources == null ? 0 : m_resources.hashCode());
        hash = hash * 31 + (m_failCause == null ? 0 : m_failCause.hashCode());
        hash = hash * 31 + (m_taskList == null ? 0 : m_taskList.hashCode());
        hash = hash * 31 + (m_createTime == null ? 0 : m_createTime.hashCode());

        return hash;
    }

    public boolean isSkipValidate() {
        return m_skipValidate != null && m_skipValidate.booleanValue();
    }

    public boolean isUp() {
        return m_up != null && m_up.booleanValue();
    }



    public OpsTask setCreateTime(java.util.Date createTime) {
        m_createTime = createTime;
        return this;
    }

    public OpsTask setDrId(Long drId) {
        m_drId = drId;
        return this;
    }

    public OpsTask setFailCause(String failCause) {
        m_failCause = failCause;
        return this;
    }

    public OpsTask setGroupId(Long groupId) {
        m_groupId = groupId;
        return this;
    }

    public OpsTask setId(Long id) {
        m_id = id;
        return this;
    }

    public OpsTask setIpList(String ipList) {
        m_ipList = ipList;
        return this;
    }

    public OpsTask setOpsType(String opsType) {
        m_opsType = opsType;
        return this;
    }

    public OpsTask setPolicyId(Long policyId) {
        m_policyId = policyId;
        return this;
    }

    public OpsTask setResources(String resources) {
        m_resources = resources;
        return this;
    }

    public OpsTask setSkipValidate(Boolean skipValidate) {
        m_skipValidate = skipValidate;
        return this;
    }

    public OpsTask setSlbId(Long slbId) {
        m_slbId = slbId;
        return this;
    }

    public OpsTask setSlbVirtualServerId(Long slbVirtualServerId) {
        m_slbVirtualServerId = slbVirtualServerId;
        return this;
    }

    public OpsTask setStatus(String status) {
        m_status = status;
        return this;
    }

    public OpsTask setTargetSlbId(Long targetSlbId) {
        m_targetSlbId = targetSlbId;
        return this;
    }

    public OpsTask setTaskList(String taskList) {
        m_taskList = taskList;
        return this;
    }

    public OpsTask setUp(Boolean up) {
        m_up = up;
        return this;
    }

    public OpsTask setVersion(Integer version) {
        m_version = version;
        return this;
    }

}
