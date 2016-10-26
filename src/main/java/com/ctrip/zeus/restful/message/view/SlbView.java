package com.ctrip.zeus.restful.message.view;

import com.ctrip.zeus.model.entity.SlbServer;
import com.ctrip.zeus.model.entity.Vip;
import com.fasterxml.jackson.annotation.JsonView;

import java.util.Date;
import java.util.List;

/**
 * Created by zhoumy on 2016/7/26.
 */
public abstract class SlbView {

    @JsonView(ViewConstraints.Info.class)
    abstract Long getId();

    @JsonView(ViewConstraints.Info.class)
    abstract String getName();

    @JsonView(ViewConstraints.Normal.class)
    abstract String getNginxBin();

    @JsonView(ViewConstraints.Normal.class)
    abstract String getNginxConf();

    @JsonView(ViewConstraints.Normal.class)
    abstract Integer getNginxWorkerProcesses();

    @JsonView(ViewConstraints.Normal.class)
    abstract List<SlbServer> getSlbServers();

    @JsonView(ViewConstraints.Normal.class)
    abstract String getStatus();

    @JsonView(ViewConstraints.Normal.class)
    abstract Integer getVersion();

    @JsonView(ViewConstraints.Normal.class)
    abstract List<Vip> getVips();

    @JsonView(ViewConstraints.Normal.class)
    abstract Date getCreatedTime();
}
