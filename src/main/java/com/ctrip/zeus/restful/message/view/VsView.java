package com.ctrip.zeus.restful.message.view;

import com.ctrip.zeus.model.entity.Domain;
import com.fasterxml.jackson.annotation.JsonView;

import java.util.Date;
import java.util.List;

/**
 * Created by zhoumy on 2016/7/26.
 */
public abstract class VsView {

    @JsonView(ViewConstraints.Info.class)
    abstract Long getId();

    @JsonView(ViewConstraints.Info.class)
    abstract String getName();

    @JsonView(ViewConstraints.Normal.class)
    abstract String getPort();

    @JsonView(ViewConstraints.Normal.class)
    abstract Long getSlbId();

    @JsonView(ViewConstraints.Normal.class)
    abstract List<Long> getSlbIds();

    @JsonView(ViewConstraints.Normal.class)
    abstract Boolean getSsl();

    @JsonView(ViewConstraints.Normal.class)
    abstract Integer getVersion();

    @JsonView(ViewConstraints.Normal.class)
    abstract List<Domain> getDomains();

    @JsonView(ViewConstraints.Normal.class)
    abstract Date getCreatedTime();
}
