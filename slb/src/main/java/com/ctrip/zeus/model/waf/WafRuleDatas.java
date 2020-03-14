package com.ctrip.zeus.model.waf;

import java.util.ArrayList;
import java.util.List;

public class WafRuleDatas {
    private Integer m_total;

    private List<RuleFile> m_ruleFiles = new ArrayList<RuleFile>();

    public WafRuleDatas() {
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



    public WafRuleDatas addRuleFile(RuleFile ruleFile) {
        m_ruleFiles.add(ruleFile);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WafRuleDatas) {
            WafRuleDatas _o = (WafRuleDatas) obj;

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_ruleFiles, _o.getRuleFiles())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<RuleFile> getRuleFiles() {
        return m_ruleFiles;
    }

    public Integer getTotal() {
        return m_total;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_ruleFiles == null ? 0 : m_ruleFiles.hashCode());

        return hash;
    }



    public WafRuleDatas setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
