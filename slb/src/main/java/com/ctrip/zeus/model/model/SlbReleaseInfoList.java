package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class SlbReleaseInfoList {
    private Integer m_total;

    private List<SlbReleaseInfo> m_slbsInfo = new ArrayList<SlbReleaseInfo>();

    public SlbReleaseInfoList() {
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



    public SlbReleaseInfoList addSlbReleaseInfo(SlbReleaseInfo slbReleaseInfo) {
        m_slbsInfo.add(slbReleaseInfo);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SlbReleaseInfoList) {
            SlbReleaseInfoList _o = (SlbReleaseInfoList) obj;

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_slbsInfo, _o.getSlbsInfo())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<SlbReleaseInfo> getSlbsInfo() {
        return m_slbsInfo;
    }

    public Integer getTotal() {
        return m_total;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_slbsInfo == null ? 0 : m_slbsInfo.hashCode());

        return hash;
    }



    public SlbReleaseInfoList setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
