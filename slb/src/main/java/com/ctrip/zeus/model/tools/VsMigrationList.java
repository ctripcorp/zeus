package com.ctrip.zeus.model.tools;

import java.util.ArrayList;
import java.util.List;

public class VsMigrationList {
    private Integer m_total;

    private List<VsMigration> m_migrations = new ArrayList<VsMigration>();

    public VsMigrationList() {
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



    public VsMigrationList addVsMigration(VsMigration vsMigration) {
        m_migrations.add(vsMigration);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VsMigrationList) {
            VsMigrationList _o = (VsMigrationList) obj;

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_migrations, _o.getMigrations())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<VsMigration> getMigrations() {
        return m_migrations;
    }

    public Integer getTotal() {
        return m_total;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_migrations == null ? 0 : m_migrations.hashCode());

        return hash;
    }



    public VsMigrationList setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
