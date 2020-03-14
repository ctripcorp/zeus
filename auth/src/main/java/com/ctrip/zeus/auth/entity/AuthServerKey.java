package com.ctrip.zeus.auth.entity;

public class AuthServerKey {
    private String m_key;

    private String m_lastChange;

    public AuthServerKey() {
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
        if (obj instanceof AuthServerKey) {
            AuthServerKey _o = (AuthServerKey) obj;

            if (!equals(m_key, _o.getKey())) {
                return false;
            }

            if (!equals(m_lastChange, _o.getLastChange())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getKey() {
        return m_key;
    }

    public String getLastChange() {
        return m_lastChange;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_key == null ? 0 : m_key.hashCode());
        hash = hash * 31 + (m_lastChange == null ? 0 : m_lastChange.hashCode());

        return hash;
    }


    public AuthServerKey setKey(String key) {
        m_key = key;
        return this;
    }

    public AuthServerKey setLastChange(String lastChange) {
        m_lastChange = lastChange;
        return this;
    }

}
