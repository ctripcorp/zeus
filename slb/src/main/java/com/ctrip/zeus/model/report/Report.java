package com.ctrip.zeus.model.report;

public class Report {
    private CmsResponse m_cmsResponse;

    private SlbMetricsList m_slbMetricsList;

    public Report() {
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
        if (obj instanceof Report) {
            Report _o = (Report) obj;

            if (!equals(m_cmsResponse, _o.getCmsResponse())) {
                return false;
            }

            if (!equals(m_slbMetricsList, _o.getSlbMetricsList())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public CmsResponse getCmsResponse() {
        return m_cmsResponse;
    }

    public SlbMetricsList getSlbMetricsList() {
        return m_slbMetricsList;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_cmsResponse == null ? 0 : m_cmsResponse.hashCode());
        hash = hash * 31 + (m_slbMetricsList == null ? 0 : m_slbMetricsList.hashCode());

        return hash;
    }



    public Report setCmsResponse(CmsResponse cmsResponse) {
        m_cmsResponse = cmsResponse;
        return this;
    }

    public Report setSlbMetricsList(SlbMetricsList slbMetricsList) {
        m_slbMetricsList = slbMetricsList;
        return this;
    }

}
