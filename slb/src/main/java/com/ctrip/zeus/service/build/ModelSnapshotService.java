package com.ctrip.zeus.service.build;

import com.ctrip.zeus.service.model.snapshot.ModelSnapshotEntity;

import java.util.List;

public interface ModelSnapshotService {

    void add(ModelSnapshotEntity entity) throws Exception;

    void rollBack(Long slbId, long version) throws Exception;

    ModelSnapshotEntity get(Long slbId, Long version) throws Exception;

    List<ModelSnapshotEntity> get(Long slbId, Long[] versions) throws Exception;

    List<ModelSnapshotEntity> get(Long slbId, Long fromVersion, Long toVersion) throws Exception;

    ModelSnapshotEntity getCanary(Long slbId, Long version) throws Exception;

    ModelSnapshotEntity merge(List<ModelSnapshotEntity> snapshots) throws Exception;

    Long findLatestFullVersionBefore(Long slbId, Long slbVersion);

    void replaceWithCanaryGroups(ModelSnapshotEntity original);
}
