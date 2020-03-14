package com.ctrip.zeus.restful.message.view;

import com.ctrip.zeus.model.model.PolicyVirtualServer;
import com.ctrip.zeus.model.model.TrafficControl;
import com.fasterxml.jackson.annotation.JsonView;

import java.util.Date;
import java.util.List;

/**
 * Created by zhoumy on 2016/7/26.
 */
public abstract class TrafficPolicyView {

    @JsonView(ViewConstraints.Info.class)
    abstract Long getId();

    @JsonView(ViewConstraints.Info.class)
    abstract String getName();

    @JsonView(ViewConstraints.Normal.class)
    abstract List<TrafficControl> getControls();

    @JsonView(ViewConstraints.Normal.class)
    abstract List<PolicyVirtualServer> getPolicyVirtualServers();

    @JsonView(ViewConstraints.Normal.class)
    abstract Integer getVersion();

    @JsonView(ViewConstraints.Normal.class)
    abstract Date getCreatedTime();


    @JsonView(ViewConstraints.Info.class)
    abstract void setId(Long id);

    @JsonView(ViewConstraints.Info.class)
    abstract void setName(String name);


    @JsonView(ViewConstraints.Normal.class)
    abstract void setVersion(Integer version);

    @JsonView(ViewConstraints.Normal.class)
    abstract void setControls(List<TrafficControl> controls);

    @JsonView(ViewConstraints.Normal.class)
    abstract void setPolicyVirtualServers(List<PolicyVirtualServer> pvs);

    @JsonView(ViewConstraints.Normal.class)
    abstract void setCreatedTime(Date time);
}
