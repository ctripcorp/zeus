package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class AppMetaList {
    private Integer m_total;

    private List<AppMeta> m_appMetas = new ArrayList<AppMeta>();

    public AppMetaList() {
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



    public AppMetaList addAppMeta(AppMeta appMeta) {
        m_appMetas.add(appMeta);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AppMetaList) {
            AppMetaList _o = (AppMetaList) obj;

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_appMetas, _o.getAppMetas())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<AppMeta> getAppMetas() {
        return m_appMetas;
    }

    public Integer getTotal() {
        return m_total;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_appMetas == null ? 0 : m_appMetas.hashCode());

        return hash;
    }



    public AppMetaList setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
