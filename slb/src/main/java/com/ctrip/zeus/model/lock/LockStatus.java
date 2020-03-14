package com.ctrip.zeus.model.lock;

public class LockStatus {
    private String m_key;

    private Long m_owner;

    private String m_server;

    private java.util.Date m_createdTime;

    public LockStatus() {
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
        if (obj instanceof LockStatus) {
            LockStatus _o = (LockStatus) obj;

            if (!equals(m_key, _o.getKey())) {
                return false;
            }

            if (!equals(m_owner, _o.getOwner())) {
                return false;
            }

            if (!equals(m_server, _o.getServer())) {
                return false;
            }

            if (!equals(m_createdTime, _o.getCreatedTime())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public java.util.Date getCreatedTime() {
        return m_createdTime;
    }

    public String getKey() {
        return m_key;
    }

    public Long getOwner() {
        return m_owner;
    }

    public String getServer() {
        return m_server;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_key == null ? 0 : m_key.hashCode());
        hash = hash * 31 + (m_owner == null ? 0 : m_owner.hashCode());
        hash = hash * 31 + (m_server == null ? 0 : m_server.hashCode());
        hash = hash * 31 + (m_createdTime == null ? 0 : m_createdTime.hashCode());

        return hash;
    }



    public LockStatus setCreatedTime(java.util.Date createdTime) {
        m_createdTime = createdTime;
        return this;
    }

    public LockStatus setKey(String key) {
        m_key = key;
        return this;
    }

    public LockStatus setOwner(Long owner) {
        m_owner = owner;
        return this;
    }

    public LockStatus setServer(String server) {
        m_server = server;
        return this;
    }

}
