package com.ctrip.zeus.model.commit;

public class Model {
    private Commit m_commit;

    private ConfSlbVersion m_confSlbVersion;

    public Model() {
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
        if (obj instanceof Model) {
            Model _o = (Model) obj;

            if (!equals(m_commit, _o.getCommit())) {
                return false;
            }

            if (!equals(m_confSlbVersion, _o.getConfSlbVersion())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Commit getCommit() {
        return m_commit;
    }

    public ConfSlbVersion getConfSlbVersion() {
        return m_confSlbVersion;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_commit == null ? 0 : m_commit.hashCode());
        hash = hash * 31 + (m_confSlbVersion == null ? 0 : m_confSlbVersion.hashCode());

        return hash;
    }



    public Model setCommit(Commit commit) {
        m_commit = commit;
        return this;
    }

    public Model setConfSlbVersion(ConfSlbVersion confSlbVersion) {
        m_confSlbVersion = confSlbVersion;
        return this;
    }

}
