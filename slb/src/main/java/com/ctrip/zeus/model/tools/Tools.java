package com.ctrip.zeus.model.tools;

public class Tools {
    private CheckResponse m_checkResponse;

    private CheckSlbreleaseResponse m_checkSlbreleaseResponse;

    private CheckTarget m_checkTarget;

    private CheckTargetList m_checkTargetList;

    private Check m_check;

    private CheckList m_checkList;

    private AbtestTarget m_abtestTarget;

    private VsPing m_vsPing;

    private VsPingList m_vsPingList;

    private Domain m_domain;

    private VsMigration m_vsMigration;

    private VsMigrationList m_vsMigrationList;

    private Header m_header;

    private Param m_param;

    public Tools() {
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
        if (obj instanceof Tools) {
            Tools _o = (Tools) obj;

            if (!equals(m_checkResponse, _o.getCheckResponse())) {
                return false;
            }

            if (!equals(m_checkSlbreleaseResponse, _o.getCheckSlbreleaseResponse())) {
                return false;
            }

            if (!equals(m_checkTarget, _o.getCheckTarget())) {
                return false;
            }

            if (!equals(m_checkTargetList, _o.getCheckTargetList())) {
                return false;
            }

            if (!equals(m_check, _o.getCheck())) {
                return false;
            }

            if (!equals(m_checkList, _o.getCheckList())) {
                return false;
            }

            if (!equals(m_abtestTarget, _o.getAbtestTarget())) {
                return false;
            }

            if (!equals(m_vsPing, _o.getVsPing())) {
                return false;
            }

            if (!equals(m_vsPingList, _o.getVsPingList())) {
                return false;
            }

            if (!equals(m_domain, _o.getDomain())) {
                return false;
            }

            if (!equals(m_vsMigration, _o.getVsMigration())) {
                return false;
            }

            if (!equals(m_vsMigrationList, _o.getVsMigrationList())) {
                return false;
            }

            if (!equals(m_header, _o.getHeader())) {
                return false;
            }

            if (!equals(m_param, _o.getParam())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public AbtestTarget getAbtestTarget() {
        return m_abtestTarget;
    }

    public Check getCheck() {
        return m_check;
    }

    public CheckList getCheckList() {
        return m_checkList;
    }

    public CheckResponse getCheckResponse() {
        return m_checkResponse;
    }

    public CheckSlbreleaseResponse getCheckSlbreleaseResponse() {
        return m_checkSlbreleaseResponse;
    }

    public CheckTarget getCheckTarget() {
        return m_checkTarget;
    }

    public CheckTargetList getCheckTargetList() {
        return m_checkTargetList;
    }

    public Domain getDomain() {
        return m_domain;
    }

    public Header getHeader() {
        return m_header;
    }

    public Param getParam() {
        return m_param;
    }

    public VsMigration getVsMigration() {
        return m_vsMigration;
    }

    public VsMigrationList getVsMigrationList() {
        return m_vsMigrationList;
    }

    public VsPing getVsPing() {
        return m_vsPing;
    }

    public VsPingList getVsPingList() {
        return m_vsPingList;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_checkResponse == null ? 0 : m_checkResponse.hashCode());
        hash = hash * 31 + (m_checkSlbreleaseResponse == null ? 0 : m_checkSlbreleaseResponse.hashCode());
        hash = hash * 31 + (m_checkTarget == null ? 0 : m_checkTarget.hashCode());
        hash = hash * 31 + (m_checkTargetList == null ? 0 : m_checkTargetList.hashCode());
        hash = hash * 31 + (m_check == null ? 0 : m_check.hashCode());
        hash = hash * 31 + (m_checkList == null ? 0 : m_checkList.hashCode());
        hash = hash * 31 + (m_abtestTarget == null ? 0 : m_abtestTarget.hashCode());
        hash = hash * 31 + (m_vsPing == null ? 0 : m_vsPing.hashCode());
        hash = hash * 31 + (m_vsPingList == null ? 0 : m_vsPingList.hashCode());
        hash = hash * 31 + (m_domain == null ? 0 : m_domain.hashCode());
        hash = hash * 31 + (m_vsMigration == null ? 0 : m_vsMigration.hashCode());
        hash = hash * 31 + (m_vsMigrationList == null ? 0 : m_vsMigrationList.hashCode());
        hash = hash * 31 + (m_header == null ? 0 : m_header.hashCode());
        hash = hash * 31 + (m_param == null ? 0 : m_param.hashCode());

        return hash;
    }



    public Tools setAbtestTarget(AbtestTarget abtestTarget) {
        m_abtestTarget = abtestTarget;
        return this;
    }

    public Tools setCheck(Check check) {
        m_check = check;
        return this;
    }

    public Tools setCheckList(CheckList checkList) {
        m_checkList = checkList;
        return this;
    }

    public Tools setCheckResponse(CheckResponse checkResponse) {
        m_checkResponse = checkResponse;
        return this;
    }

    public Tools setCheckSlbreleaseResponse(CheckSlbreleaseResponse checkSlbreleaseResponse) {
        m_checkSlbreleaseResponse = checkSlbreleaseResponse;
        return this;
    }

    public Tools setCheckTarget(CheckTarget checkTarget) {
        m_checkTarget = checkTarget;
        return this;
    }

    public Tools setCheckTargetList(CheckTargetList checkTargetList) {
        m_checkTargetList = checkTargetList;
        return this;
    }

    public Tools setDomain(Domain domain) {
        m_domain = domain;
        return this;
    }

    public Tools setHeader(Header header) {
        m_header = header;
        return this;
    }

    public Tools setParam(Param param) {
        m_param = param;
        return this;
    }

    public Tools setVsMigration(VsMigration vsMigration) {
        m_vsMigration = vsMigration;
        return this;
    }

    public Tools setVsMigrationList(VsMigrationList vsMigrationList) {
        m_vsMigrationList = vsMigrationList;
        return this;
    }

    public Tools setVsPing(VsPing vsPing) {
        m_vsPing = vsPing;
        return this;
    }

    public Tools setVsPingList(VsPingList vsPingList) {
        m_vsPingList = vsPingList;
        return this;
    }

}
