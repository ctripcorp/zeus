package com.ctrip.zeus.model.queue;

import java.util.ArrayList;
import java.util.List;

public class SlbMessageData {
    private String m_uri;

    private String m_query;

    private String m_user;

    private String m_type;

    private String m_clientIp;

    private String m_description;

    private String m_errorMessage;

    private String m_outMessage;

    private Boolean m_success;

    private List<String> m_ips = new ArrayList<String>();

    private CertData m_certData;

    private List<GroupData> m_groupDatas = new ArrayList<GroupData>();

    private List<VsData> m_vsDatas = new ArrayList<VsData>();

    private List<SlbData> m_slbDatas = new ArrayList<SlbData>();

    private List<PolicyData> m_policyDatas = new ArrayList<PolicyData>();

    private List<DrData> m_drDatas = new ArrayList<DrData>();

    private FlowData m_flowData;

    private List<RuleData> m_ruleDatas = new ArrayList<RuleData>();

    public SlbMessageData() {
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



    public SlbMessageData addDrData(DrData drData) {
        m_drDatas.add(drData);
        return this;
    }

    public SlbMessageData addGroupData(GroupData groupData) {
        m_groupDatas.add(groupData);
        return this;
    }

    public SlbMessageData addIp(String ip) {
        m_ips.add(ip);
        return this;
    }

    public SlbMessageData addPolicyData(PolicyData policyData) {
        m_policyDatas.add(policyData);
        return this;
    }

    public SlbMessageData addRuleData(RuleData ruleData) {
        m_ruleDatas.add(ruleData);
        return this;
    }

    public SlbMessageData addSlbData(SlbData slbData) {
        m_slbDatas.add(slbData);
        return this;
    }

    public SlbMessageData addVsData(VsData vsData) {
        m_vsDatas.add(vsData);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SlbMessageData) {
            SlbMessageData _o = (SlbMessageData) obj;

            if (!equals(m_uri, _o.getUri())) {
                return false;
            }

            if (!equals(m_query, _o.getQuery())) {
                return false;
            }

            if (!equals(m_user, _o.getUser())) {
                return false;
            }

            if (!equals(m_type, _o.getType())) {
                return false;
            }

            if (!equals(m_clientIp, _o.getClientIp())) {
                return false;
            }

            if (!equals(m_description, _o.getDescription())) {
                return false;
            }

            if (!equals(m_errorMessage, _o.getErrorMessage())) {
                return false;
            }

            if (!equals(m_outMessage, _o.getOutMessage())) {
                return false;
            }

            if (!equals(m_success, _o.getSuccess())) {
                return false;
            }

            if (!equals(m_ips, _o.getIps())) {
                return false;
            }

            if (!equals(m_certData, _o.getCertData())) {
                return false;
            }

            if (!equals(m_groupDatas, _o.getGroupDatas())) {
                return false;
            }

            if (!equals(m_vsDatas, _o.getVsDatas())) {
                return false;
            }

            if (!equals(m_slbDatas, _o.getSlbDatas())) {
                return false;
            }

            if (!equals(m_policyDatas, _o.getPolicyDatas())) {
                return false;
            }

            if (!equals(m_drDatas, _o.getDrDatas())) {
                return false;
            }

            if (!equals(m_flowData, _o.getFlowData())) {
                return false;
            }

            if (!equals(m_ruleDatas, _o.getRuleDatas())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public CertData getCertData() {
        return m_certData;
    }

    public String getClientIp() {
        return m_clientIp;
    }

    public String getDescription() {
        return m_description;
    }

    public List<DrData> getDrDatas() {
        return m_drDatas;
    }

    public String getErrorMessage() {
        return m_errorMessage;
    }

    public FlowData getFlowData() {
        return m_flowData;
    }

    public List<GroupData> getGroupDatas() {
        return m_groupDatas;
    }

    public List<String> getIps() {
        return m_ips;
    }

    public String getOutMessage() {
        return m_outMessage;
    }

    public List<PolicyData> getPolicyDatas() {
        return m_policyDatas;
    }

    public String getQuery() {
        return m_query;
    }

    public List<RuleData> getRuleDatas() {
        return m_ruleDatas;
    }

    public List<SlbData> getSlbDatas() {
        return m_slbDatas;
    }

    public Boolean getSuccess() {
        return m_success;
    }

    public String getType() {
        return m_type;
    }

    public String getUri() {
        return m_uri;
    }

    public String getUser() {
        return m_user;
    }

    public List<VsData> getVsDatas() {
        return m_vsDatas;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_uri == null ? 0 : m_uri.hashCode());
        hash = hash * 31 + (m_query == null ? 0 : m_query.hashCode());
        hash = hash * 31 + (m_user == null ? 0 : m_user.hashCode());
        hash = hash * 31 + (m_type == null ? 0 : m_type.hashCode());
        hash = hash * 31 + (m_clientIp == null ? 0 : m_clientIp.hashCode());
        hash = hash * 31 + (m_description == null ? 0 : m_description.hashCode());
        hash = hash * 31 + (m_errorMessage == null ? 0 : m_errorMessage.hashCode());
        hash = hash * 31 + (m_outMessage == null ? 0 : m_outMessage.hashCode());
        hash = hash * 31 + (m_success == null ? 0 : m_success.hashCode());
        hash = hash * 31 + (m_ips == null ? 0 : m_ips.hashCode());
        hash = hash * 31 + (m_certData == null ? 0 : m_certData.hashCode());
        hash = hash * 31 + (m_groupDatas == null ? 0 : m_groupDatas.hashCode());
        hash = hash * 31 + (m_vsDatas == null ? 0 : m_vsDatas.hashCode());
        hash = hash * 31 + (m_slbDatas == null ? 0 : m_slbDatas.hashCode());
        hash = hash * 31 + (m_policyDatas == null ? 0 : m_policyDatas.hashCode());
        hash = hash * 31 + (m_drDatas == null ? 0 : m_drDatas.hashCode());
        hash = hash * 31 + (m_flowData == null ? 0 : m_flowData.hashCode());
        hash = hash * 31 + (m_ruleDatas == null ? 0 : m_ruleDatas.hashCode());

        return hash;
    }

    public boolean isSuccess() {
        return m_success != null && m_success.booleanValue();
    }



    public SlbMessageData setCertData(CertData certData) {
        m_certData = certData;
        return this;
    }

    public SlbMessageData setClientIp(String clientIp) {
        m_clientIp = clientIp;
        return this;
    }

    public SlbMessageData setDescription(String description) {
        m_description = description;
        return this;
    }

    public SlbMessageData setErrorMessage(String errorMessage) {
        m_errorMessage = errorMessage;
        return this;
    }

    public SlbMessageData setFlowData(FlowData flowData) {
        m_flowData = flowData;
        return this;
    }

    public SlbMessageData setOutMessage(String outMessage) {
        m_outMessage = outMessage;
        return this;
    }

    public SlbMessageData setQuery(String query) {
        m_query = query;
        return this;
    }

    public SlbMessageData setSuccess(Boolean success) {
        m_success = success;
        return this;
    }

    public SlbMessageData setType(String type) {
        m_type = type;
        return this;
    }

    public SlbMessageData setUri(String uri) {
        m_uri = uri;
        return this;
    }

    public SlbMessageData setUser(String user) {
        m_user = user;
        return this;
    }

}
