package com.ctrip.zeus.model.nginx;

import java.util.ArrayList;
import java.util.List;

public class NginxServerStatusList {
    private Integer m_total;

    private List<NginxServerStatus> m_nginxServerStatuses = new ArrayList<NginxServerStatus>();

    public NginxServerStatusList() {
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



    public NginxServerStatusList addNginxServerStatus(NginxServerStatus nginxServerStatus) {
        m_nginxServerStatuses.add(nginxServerStatus);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NginxServerStatusList) {
            NginxServerStatusList _o = (NginxServerStatusList) obj;

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_nginxServerStatuses, _o.getNginxServerStatuses())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<NginxServerStatus> getNginxServerStatuses() {
        return m_nginxServerStatuses;
    }

    public Integer getTotal() {
        return m_total;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_nginxServerStatuses == null ? 0 : m_nginxServerStatuses.hashCode());

        return hash;
    }



    public NginxServerStatusList setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
