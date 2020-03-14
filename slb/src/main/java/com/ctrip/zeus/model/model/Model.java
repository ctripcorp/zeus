package com.ctrip.zeus.model.model;

public class Model {
    private Group m_group;

    private GroupVirtualServer m_groupVirtualServer;

    private GroupServer m_groupServer;

    private RouteRule m_routeRule;

    private VirtualServer m_virtualServer;

    private Slb m_slb;

    private Rule m_rule;

    private Rules m_rules;

    private ConditionRuleAttribute m_conditionRuleAttribute;

    private Condition m_condition;

    private ConditionAction m_conditionAction;

    private ConditionHeader m_conditionHeader;

    private PolicyVirtualServer m_policyVirtualServer;

    private TrafficPolicy m_trafficPolicy;

    private TrafficControl m_trafficControl;

    private PolicyViewList m_policyViewList;

    private GroupList m_groupList;

    private GroupServerList m_groupServerList;

    private VirtualServerList m_virtualServerList;

    private SlbList m_slbList;

    private GroupVsBound m_groupVsBound;

    private GroupVsBoundList m_groupVsBoundList;

    private SlbReleaseInfo m_slbReleaseInfo;

    private SlbReleaseInfoList m_slbReleaseInfoList;

    private ServerWarInfo m_serverWarInfo;

    private SlbGroupCheckFailureEntityList m_slbGroupCheckFailureEntityList;

    private SlbGroupCheckFailureEntity m_slbGroupCheckFailureEntity;

    private MemberAction m_memberAction;

    private ServerAction m_serverAction;

    private ConfReq m_confReq;

    private DyUpstreamOpsData m_dyUpstreamOpsData;

    private SlbValidateResponse m_slbValidateResponse;

    private Dr m_dr;

    private DrTraffic m_drTraffic;

    private Destination m_destination;

    private SoaConfig m_soaConfig;

    private SoaDefaultConfig m_soaDefaultConfig;

