package com.ctrip.zeus.model.nginx;

import java.util.ArrayList;
import java.util.List;

public class NginxResponseList {
    private Integer m_total;

    private List<NginxResponse> m_nginxResponses = new ArrayList<NginxResponse>();

    public NginxResponseList() {
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



    public NginxResponseList addNginxResponse(NginxResponse nginxResponse) {
        m_nginxResponses.add(nginxResponse);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NginxResponseList) {
            NginxResponseList _o = (NginxResponseList) obj;

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_nginxResponses, _o.getNginxResponses())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<NginxResponse> getNginxResponses() {
        return m_nginxResponses;
    }

    public Integer getTotal() {
        return m_total;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_nginxResponses == null ? 0 : m_nginxResponses.hashCode());

        return hash;
    }



    public NginxResponseList setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
