package com.ctrip.zeus.service.model.snapshot;

import com.ctrip.zeus.model.commit.Commit;
import com.ctrip.zeus.restful.message.view.ExtendedView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ModelSnapshotEntity {
    private ModelEntities models = new ModelEntities();
    private Map<Long, ExtendedView.ExtendedGroup> canaryGroups = new HashMap<>();
    private Commit commits;
    private Set<String> allUpGroupServers = new HashSet<>();
    private Set<String> allDownServers = new HashSet<>();
    private boolean isFullUpdate = false;
    private Long targetSlbId;
    private Long version;

    public Long getVersion() {
        return version;
    }

    public ModelSnapshotEntity setVersion(Long version) {
        this.version = version;
        return this;
    }

    public Long getTargetSlbId() {
        return targetSlbId;
    }

    public ModelSnapshotEntity setTargetSlbId(Long targetSlbId) {
        this.targetSlbId = targetSlbId;
        return this;
    }

    public ModelEntities getModels() {
        return models;
    }

    public ModelSnapshotEntity setModels(ModelEntities models) {
        this.models = models;
        return this;
    }

    public Map<Long, ExtendedView.ExtendedGroup> getCanaryGroups() {
        return canaryGroups;
    }

    public ModelSnapshotEntity setCanaryGroups(Map<Long, ExtendedView.ExtendedGroup> canaryGroups) {
        this.canaryGroups = canaryGroups;
        return this;
    }

    public Commit getCommits() {
        return commits;
    }

    public ModelSnapshotEntity setCommits(Commit commits) {
        this.commits = commits;
        return this;
    }

    public Set<String> getAllUpGroupServers() {
        return allUpGroupServers;
    }

    public ModelSnapshotEntity setAllUpGroupServers(Set<String> allUpGroupServers) {
        this.allUpGroupServers = allUpGroupServers;
        return this;
    }

    public Set<String> getAllDownServers() {
        return allDownServers;
    }

    public ModelSnapshotEntity setAllDownServers(Set<String> allDownServers) {
        this.allDownServers = allDownServers;
        return this;
    }

    public boolean isFullUpdate() {
        return isFullUpdate;
    }

    public ModelSnapshotEntity setFullUpdate(boolean fullUpdate) {
        isFullUpdate = fullUpdate;
        return this;
    }
}
