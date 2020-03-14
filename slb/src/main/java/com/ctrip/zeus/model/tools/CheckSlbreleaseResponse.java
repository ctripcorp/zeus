package com.ctrip.zeus.model.tools;

public class CheckSlbreleaseResponse {
    private Integer m_code;

    private String m_status;

    private String m_commitId;

    public CheckSlbreleaseResponse() {
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
        if (obj instanceof CheckSlbreleaseResponse) {
            CheckSlbreleaseResponse _o = (CheckSlbreleaseResponse) obj;

            if (!equals(m_code, _o.getCode())) {
                return false;
            }

            if (!equals(m_status, _o.getStatus())) {
                return false;
            }

            if (!equals(m_commitId, _o.getCommitId())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Integer getCode() {
        return m_code;
    }

    public String getCommitId() {
        return m_commitId;
    }

    public String getStatus() {
        return m_status;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_code == null ? 0 : m_code.hashCode());
        hash = hash * 31 + (m_status == null ? 0 : m_status.hashCode());
        hash = hash * 31 + (m_commitId == null ? 0 : m_commitId.hashCode());

        return hash;
    }



    public CheckSlbreleaseResponse setCode(Integer code) {
        m_code = code;
        return this;
    }

    public CheckSlbreleaseResponse setCommitId(String commitId) {
        m_commitId = commitId;
        return this;
    }

    public CheckSlbreleaseResponse setStatus(String status) {
        m_status = status;
        return this;
    }

}
