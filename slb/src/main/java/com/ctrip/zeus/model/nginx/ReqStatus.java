package com.ctrip.zeus.model.nginx;

public class ReqStatus {
    private String m_hostName;

    private Long m_slbId;

    private Long m_groupId;

    private String m_groupName;

    private Long m_bytesInTotal;

    private Long m_bytesOutTotal;

    private Long m_upRequests;

    private Double m_upResponseTime;

    private Long m_upTries;

    private Double m_responseTime;

    private Long m_totalRequests;

    private Long m_successCount;

    private Long m_redirectionCount;

    private Long m_clientErrCount;

    private Long m_serverErrCount;

    private java.util.Date m_time;

    public ReqStatus() {
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
        if (obj instanceof ReqStatus) {
            ReqStatus _o = (ReqStatus) obj;

            if (!equals(m_hostName, _o.getHostName())) {
                return false;
            }

            if (!equals(m_slbId, _o.getSlbId())) {
                return false;
            }

            if (!equals(m_groupId, _o.getGroupId())) {
                return false;
            }

            if (!equals(m_groupName, _o.getGroupName())) {
                return false;
            }

            if (!equals(m_bytesInTotal, _o.getBytesInTotal())) {
                return false;
            }

            if (!equals(m_bytesOutTotal, _o.getBytesOutTotal())) {
                return false;
            }

            if (!equals(m_upRequests, _o.getUpRequests())) {
                return false;
            }

            if (!equals(m_upResponseTime, _o.getUpResponseTime())) {
                return false;
            }

            if (!equals(m_upTries, _o.getUpTries())) {
                return false;
            }

            if (!equals(m_responseTime, _o.getResponseTime())) {
                return false;
            }

            if (!equals(m_totalRequests, _o.getTotalRequests())) {
                return false;
            }

            if (!equals(m_successCount, _o.getSuccessCount())) {
                return false;
            }

            if (!equals(m_redirectionCount, _o.getRedirectionCount())) {
                return false;
            }

            if (!equals(m_clientErrCount, _o.getClientErrCount())) {
                return false;
            }

            if (!equals(m_serverErrCount, _o.getServerErrCount())) {
                return false;
            }

            if (!equals(m_time, _o.getTime())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Long getBytesInTotal() {
        return m_bytesInTotal;
    }

    public Long getBytesOutTotal() {
        return m_bytesOutTotal;
    }

    public Long getClientErrCount() {
        return m_clientErrCount;
    }

    public Long getGroupId() {
        return m_groupId;
    }

    public String getGroupName() {
        return m_groupName;
    }

    public String getHostName() {
        return m_hostName;
    }

    public Long getRedirectionCount() {
        return m_redirectionCount;
    }

    public Double getResponseTime() {
        return m_responseTime;
    }

    public Long getServerErrCount() {
        return m_serverErrCount;
    }

    public Long getSlbId() {
        return m_slbId;
    }

    public Long getSuccessCount() {
        return m_successCount;
    }

    public java.util.Date getTime() {
        return m_time;
    }

    public Long getTotalRequests() {
        return m_totalRequests;
    }

    public Long getUpRequests() {
        return m_upRequests;
    }

    public Double getUpResponseTime() {
        return m_upResponseTime;
    }

    public Long getUpTries() {
        return m_upTries;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_hostName == null ? 0 : m_hostName.hashCode());
        hash = hash * 31 + (m_slbId == null ? 0 : m_slbId.hashCode());
        hash = hash * 31 + (m_groupId == null ? 0 : m_groupId.hashCode());
        hash = hash * 31 + (m_groupName == null ? 0 : m_groupName.hashCode());
        hash = hash * 31 + (m_bytesInTotal == null ? 0 : m_bytesInTotal.hashCode());
        hash = hash * 31 + (m_bytesOutTotal == null ? 0 : m_bytesOutTotal.hashCode());
        hash = hash * 31 + (m_upRequests == null ? 0 : m_upRequests.hashCode());
        hash = hash * 31 + (m_upResponseTime == null ? 0 : m_upResponseTime.hashCode());
        hash = hash * 31 + (m_upTries == null ? 0 : m_upTries.hashCode());
        hash = hash * 31 + (m_responseTime == null ? 0 : m_responseTime.hashCode());
        hash = hash * 31 + (m_totalRequests == null ? 0 : m_totalRequests.hashCode());
        hash = hash * 31 + (m_successCount == null ? 0 : m_successCount.hashCode());
        hash = hash * 31 + (m_redirectionCount == null ? 0 : m_redirectionCount.hashCode());
        hash = hash * 31 + (m_clientErrCount == null ? 0 : m_clientErrCount.hashCode());
        hash = hash * 31 + (m_serverErrCount == null ? 0 : m_serverErrCount.hashCode());
        hash = hash * 31 + (m_time == null ? 0 : m_time.hashCode());

        return hash;
    }



    public ReqStatus setBytesInTotal(Long bytesInTotal) {
        m_bytesInTotal = bytesInTotal;
        return this;
    }

    public ReqStatus setBytesOutTotal(Long bytesOutTotal) {
        m_bytesOutTotal = bytesOutTotal;
        return this;
    }

    public ReqStatus setClientErrCount(Long clientErrCount) {
        m_clientErrCount = clientErrCount;
        return this;
    }

    public ReqStatus setGroupId(Long groupId) {
        m_groupId = groupId;
        return this;
    }

    public ReqStatus setGroupName(String groupName) {
        m_groupName = groupName;
        return this;
    }

    public ReqStatus setHostName(String hostName) {
        m_hostName = hostName;
        return this;
    }

    public ReqStatus setRedirectionCount(Long redirectionCount) {
        m_redirectionCount = redirectionCount;
        return this;
    }

    public ReqStatus setResponseTime(Double responseTime) {
        m_responseTime = responseTime;
        return this;
    }

    public ReqStatus setServerErrCount(Long serverErrCount) {
        m_serverErrCount = serverErrCount;
        return this;
    }

    public ReqStatus setSlbId(Long slbId) {
        m_slbId = slbId;
        return this;
    }

    public ReqStatus setSuccessCount(Long successCount) {
        m_successCount = successCount;
        return this;
    }

    public ReqStatus setTime(java.util.Date time) {
        m_time = time;
        return this;
    }

    public ReqStatus setTotalRequests(Long totalRequests) {
        m_totalRequests = totalRequests;
        return this;
    }

    public ReqStatus setUpRequests(Long upRequests) {
        m_upRequests = upRequests;
        return this;
    }

    public ReqStatus setUpResponseTime(Double upResponseTime) {
        m_upResponseTime = upResponseTime;
        return this;
    }

    public ReqStatus setUpTries(Long upTries) {
        m_upTries = upTries;
        return this;
    }

}
