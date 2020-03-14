package com.ctrip.zeus.model.queue;

public class Message {
    private String m_performer;

    private String m_type;

    private String m_status;

    private Long m_targetId;

    private String m_targetData;

    private java.util.Date m_createTime;

    public Message() {
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
        if (obj instanceof Message) {
            Message _o = (Message) obj;

            if (!equals(m_performer, _o.getPerformer())) {
                return false;
            }

            if (!equals(m_type, _o.getType())) {
                return false;
            }

            if (!equals(m_status, _o.getStatus())) {
                return false;
            }

            if (!equals(m_targetId, _o.getTargetId())) {
                return false;
            }

            if (!equals(m_targetData, _o.getTargetData())) {
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

    public String getPerformer() {
        return m_performer;
    }

    public String getStatus() {
        return m_status;
    }

    public String getTargetData() {
        return m_targetData;
    }

    public Long getTargetId() {
        return m_targetId;
    }

    public String getType() {
        return m_type;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_performer == null ? 0 : m_performer.hashCode());
        hash = hash * 31 + (m_type == null ? 0 : m_type.hashCode());
        hash = hash * 31 + (m_status == null ? 0 : m_status.hashCode());
        hash = hash * 31 + (m_targetId == null ? 0 : m_targetId.hashCode());
        hash = hash * 31 + (m_targetData == null ? 0 : m_targetData.hashCode());
        hash = hash * 31 + (m_createTime == null ? 0 : m_createTime.hashCode());

        return hash;
    }



    public Message setCreateTime(java.util.Date createTime) {
        m_createTime = createTime;
        return this;
    }

    public Message setPerformer(String performer) {
        m_performer = performer;
        return this;
    }

    public Message setStatus(String status) {
        m_status = status;
        return this;
    }

    public Message setTargetData(String targetData) {
        m_targetData = targetData;
        return this;
    }

    public Message setTargetId(Long targetId) {
        m_targetId = targetId;
        return this;
    }

    public Message setType(String type) {
        m_type = type;
        return this;
    }

}
