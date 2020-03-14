package com.ctrip.zeus.flow.splitvs.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SplitVsFlowEntity {

    Long id;
    String name;
    String status;
    Date createTime;

    Long sourceVsId;
    List<List<String>> domainGroups = new ArrayList<>();
    List<Long> newVsIds = new ArrayList<>();

    SplitVsFlowStepEntity created;
    SplitVsFlowStepEntity createAndBindNewVs;
    SplitVsFlowStepEntity splitVs;
    SplitVsFlowStepEntity rollback;

    public SplitVsFlowStepEntity getRollback() {
        return rollback;
    }

    public SplitVsFlowEntity setRollback(SplitVsFlowStepEntity rollback) {
        this.rollback = rollback;
        return this;
    }

    public Long getId() {
        return id;
    }

    public SplitVsFlowEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public SplitVsFlowEntity setName(String name) {
        this.name = name;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public SplitVsFlowEntity setStatus(String status) {
        this.status = status;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public SplitVsFlowEntity setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public Long getSourceVsId() {
        return sourceVsId;
    }

    public SplitVsFlowEntity setSourceVsId(Long sourceVsId) {
        this.sourceVsId = sourceVsId;
        return this;
    }

    public List<List<String>> getDomainGroups() {
        return domainGroups;
    }

    public SplitVsFlowEntity setDomainGroups(List<List<String>> domainGroups) {
        this.domainGroups = domainGroups;
        return this;
    }

    public List<Long> getNewVsIds() {
        return newVsIds;
    }

    public SplitVsFlowEntity setNewVsIds(List<Long> newVsIds) {
        this.newVsIds = newVsIds;
        return this;
    }

    public SplitVsFlowStepEntity getCreated() {
        return created;
    }

    public SplitVsFlowEntity setCreated(SplitVsFlowStepEntity created) {
        this.created = created;
        return this;
    }

    public SplitVsFlowStepEntity getCreateAndBindNewVs() {
        return createAndBindNewVs;
    }

    public SplitVsFlowEntity setCreateAndBindNewVs(SplitVsFlowStepEntity createAndBindNewVs) {
        this.createAndBindNewVs = createAndBindNewVs;
        return this;
    }

    public SplitVsFlowStepEntity getSplitVs() {
        return splitVs;
    }

    public SplitVsFlowEntity setSplitVs(SplitVsFlowStepEntity splitVs) {
        this.splitVs = splitVs;
        return this;
    }
}
