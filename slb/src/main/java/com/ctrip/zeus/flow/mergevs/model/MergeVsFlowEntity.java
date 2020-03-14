package com.ctrip.zeus.flow.mergevs.model;

import com.ctrip.zeus.model.model.Domain;

import java.util.*;

public class MergeVsFlowEntity {
    Long id;
    String name;
    String status;
    String cid;
    Map<Long, List<Domain>> domains = new HashMap<>();
    Date createTime;

    List<Long> sourceVsId = new ArrayList<>();
    Long newVsId;

    MergeVsFlowStepEntity created;
    MergeVsFlowStepEntity createAndBindNewVs;
    MergeVsFlowStepEntity mergeVs;
    MergeVsFlowStepEntity cleanVs;
    MergeVsFlowStepEntity rollback;


    public MergeVsFlowStepEntity getRollback() {
        return rollback;
    }

    public MergeVsFlowEntity setRollback(MergeVsFlowStepEntity rollback) {
        this.rollback = rollback;
        return this;
    }

    public Map<Long, List<Domain>> getDomains() {
        return domains;
    }

    public MergeVsFlowEntity addDomains(Long vsId, List<Domain> domains) {
        if (domains == null) return this;
        this.domains.put(vsId, domains);
        return this;
    }

    public String getCid() {
        return cid;
    }

    public MergeVsFlowEntity setCid(String cid) {
        this.cid = cid;
        return this;
    }

    public MergeVsFlowStepEntity getCleanVs() {
        return cleanVs;
    }

    public MergeVsFlowEntity setCleanVs(MergeVsFlowStepEntity cleanVs) {
        this.cleanVs = cleanVs;
        return this;
    }

    public MergeVsFlowStepEntity getMergeVs() {
        return mergeVs;
    }

    public MergeVsFlowEntity setMergeVs(MergeVsFlowStepEntity mergeVs) {
        this.mergeVs = mergeVs;
        return this;
    }

    public Long getId() {
        return id;
    }

    public MergeVsFlowEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public MergeVsFlowEntity setName(String name) {
        this.name = name;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public MergeVsFlowEntity setStatus(String status) {
        this.status = status;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public MergeVsFlowEntity setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public List<Long> getSourceVsId() {
        return sourceVsId;
    }

    public MergeVsFlowEntity setSourceVsId(List<Long> sourceVsId) {
        this.sourceVsId = sourceVsId;
        return this;
    }

    public Long getNewVsId() {
        return newVsId;
    }

    public MergeVsFlowEntity setNewVsId(Long newVsId) {
        this.newVsId = newVsId;
        return this;
    }

    public MergeVsFlowStepEntity getCreated() {
        return created;
    }

    public MergeVsFlowEntity setCreated(MergeVsFlowStepEntity created) {
        this.created = created;
        return this;
    }

    public MergeVsFlowStepEntity getCreateAndBindNewVs() {
        return createAndBindNewVs;
    }

    public MergeVsFlowEntity setCreateAndBindNewVs(MergeVsFlowStepEntity createAndBindNewVs) {
        this.createAndBindNewVs = createAndBindNewVs;
        return this;
    }


}
