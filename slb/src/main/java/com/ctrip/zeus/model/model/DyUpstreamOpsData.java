package com.ctrip.zeus.model.model;

public class DyUpstreamOpsData {
    private String m_upstreamName;

    private String m_upstreamCommands;

    public DyUpstreamOpsData() {
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
        if (obj instanceof DyUpstreamOpsData) {
            DyUpstreamOpsData _o = (DyUpstreamOpsData) obj;

            if (!equals(m_upstreamName, _o.getUpstreamName())) {
                return false;
            }

            if (!equals(m_upstreamCommands, _o.getUpstreamCommands())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getUpstreamCommands() {
        return m_upstreamCommands;
    }

    public String getUpstreamName() {
        return m_upstreamName;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_upstreamName == null ? 0 : m_upstreamName.hashCode());
        hash = hash * 31 + (m_upstreamCommands == null ? 0 : m_upstreamCommands.hashCode());

        return hash;
    }



    public DyUpstreamOpsData setUpstreamCommands(String upstreamCommands) {
        m_upstreamCommands = upstreamCommands;
        return this;
    }

    public DyUpstreamOpsData setUpstreamName(String upstreamName) {
        m_upstreamName = upstreamName;
        return this;
    }

}
