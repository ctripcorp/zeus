package com.ctrip.zeus.restful.message.view;

import com.ctrip.zeus.model.model.Domain;
import com.ctrip.zeus.model.model.Rule;
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
    abstract List<Long> getSlbIds();

    @JsonView(ViewConstraints.Normal.class)
    abstract Boolean getSsl();

    @JsonView(ViewConstraints.Normal.class)
    abstract Integer getVersion();

    @JsonView(ViewConstraints.Normal.class)
    abstract List<Domain> getDomains();

    @JsonView(ViewConstraints.Normal.class)
    abstract List<Rule> getRuleSet();

    @JsonView(ViewConstraints.Normal.class)
    abstract Date getCreatedTime();


    @JsonView(ViewConstraints.Info.class)
    abstract void setId(Long id);

    @JsonView(ViewConstraints.Info.class)
    abstract void setName(String name);

    @JsonView(ViewConstraints.Normal.class)
    abstract void setPort(String port);

    @JsonView(ViewConstraints.Normal.class)
    abstract void setSlbIds(List<Long> slbIds);

    @JsonView(ViewConstraints.Normal.class)
    abstract void setSsl(Boolean ssl);

    @JsonView(ViewConstraints.Normal.class)
    abstract void setVersion(Integer v);

    @JsonView(ViewConstraints.Normal.class)
    abstract void setDomains(List<Domain> domains);

    @JsonView(ViewConstraints.Normal.class)
    abstract void setRuleSet(List<Rule> ruleSet);

    @JsonView(ViewConstraints.Normal.class)
    abstract void setCreatedTime(Date date);
}
