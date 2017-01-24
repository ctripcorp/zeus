package com.ctrip.zeus.restful.message.view;

import com.ctrip.zeus.model.entity.GroupServer;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.model.entity.HealthCheck;
import com.ctrip.zeus.model.entity.LoadBalancingMethod;
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
    abstract Date getCreatedTime();

    @JsonView(ViewConstraints.Detail.class)
    abstract List<GroupVirtualServer> getGroupVirtualServers();

    @JsonIgnore
    abstract Boolean getVirtual();
}