    public Model() {
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
        if (obj instanceof Model) {
            Model _o = (Model) obj;

            if (!equals(m_group, _o.getGroup())) {
                return false;
            }

            if (!equals(m_groupVirtualServer, _o.getGroupVirtualServer())) {
                return false;
            }

            if (!equals(m_groupServer, _o.getGroupServer())) {
                return false;
            }

            if (!equals(m_routeRule, _o.getRouteRule())) {
                return false;
            }

            if (!equals(m_virtualServer, _o.getVirtualServer())) {
                return false;
            }

            if (!equals(m_slb, _o.getSlb())) {
                return false;
            }

            if (!equals(m_rule, _o.getRule())) {
                return false;
            }

            if (!equals(m_rules, _o.getRules())) {
                return false;
            }

            if (!equals(m_conditionRuleAttribute, _o.getConditionRuleAttribute())) {
                return false;
            }

            if (!equals(m_condition, _o.getCondition())) {
                return false;
            }

            if (!equals(m_conditionAction, _o.getConditionAction())) {
                return false;
            }

            if (!equals(m_conditionHeader, _o.getConditionHeader())) {
                return false;
            }

            if (!equals(m_policyVirtualServer, _o.getPolicyVirtualServer())) {
                return false;
            }

            if (!equals(m_trafficPolicy, _o.getTrafficPolicy())) {
                return false;
            }

            if (!equals(m_trafficControl, _o.getTrafficControl())) {
                return false;
            }

            if (!equals(m_policyViewList, _o.getPolicyViewList())) {
                return false;
            }

            if (!equals(m_groupList, _o.getGroupList())) {
                return false;
            }

            if (!equals(m_groupServerList, _o.getGroupServerList())) {
                return false;
            }

            if (!equals(m_virtualServerList, _o.getVirtualServerList())) {
                return false;
            }

            if (!equals(m_slbList, _o.getSlbList())) {
                return false;
            }

            if (!equals(m_groupVsBound, _o.getGroupVsBound())) {
                return false;
            }

            if (!equals(m_groupVsBoundList, _o.getGroupVsBoundList())) {
                return false;
            }

            if (!equals(m_slbReleaseInfo, _o.getSlbReleaseInfo())) {
                return false;
            }

            if (!equals(m_slbReleaseInfoList, _o.getSlbReleaseInfoList())) {
                return false;
            }

            if (!equals(m_serverWarInfo, _o.getServerWarInfo())) {
                return false;
            }

            if (!equals(m_slbGroupCheckFailureEntityList, _o.getSlbGroupCheckFailureEntityList())) {
                return false;
            }

            if (!equals(m_slbGroupCheckFailureEntity, _o.getSlbGroupCheckFailureEntity())) {
                return false;
            }

            if (!equals(m_memberAction, _o.getMemberAction())) {
                return false;
            }

            if (!equals(m_serverAction, _o.getServerAction())) {
                return false;
            }

            if (!equals(m_confReq, _o.getConfReq())) {
                return false;
            }

            if (!equals(m_dyUpstreamOpsData, _o.getDyUpstreamOpsData())) {
                return false;
            }

            if (!equals(m_slbValidateResponse, _o.getSlbValidateResponse())) {
                return false;
            }

            if (!equals(m_dr, _o.getDr())) {
                return false;
            }

            if (!equals(m_drTraffic, _o.getDrTraffic())) {
                return false;
            }

            if (!equals(m_destination, _o.getDestination())) {
                return false;
            }

            if (!equals(m_soaConfig, _o.getSoaConfig())) {
                return false;
            }

            if (!equals(m_soaDefaultConfig, _o.getSoaDefaultConfig())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Condition getCondition() {
        return m_condition;
    }

    public ConditionAction getConditionAction() {
        return m_conditionAction;
    }

    public ConditionHeader getConditionHeader() {
        return m_conditionHeader;
    }

    public ConditionRuleAttribute getConditionRuleAttribute() {
        return m_conditionRuleAttribute;
    }

    public ConfReq getConfReq() {
        return m_confReq;
    }

    public Destination getDestination() {
        return m_destination;
    }

    public Dr getDr() {
        return m_dr;
    }

    public DrTraffic getDrTraffic() {
        return m_drTraffic;
    }

    public DyUpstreamOpsData getDyUpstreamOpsData() {
        return m_dyUpstreamOpsData;
    }

    public Group getGroup() {
        return m_group;
    }

    public GroupList getGroupList() {
        return m_groupList;
    }

    public GroupServer getGroupServer() {
        return m_groupServer;
    }

    public GroupServerList getGroupServerList() {
        return m_groupServerList;
    }

    public GroupVirtualServer getGroupVirtualServer() {
        return m_groupVirtualServer;
    }

    public GroupVsBound getGroupVsBound() {
        return m_groupVsBound;
    }

    public GroupVsBoundList getGroupVsBoundList() {
        return m_groupVsBoundList;
    }

    public MemberAction getMemberAction() {
        return m_memberAction;
    }

    public PolicyViewList getPolicyViewList() {
        return m_policyViewList;
    }

    public PolicyVirtualServer getPolicyVirtualServer() {
        return m_policyVirtualServer;
    }

    public RouteRule getRouteRule() {
        return m_routeRule;
    }

    public Rule getRule() {
        return m_rule;
    }

    public Rules getRules() {
        return m_rules;
    }

    public ServerAction getServerAction() {
        return m_serverAction;
    }

    public ServerWarInfo getServerWarInfo() {
        return m_serverWarInfo;
    }

    public Slb getSlb() {
        return m_slb;
    }

    public SlbGroupCheckFailureEntity getSlbGroupCheckFailureEntity() {
        return m_slbGroupCheckFailureEntity;
    }

    public SlbGroupCheckFailureEntityList getSlbGroupCheckFailureEntityList() {
        return m_slbGroupCheckFailureEntityList;
    }

    public SlbList getSlbList() {
        return m_slbList;
    }

    public SlbReleaseInfo getSlbReleaseInfo() {
        return m_slbReleaseInfo;
    }

    public SlbReleaseInfoList getSlbReleaseInfoList() {
        return m_slbReleaseInfoList;
    }

    public SlbValidateResponse getSlbValidateResponse() {
        return m_slbValidateResponse;
    }

    public SoaConfig getSoaConfig() {
        return m_soaConfig;
    }

    public SoaDefaultConfig getSoaDefaultConfig() {
        return m_soaDefaultConfig;
    }

    public TrafficControl getTrafficControl() {
        return m_trafficControl;
    }

    public TrafficPolicy getTrafficPolicy() {
        return m_trafficPolicy;
    }

    public VirtualServer getVirtualServer() {
        return m_virtualServer;
    }

    public VirtualServerList getVirtualServerList() {
        return m_virtualServerList;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_group == null ? 0 : m_group.hashCode());
        hash = hash * 31 + (m_groupVirtualServer == null ? 0 : m_groupVirtualServer.hashCode());
        hash = hash * 31 + (m_groupServer == null ? 0 : m_groupServer.hashCode());
        hash = hash * 31 + (m_routeRule == null ? 0 : m_routeRule.hashCode());
        hash = hash * 31 + (m_virtualServer == null ? 0 : m_virtualServer.hashCode());
        hash = hash * 31 + (m_slb == null ? 0 : m_slb.hashCode());
        hash = hash * 31 + (m_rule == null ? 0 : m_rule.hashCode());
        hash = hash * 31 + (m_rules == null ? 0 : m_rules.hashCode());
        hash = hash * 31 + (m_conditionRuleAttribute == null ? 0 : m_conditionRuleAttribute.hashCode());
        hash = hash * 31 + (m_condition == null ? 0 : m_condition.hashCode());
        hash = hash * 31 + (m_conditionAction == null ? 0 : m_conditionAction.hashCode());
        hash = hash * 31 + (m_conditionHeader == null ? 0 : m_conditionHeader.hashCode());
        hash = hash * 31 + (m_policyVirtualServer == null ? 0 : m_policyVirtualServer.hashCode());
        hash = hash * 31 + (m_trafficPolicy == null ? 0 : m_trafficPolicy.hashCode());
        hash = hash * 31 + (m_trafficControl == null ? 0 : m_trafficControl.hashCode());
        hash = hash * 31 + (m_policyViewList == null ? 0 : m_policyViewList.hashCode());
        hash = hash * 31 + (m_groupList == null ? 0 : m_groupList.hashCode());
        hash = hash * 31 + (m_groupServerList == null ? 0 : m_groupServerList.hashCode());
        hash = hash * 31 + (m_virtualServerList == null ? 0 : m_virtualServerList.hashCode());
        hash = hash * 31 + (m_slbList == null ? 0 : m_slbList.hashCode());
        hash = hash * 31 + (m_groupVsBound == null ? 0 : m_groupVsBound.hashCode());
        hash = hash * 31 + (m_groupVsBoundList == null ? 0 : m_groupVsBoundList.hashCode());
        hash = hash * 31 + (m_slbReleaseInfo == null ? 0 : m_slbReleaseInfo.hashCode());
        hash = hash * 31 + (m_slbReleaseInfoList == null ? 0 : m_slbReleaseInfoList.hashCode());
        hash = hash * 31 + (m_serverWarInfo == null ? 0 : m_serverWarInfo.hashCode());
        hash = hash * 31 + (m_slbGroupCheckFailureEntityList == null ? 0 : m_slbGroupCheckFailureEntityList.hashCode());
        hash = hash * 31 + (m_slbGroupCheckFailureEntity == null ? 0 : m_slbGroupCheckFailureEntity.hashCode());
        hash = hash * 31 + (m_memberAction == null ? 0 : m_memberAction.hashCode());
        hash = hash * 31 + (m_serverAction == null ? 0 : m_serverAction.hashCode());
        hash = hash * 31 + (m_confReq == null ? 0 : m_confReq.hashCode());
        hash = hash * 31 + (m_dyUpstreamOpsData == null ? 0 : m_dyUpstreamOpsData.hashCode());
        hash = hash * 31 + (m_slbValidateResponse == null ? 0 : m_slbValidateResponse.hashCode());
        hash = hash * 31 + (m_dr == null ? 0 : m_dr.hashCode());
        hash = hash * 31 + (m_drTraffic == null ? 0 : m_drTraffic.hashCode());
        hash = hash * 31 + (m_destination == null ? 0 : m_destination.hashCode());
        hash = hash * 31 + (m_soaConfig == null ? 0 : m_soaConfig.hashCode());
        hash = hash * 31 + (m_soaDefaultConfig == null ? 0 : m_soaDefaultConfig.hashCode());

        return hash;
    }



    public Model setCondition(Condition condition) {
        m_condition = condition;
        return this;
    }

    public Model setConditionAction(ConditionAction conditionAction) {
        m_conditionAction = conditionAction;
        return this;
    }

    public Model setConditionHeader(ConditionHeader conditionHeader) {
        m_conditionHeader = conditionHeader;
        return this;
    }

    public Model setConditionRuleAttribute(ConditionRuleAttribute conditionRuleAttribute) {
        m_conditionRuleAttribute = conditionRuleAttribute;
        return this;
    }

    public Model setConfReq(ConfReq confReq) {
        m_confReq = confReq;
        return this;
    }

    public Model setDestination(Destination destination) {
        m_destination = destination;
        return this;
    }

    public Model setDr(Dr dr) {
        m_dr = dr;
        return this;
    }

    public Model setDrTraffic(DrTraffic drTraffic) {
        m_drTraffic = drTraffic;
        return this;
    }

    public Model setDyUpstreamOpsData(DyUpstreamOpsData dyUpstreamOpsData) {
        m_dyUpstreamOpsData = dyUpstreamOpsData;
        return this;
    }

    public Model setGroup(Group group) {
        m_group = group;
        return this;
    }

    public Model setGroupList(GroupList groupList) {
        m_groupList = groupList;
        return this;
    }

    public Model setGroupServer(GroupServer groupServer) {
        m_groupServer = groupServer;
        return this;
    }

    public Model setGroupServerList(GroupServerList groupServerList) {
        m_groupServerList = groupServerList;
        return this;
    }

    public Model setGroupVirtualServer(GroupVirtualServer groupVirtualServer) {
        m_groupVirtualServer = groupVirtualServer;
        return this;
    }

    public Model setGroupVsBound(GroupVsBound groupVsBound) {
        m_groupVsBound = groupVsBound;
        return this;
    }

    public Model setGroupVsBoundList(GroupVsBoundList groupVsBoundList) {
        m_groupVsBoundList = groupVsBoundList;
        return this;
    }

    public Model setMemberAction(MemberAction memberAction) {
        m_memberAction = memberAction;
        return this;
    }

    public Model setPolicyViewList(PolicyViewList policyViewList) {
        m_policyViewList = policyViewList;
        return this;
    }

    public Model setPolicyVirtualServer(PolicyVirtualServer policyVirtualServer) {
        m_policyVirtualServer = policyVirtualServer;
        return this;
    }

    public Model setRouteRule(RouteRule routeRule) {
        m_routeRule = routeRule;
        return this;
    }

    public Model setRule(Rule rule) {
        m_rule = rule;
        return this;
    }

    public Model setRules(Rules rules) {
        m_rules = rules;
        return this;
    }

    public Model setServerAction(ServerAction serverAction) {
        m_serverAction = serverAction;
        return this;
    }

    public Model setServerWarInfo(ServerWarInfo serverWarInfo) {
        m_serverWarInfo = serverWarInfo;
        return this;
    }

    public Model setSlb(Slb slb) {
        m_slb = slb;
        return this;
    }

    public Model setSlbGroupCheckFailureEntity(SlbGroupCheckFailureEntity slbGroupCheckFailureEntity) {
        m_slbGroupCheckFailureEntity = slbGroupCheckFailureEntity;
        return this;
    }

    public Model setSlbGroupCheckFailureEntityList(SlbGroupCheckFailureEntityList slbGroupCheckFailureEntityList) {
        m_slbGroupCheckFailureEntityList = slbGroupCheckFailureEntityList;
        return this;
    }

    public Model setSlbList(SlbList slbList) {
        m_slbList = slbList;
        return this;
    }

    public Model setSlbReleaseInfo(SlbReleaseInfo slbReleaseInfo) {
        m_slbReleaseInfo = slbReleaseInfo;
        return this;
    }

    public Model setSlbReleaseInfoList(SlbReleaseInfoList slbReleaseInfoList) {
        m_slbReleaseInfoList = slbReleaseInfoList;
        return this;
    }

    public Model setSlbValidateResponse(SlbValidateResponse slbValidateResponse) {
        m_slbValidateResponse = slbValidateResponse;
        return this;
    }

    public Model setSoaConfig(SoaConfig soaConfig) {
        m_soaConfig = soaConfig;
        return this;
    }

    public Model setSoaDefaultConfig(SoaDefaultConfig soaDefaultConfig) {
        m_soaDefaultConfig = soaDefaultConfig;
        return this;
    }

    public Model setTrafficControl(TrafficControl trafficControl) {
        m_trafficControl = trafficControl;
        return this;
    }

    public Model setTrafficPolicy(TrafficPolicy trafficPolicy) {
        m_trafficPolicy = trafficPolicy;
        return this;
    }

    public Model setVirtualServer(VirtualServer virtualServer) {
        m_virtualServer = virtualServer;
        return this;
    }

    public Model setVirtualServerList(VirtualServerList virtualServerList) {
        m_virtualServerList = virtualServerList;
        return this;
    }

}
