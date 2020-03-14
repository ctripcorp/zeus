package com.ctrip.zeus.model.nginx;

public class Nginx {
    private UpstreamStatus m_upstreamStatus;

    private NginxResponse m_nginxResponse;

    private NginxServerStatus m_nginxServerStatus;

    private NginxResponseList m_nginxResponseList;

    private NginxServerStatusList m_nginxServerStatusList;

    private TrafficStatusList m_trafficStatusList;

    private TrafficStatus m_trafficStatus;

    private ReqStatus m_reqStatus;

    private SlbConfResponse m_slbConfResponse;

    private VirtualServerConfResponse m_virtualServerConfResponse;

    private VsConfData m_vsConfData;

    private NginxConfEntry m_nginxConfEntry;

    private ConfFile m_confFile;

    public Nginx() {
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
        if (obj instanceof Nginx) {
            Nginx _o = (Nginx) obj;

            if (!equals(m_upstreamStatus, _o.getUpstreamStatus())) {
                return false;
            }

            if (!equals(m_nginxResponse, _o.getNginxResponse())) {
                return false;
            }

            if (!equals(m_nginxServerStatus, _o.getNginxServerStatus())) {
                return false;
            }

            if (!equals(m_nginxResponseList, _o.getNginxResponseList())) {
                return false;
            }

            if (!equals(m_nginxServerStatusList, _o.getNginxServerStatusList())) {
                return false;
            }

            if (!equals(m_trafficStatusList, _o.getTrafficStatusList())) {
                return false;
            }

            if (!equals(m_trafficStatus, _o.getTrafficStatus())) {
                return false;
            }

            if (!equals(m_reqStatus, _o.getReqStatus())) {
                return false;
            }

            if (!equals(m_slbConfResponse, _o.getSlbConfResponse())) {
                return false;
            }

            if (!equals(m_virtualServerConfResponse, _o.getVirtualServerConfResponse())) {
                return false;
            }

            if (!equals(m_vsConfData, _o.getVsConfData())) {
                return false;
            }

            if (!equals(m_nginxConfEntry, _o.getNginxConfEntry())) {
                return false;
            }

            if (!equals(m_confFile, _o.getConfFile())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public ConfFile getConfFile() {
        return m_confFile;
    }

    public NginxConfEntry getNginxConfEntry() {
        return m_nginxConfEntry;
    }

    public NginxResponse getNginxResponse() {
        return m_nginxResponse;
    }

    public NginxResponseList getNginxResponseList() {
        return m_nginxResponseList;
    }

    public NginxServerStatus getNginxServerStatus() {
        return m_nginxServerStatus;
    }

    public NginxServerStatusList getNginxServerStatusList() {
        return m_nginxServerStatusList;
    }

    public ReqStatus getReqStatus() {
        return m_reqStatus;
    }

    public SlbConfResponse getSlbConfResponse() {
        return m_slbConfResponse;
    }

    public TrafficStatus getTrafficStatus() {
        return m_trafficStatus;
    }

    public TrafficStatusList getTrafficStatusList() {
        return m_trafficStatusList;
    }

    public UpstreamStatus getUpstreamStatus() {
        return m_upstreamStatus;
    }

    public VirtualServerConfResponse getVirtualServerConfResponse() {
        return m_virtualServerConfResponse;
    }

    public VsConfData getVsConfData() {
        return m_vsConfData;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_upstreamStatus == null ? 0 : m_upstreamStatus.hashCode());
        hash = hash * 31 + (m_nginxResponse == null ? 0 : m_nginxResponse.hashCode());
        hash = hash * 31 + (m_nginxServerStatus == null ? 0 : m_nginxServerStatus.hashCode());
        hash = hash * 31 + (m_nginxResponseList == null ? 0 : m_nginxResponseList.hashCode());
        hash = hash * 31 + (m_nginxServerStatusList == null ? 0 : m_nginxServerStatusList.hashCode());
        hash = hash * 31 + (m_trafficStatusList == null ? 0 : m_trafficStatusList.hashCode());
        hash = hash * 31 + (m_trafficStatus == null ? 0 : m_trafficStatus.hashCode());
        hash = hash * 31 + (m_reqStatus == null ? 0 : m_reqStatus.hashCode());
        hash = hash * 31 + (m_slbConfResponse == null ? 0 : m_slbConfResponse.hashCode());
        hash = hash * 31 + (m_virtualServerConfResponse == null ? 0 : m_virtualServerConfResponse.hashCode());
        hash = hash * 31 + (m_vsConfData == null ? 0 : m_vsConfData.hashCode());
        hash = hash * 31 + (m_nginxConfEntry == null ? 0 : m_nginxConfEntry.hashCode());
        hash = hash * 31 + (m_confFile == null ? 0 : m_confFile.hashCode());

        return hash;
    }



    public Nginx setConfFile(ConfFile confFile) {
        m_confFile = confFile;
        return this;
    }

    public Nginx setNginxConfEntry(NginxConfEntry nginxConfEntry) {
        m_nginxConfEntry = nginxConfEntry;
        return this;
    }

    public Nginx setNginxResponse(NginxResponse nginxResponse) {
        m_nginxResponse = nginxResponse;
        return this;
    }

    public Nginx setNginxResponseList(NginxResponseList nginxResponseList) {
        m_nginxResponseList = nginxResponseList;
        return this;
    }

    public Nginx setNginxServerStatus(NginxServerStatus nginxServerStatus) {
        m_nginxServerStatus = nginxServerStatus;
        return this;
    }

    public Nginx setNginxServerStatusList(NginxServerStatusList nginxServerStatusList) {
        m_nginxServerStatusList = nginxServerStatusList;
        return this;
    }

    public Nginx setReqStatus(ReqStatus reqStatus) {
        m_reqStatus = reqStatus;
        return this;
    }

    public Nginx setSlbConfResponse(SlbConfResponse slbConfResponse) {
        m_slbConfResponse = slbConfResponse;
        return this;
    }

    public Nginx setTrafficStatus(TrafficStatus trafficStatus) {
        m_trafficStatus = trafficStatus;
        return this;
    }

    public Nginx setTrafficStatusList(TrafficStatusList trafficStatusList) {
        m_trafficStatusList = trafficStatusList;
        return this;
    }

    public Nginx setUpstreamStatus(UpstreamStatus upstreamStatus) {
        m_upstreamStatus = upstreamStatus;
        return this;
    }

    public Nginx setVirtualServerConfResponse(VirtualServerConfResponse virtualServerConfResponse) {
        m_virtualServerConfResponse = virtualServerConfResponse;
        return this;
    }

    public Nginx setVsConfData(VsConfData vsConfData) {
        m_vsConfData = vsConfData;
        return this;
    }

}
