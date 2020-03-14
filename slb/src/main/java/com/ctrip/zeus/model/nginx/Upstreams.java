package com.ctrip.zeus.model.nginx;

import java.util.ArrayList;
import java.util.List;

public class Upstreams {
    private List<ConfFile> m_files = new ArrayList<ConfFile>();

    public Upstreams() {
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



    public Upstreams addConfFile(ConfFile confFile) {
        m_files.add(confFile);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Upstreams) {
            Upstreams _o = (Upstreams) obj;

            if (!equals(m_files, _o.getFiles())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<ConfFile> getFiles() {
        return m_files;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_files == null ? 0 : m_files.hashCode());

        return hash;
    }



}
