package com.ctrip.zeus.model.model;

public class ServerWarInfo {
    private String m_commitId;

    public ServerWarInfo() {
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
        if (obj instanceof ServerWarInfo) {
            ServerWarInfo _o = (ServerWarInfo) obj;

            if (!equals(m_commitId, _o.getCommitId())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getCommitId() {
        return m_commitId;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_commitId == null ? 0 : m_commitId.hashCode());

        return hash;
    }


    public ServerWarInfo setCommitId(String commitId) {
        m_commitId = commitId;
        return this;
    }

}
