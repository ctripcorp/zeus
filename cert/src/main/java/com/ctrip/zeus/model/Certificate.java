package com.ctrip.zeus.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @Discription
 **/
public class Certificate {
    private Long id;
    private Date expireTime;
    private Date issueTime;
    private String certData;
    private String keyData;
    private List<Long> vsIds = new ArrayList<>();
    private List<String> slbServers = new ArrayList<>();
    private String domain;
    private String cid;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public Date getIssueTime() {
        return issueTime;
    }

    public void setIssueTime(Date issueTime) {
        this.issueTime = issueTime;
    }

    public String getCertData() {
        return certData;
    }

    public void setCertData(String certData) {
        this.certData = certData;
    }

    public String getKeyData() {
        return keyData;
    }

    public void setKeyData(String keyData) {
        this.keyData = keyData;
    }

    public List<Long> getVsIds() {
        return vsIds;
    }

    public void setVsIds(List<Long> vsIds) {
        this.vsIds = vsIds;
    }

    public List<String> getSlbServers() {
        return slbServers;
    }

    public void setSlbServers(List<String> slbServers) {
        this.slbServers = slbServers;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Certificate)) return false;
        Certificate that = (Certificate) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getExpireTime(), that.getExpireTime()) &&
                Objects.equals(getIssueTime(), that.getIssueTime()) &&
                Objects.equals(getCertData(), that.getCertData()) &&
                Objects.equals(getKeyData(), that.getKeyData()) &&
                Objects.equals(getVsIds(), that.getVsIds()) &&
                Objects.equals(getSlbServers(), that.getSlbServers()) &&
                Objects.equals(getDomain(), that.getDomain()) &&
                Objects.equals(getCid(), that.getCid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getExpireTime(), getIssueTime(), getCertData(), getKeyData(), getVsIds(), getSlbServers(), getDomain(), getCid());
    }
}
