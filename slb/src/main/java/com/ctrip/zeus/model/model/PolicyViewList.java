package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class PolicyViewList {
    private Integer m_total;

    private List<PolicyView> m_policyViews = new ArrayList<PolicyView>();

    public PolicyViewList() {
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



    public PolicyViewList addPolicyView(PolicyView policyView) {
        m_policyViews.add(policyView);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PolicyViewList) {
            PolicyViewList _o = (PolicyViewList) obj;

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_policyViews, _o.getPolicyViews())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<PolicyView> getPolicyViews() {
        return m_policyViews;
    }

    public Integer getTotal() {
        return m_total;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_policyViews == null ? 0 : m_policyViews.hashCode());

        return hash;
    }



    public PolicyViewList setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
