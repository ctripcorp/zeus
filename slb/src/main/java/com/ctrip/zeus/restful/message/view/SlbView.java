package com.ctrip.zeus.restful.message.view;

import com.ctrip.zeus.model.model.Rule;
import com.ctrip.zeus.model.model.SlbServer;
import com.ctrip.zeus.model.model.Vip;
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
    abstract List<Rule> getRuleSet();

    @JsonView(ViewConstraints.Normal.class)
    abstract Date getCreatedTime();



    @JsonView(ViewConstraints.Info.class)
    abstract void setId(Long id);

    @JsonView(ViewConstraints.Info.class)
    abstract void setName(String name);

    @JsonView(ViewConstraints.Normal.class)
    abstract void setNginxBin(String bin);

    @JsonView(ViewConstraints.Normal.class)
    abstract void setNginxConf(String conf);

    @JsonView(ViewConstraints.Normal.class)
    abstract void setNginxWorkerProcesses(Integer w);

    @JsonView(ViewConstraints.Normal.class)
    abstract void setSlbServers(List<SlbServer> servers);

    @JsonView(ViewConstraints.Normal.class)
    abstract void setStatus(String  status);

    @JsonView(ViewConstraints.Normal.class)
    abstract void setVersion(Integer version);

    @JsonView(ViewConstraints.Normal.class)
    abstract void setVips( List<Vip> vips);

    @JsonView(ViewConstraints.Normal.class)
    abstract void setRuleSet(List<Rule> rules);

    @JsonView(ViewConstraints.Normal.class)
    abstract void setCreatedTime(Date time);
}
