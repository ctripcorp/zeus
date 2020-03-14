package com.ctrip.zeus.restful.message.view;

import com.ctrip.zeus.model.model.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import java.util.Date;
import java.util.List;

/**
 * Created by zhoumy on 2016/7/25.
 */
public abstract class GroupView {
    @JsonView(ViewConstraints.Info.class)
    abstract Long getId();

    @JsonView(ViewConstraints.Info.class)
    abstract String getName();

    @JsonView(ViewConstraints.Info.class)
    abstract String getType();

    @JsonView(ViewConstraints.Info.class)
    abstract String getAppId();

    @JsonView(ViewConstraints.Normal.class)
    abstract Boolean getSsl();

    @JsonView(ViewConstraints.Normal.class)
    abstract Integer getVersion();

    @JsonView(ViewConstraints.Detail.class)
    abstract HealthCheck getHealthCheck();

    @JsonView(ViewConstraints.Detail.class)
    abstract LoadBalancingMethod getLoadBalancingMethod();

    @JsonView(ViewConstraints.Normal.class)
    abstract List<GroupServer> getGroupServers();

    @JsonView(ViewConstraints.Normal.class)
    abstract List<Rule> getRuleSet();

    @JsonView(ViewConstraints.Normal.class)
    abstract Date getCreatedTime();

    @JsonView(ViewConstraints.Detail.class)
    abstract List<GroupVirtualServer> getGroupVirtualServers();

    @JsonView(ViewConstraints.Detail.class)
    abstract Boolean getVirtual();


    @JsonView(ViewConstraints.Info.class)
    abstract void setId(Long id);

    @JsonView(ViewConstraints.Info.class)
    abstract void setName(String name);

    @JsonView(ViewConstraints.Info.class)
    abstract void setType(String type);

    @JsonView(ViewConstraints.Info.class)
    abstract void setAppId(String appId);

    @JsonView(ViewConstraints.Normal.class)
    abstract void setSsl(Boolean ssl);

    @JsonView(ViewConstraints.Normal.class)
    abstract void setVersion(Integer version);

    @JsonView(ViewConstraints.Detail.class)
    abstract void setHealthCheck(HealthCheck hc);

    @JsonView(ViewConstraints.Detail.class)
    abstract void setLoadBalancingMethod(LoadBalancingMethod loadBalancingMethod);

    @JsonView(ViewConstraints.Normal.class)
    abstract void setGroupServers(List<GroupServer> servers);

    @JsonView(ViewConstraints.Normal.class)
    abstract void setRuleSet(List<Rule> rules);

    @JsonView(ViewConstraints.Normal.class)
    abstract void setCreatedTime(Date date);

    @JsonView(ViewConstraints.Detail.class)
    abstract void setGroupVirtualServers(List<GroupVirtualServer> gvs);

    @JsonView(ViewConstraints.Detail.class)
    abstract void setVirtual(Boolean virtual);
}
