package com.ctrip.zeus.model.feedback;

public class FeedbackData {
    private Long m_id;

    private String m_user;

    private java.util.Date m_createTime;

    private String m_description;

    public FeedbackData() {
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
        if (obj instanceof FeedbackData) {
            FeedbackData _o = (FeedbackData) obj;

            if (!equals(m_id, _o.getId())) {
                return false;
            }

            if (!equals(m_user, _o.getUser())) {
                return false;
            }

            if (!equals(m_createTime, _o.getCreateTime())) {
                return false;
            }

            if (!equals(m_description, _o.getDescription())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public java.util.Date getCreateTime() {
        return m_createTime;
    }

    public String getDescription() {
        return m_description;
    }

    public Long getId() {
        return m_id;
    }

    public String getUser() {
        return m_user;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());
        hash = hash * 31 + (m_user == null ? 0 : m_user.hashCode());
        hash = hash * 31 + (m_createTime == null ? 0 : m_createTime.hashCode());
        hash = hash * 31 + (m_description == null ? 0 : m_description.hashCode());

        return hash;
    }



    public FeedbackData setCreateTime(java.util.Date createTime) {
        m_createTime = createTime;
        return this;
    }

    public FeedbackData setDescription(String description) {
        m_description = description;
        return this;
    }

    public FeedbackData setId(Long id) {
        m_id = id;
        return this;
    }

    public FeedbackData setUser(String user) {
        m_user = user;
        return this;
    }

}
