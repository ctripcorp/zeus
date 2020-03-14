package com.ctrip.zeus.model.waf;

import java.util.ArrayList;
import java.util.List;

public class WafResponseList {
    private Integer m_status;

    private Integer m_total;

    private List<WafResponse> m_wafResponses = new ArrayList<WafResponse>();

    public WafResponseList() {
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



    public WafResponseList addWafResponse(WafResponse wafResponse) {
        m_wafResponses.add(wafResponse);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WafResponseList) {
            WafResponseList _o = (WafResponseList) obj;

            if (!equals(m_status, _o.getStatus())) {
                return false;
            }

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_wafResponses, _o.getWafResponses())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Integer getStatus() {
        return m_status;
    }

    public Integer getTotal() {
        return m_total;
    }

    public List<WafResponse> getWafResponses() {
        return m_wafResponses;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_status == null ? 0 : m_status.hashCode());
        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_wafResponses == null ? 0 : m_wafResponses.hashCode());

        return hash;
    }



    public WafResponseList setStatus(Integer status) {
        m_status = status;
        return this;
    }

    public WafResponseList setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
