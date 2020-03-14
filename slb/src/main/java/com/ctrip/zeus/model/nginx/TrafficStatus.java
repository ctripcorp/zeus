package com.ctrip.zeus.model.nginx;

import java.util.ArrayList;
import java.util.List;

public class TrafficStatus {
    private String m_serverIp;

    private Long m_activeConnections;

    private Long m_accepts;

    private Long m_handled;

    private Long m_requests;

    private Double m_responseTime;

    private Long m_reading;

    private Long m_writing;

    private Long m_waiting;

    private java.util.Date m_time;

    private List<ReqStatus> m_reqStatuses = new ArrayList<ReqStatus>();

    public TrafficStatus() {
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



    public TrafficStatus addReqStatus(ReqStatus reqStatus) {
        m_reqStatuses.add(reqStatus);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TrafficStatus) {
            TrafficStatus _o = (TrafficStatus) obj;

            if (!equals(m_serverIp, _o.getServerIp())) {
                return false;
            }

            if (!equals(m_activeConnections, _o.getActiveConnections())) {
                return false;
            }

            if (!equals(m_accepts, _o.getAccepts())) {
                return false;
            }

            if (!equals(m_handled, _o.getHandled())) {
                return false;
            }

            if (!equals(m_requests, _o.getRequests())) {
                return false;
            }

            if (!equals(m_responseTime, _o.getResponseTime())) {
                return false;
            }

            if (!equals(m_reading, _o.getReading())) {
                return false;
            }

            if (!equals(m_writing, _o.getWriting())) {
                return false;
            }

            if (!equals(m_waiting, _o.getWaiting())) {
                return false;
            }

            if (!equals(m_time, _o.getTime())) {
                return false;
            }

            if (!equals(m_reqStatuses, _o.getReqStatuses())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Long getAccepts() {
        return m_accepts;
    }

    public Long getActiveConnections() {
        return m_activeConnections;
    }

    public Long getHandled() {
        return m_handled;
    }

    public Long getReading() {
        return m_reading;
    }

    public List<ReqStatus> getReqStatuses() {
        return m_reqStatuses;
    }

    public Long getRequests() {
        return m_requests;
    }

    public Double getResponseTime() {
        return m_responseTime;
    }

    public String getServerIp() {
        return m_serverIp;
    }

    public java.util.Date getTime() {
        return m_time;
    }

    public Long getWaiting() {
        return m_waiting;
    }

    public Long getWriting() {
        return m_writing;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_serverIp == null ? 0 : m_serverIp.hashCode());
        hash = hash * 31 + (m_activeConnections == null ? 0 : m_activeConnections.hashCode());
        hash = hash * 31 + (m_accepts == null ? 0 : m_accepts.hashCode());
        hash = hash * 31 + (m_handled == null ? 0 : m_handled.hashCode());
        hash = hash * 31 + (m_requests == null ? 0 : m_requests.hashCode());
        hash = hash * 31 + (m_responseTime == null ? 0 : m_responseTime.hashCode());
        hash = hash * 31 + (m_reading == null ? 0 : m_reading.hashCode());
        hash = hash * 31 + (m_writing == null ? 0 : m_writing.hashCode());
        hash = hash * 31 + (m_waiting == null ? 0 : m_waiting.hashCode());
        hash = hash * 31 + (m_time == null ? 0 : m_time.hashCode());
        hash = hash * 31 + (m_reqStatuses == null ? 0 : m_reqStatuses.hashCode());

        return hash;
    }



    public TrafficStatus setAccepts(Long accepts) {
        m_accepts = accepts;
        return this;
    }

    public TrafficStatus setActiveConnections(Long activeConnections) {
        m_activeConnections = activeConnections;
        return this;
    }

    public TrafficStatus setHandled(Long handled) {
        m_handled = handled;
        return this;
    }

    public TrafficStatus setReading(Long reading) {
        m_reading = reading;
        return this;
    }

    public TrafficStatus setRequests(Long requests) {
        m_requests = requests;
        return this;
    }

    public TrafficStatus setResponseTime(Double responseTime) {
        m_responseTime = responseTime;
        return this;
    }

    public TrafficStatus setServerIp(String serverIp) {
        m_serverIp = serverIp;
        return this;
    }

    public TrafficStatus setTime(java.util.Date time) {
        m_time = time;
        return this;
    }

    public TrafficStatus setWaiting(Long waiting) {
        m_waiting = waiting;
        return this;
    }

    public TrafficStatus setWriting(Long writing) {
        m_writing = writing;
        return this;
    }

}
