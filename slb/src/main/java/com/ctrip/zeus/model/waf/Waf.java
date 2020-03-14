package com.ctrip.zeus.model.waf;

public class Waf {
    private WafResponseList m_wafResponseList;

    private WafRuleDatas m_wafRuleDatas;

    public Waf() {
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
        if (obj instanceof Waf) {
            Waf _o = (Waf) obj;

            if (!equals(m_wafResponseList, _o.getWafResponseList())) {
                return false;
            }

            if (!equals(m_wafRuleDatas, _o.getWafRuleDatas())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public WafResponseList getWafResponseList() {
        return m_wafResponseList;
    }

    public WafRuleDatas getWafRuleDatas() {
        return m_wafRuleDatas;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_wafResponseList == null ? 0 : m_wafResponseList.hashCode());
        hash = hash * 31 + (m_wafRuleDatas == null ? 0 : m_wafRuleDatas.hashCode());

        return hash;
    }



    public Waf setWafResponseList(WafResponseList wafResponseList) {
        m_wafResponseList = wafResponseList;
        return this;
    }

    public Waf setWafRuleDatas(WafRuleDatas wafRuleDatas) {
        m_wafRuleDatas = wafRuleDatas;
        return this;
    }

}
