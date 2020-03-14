package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class AppList {
    private Integer m_total;

    private List<App> m_apps = new ArrayList<App>();

    public AppList() {
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



    public AppList addApp(App app) {
        m_apps.add(app);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AppList) {
            AppList _o = (AppList) obj;

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_apps, _o.getApps())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<App> getApps() {
        return m_apps;
    }

    public Integer getTotal() {
        return m_total;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_apps == null ? 0 : m_apps.hashCode());

        return hash;
    }



    public AppList setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
