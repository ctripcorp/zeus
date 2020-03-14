package com.ctrip.zeus.restful.message.view;

import com.fasterxml.jackson.annotation.JsonView;

import java.util.Date;
import java.util.List;

/**
 * @Discription
 **/
public abstract class CertificateView {

    @JsonView(ViewConstraints.Info.class)
    abstract Long getId();

    @JsonView(ViewConstraints.Info.class)
    abstract Date getIssueTime();

    @JsonView(ViewConstraints.Info.class)
    abstract Date getExpireTime();

    @JsonView(ViewConstraints.Normal.class)
    abstract String getCert();

    @JsonView(ViewConstraints.Normal.class)
    abstract String getKey();

    @JsonView(ViewConstraints.Detail.class)
    abstract List<Long> getVsIds();

    @JsonView(ViewConstraints.Detail.class)
    abstract List<String> getSlbServers();

    @JsonView(ViewConstraints.Info.class)
    abstract void setId(Long id);

    @JsonView(ViewConstraints.Info.class)
    abstract void setIssueTime(Date time);

    @JsonView(ViewConstraints.Info.class)
    abstract void setExpireTime(Date time);

    @JsonView(ViewConstraints.Normal.class)
    abstract void setCert(String cert);

    @JsonView(ViewConstraints.Normal.class)
    abstract void setKey(String key);

    @JsonView(ViewConstraints.Detail.class)
    abstract void setVsIds(List<Long> vsIds);

    @JsonView(ViewConstraints.Detail.class)
    abstract void setSlbServers(List<String> servers);

    @JsonView(ViewConstraints.Detail.class)
    abstract String getDomain();

    @JsonView(ViewConstraints.Detail.class)
    abstract void setDomain(String domain);

    @JsonView(ViewConstraints.Detail.class)
    abstract String getCid();

    @JsonView(ViewConstraints.Detail.class)
    abstract void setCid(String cid);
}
