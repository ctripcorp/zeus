package com.ctrip.zeus.service.build.impl;

import com.ctrip.zeus.dao.entity.NginxModelSnapshot;
import com.ctrip.zeus.dao.entity.NginxModelSnapshotExample;
import com.ctrip.zeus.dao.mapper.NginxModelSnapshotMapper;
import com.ctrip.zeus.exceptions.NotFoundException;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.commit.Commit;
import com.ctrip.zeus.restful.message.view.ExtendedView;
import com.ctrip.zeus.service.build.ModelSnapshotService;
import com.ctrip.zeus.service.build.util.NginxModelSnapshotType;
import com.ctrip.zeus.service.commit.CommitMergeService;
import com.ctrip.zeus.service.commit.util.CommitType;
import com.ctrip.zeus.service.model.snapshot.ModelEntities;
import com.ctrip.zeus.service.model.snapshot.ModelSnapshotEntity;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.ctrip.zeus.support.ObjectJsonWriter;
import com.ctrip.zeus.util.AssertUtils;
import com.ctrip.zeus.util.CompressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service("ModelSnapshotService")
public class ModelSnapshotServiceImpl implements ModelSnapshotService {

    @Resource
    private NginxModelSnapshotMapper nginxModelSnapshotMapper;
    @Resource
    private CommitMergeService commitMergeService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void add(ModelSnapshotEntity entity) throws Exception {
        AssertUtils.assertNotNull(entity, "Entity is null.");
        AssertUtils.assertNotNull(entity.getTargetSlbId(), "Entity TargetSLbId is Null.");
        AssertUtils.assertNotNull(entity.getVersion(), "Entity version is Null.");
        nginxModelSnapshotMapper.insert(NginxModelSnapshot.builder()
                .slbId(entity.getTargetSlbId())
                .version(entity.getVersion())
                .snapshotType(entity.isFullUpdate() ? NginxModelSnapshotType.FULL : NginxModelSnapshotType.INCREMENTAL)
                .content(CompressUtils.compress(ObjectJsonWriter.write(entity))).build());
    }

    @Override
    public void rollBack(Long slbId, long version) throws Exception {
        AssertUtils.assertNotNull(slbId, "Slb Id is Null.");
        nginxModelSnapshotMapper.deleteByExample(
                NginxModelSnapshotExample.newAndCreateCriteria()
                        .andSlbIdEqualTo(slbId).andVersionGreaterThan(version).example());
    }

    @Override
    public ModelSnapshotEntity get(Long slbId, Long version) throws Exception {
        AssertUtils.assertNotNull(slbId, "Slb Id is Null.");
        NginxModelSnapshot snapshot = null;
        if (version != null) {
            snapshot = nginxModelSnapshotMapper.selectOneByExampleWithBLOBs(
                    NginxModelSnapshotExample.newAndCreateCriteria().andSlbIdEqualTo(slbId).andVersionEqualTo(version).example());
        } else {
            snapshot = nginxModelSnapshotMapper.selectOneByExampleWithBLOBs(
                    NginxModelSnapshotExample.newAndCreateCriteria().andSlbIdEqualTo(slbId).example().orderBy("version desc").limit(1));

        }
        if (snapshot == null || snapshot.getContent() == null) {
            throw new NotFoundException("Not Found Snapshot By SlbId:" + slbId + ";Version:" + version);
        }
        return ObjectJsonParser.parse(CompressUtils.decompress(snapshot.getContent()), ModelSnapshotEntity.class);
    }

    @Override
    public List<ModelSnapshotEntity> get(Long slbId, Long[] versions) throws Exception {
        AssertUtils.assertNotNull(slbId, "Slb Id is Null.");
        AssertUtils.assertNotNull(versions, "versions is Null.");
        List<ModelSnapshotEntity> result = new ArrayList<>();
        if (versions.length == 0) {
            throw new ValidationException("Versions is empty");
        }
        List<NginxModelSnapshot> snapshots = nginxModelSnapshotMapper.selectByExampleWithBLOBs(
                NginxModelSnapshotExample.newAndCreateCriteria().andSlbIdEqualTo(slbId).andVersionIn(Arrays.asList(versions)).example());
        if (snapshots == null) {
            return result;
        }
        snapshots.forEach(e -> {
            if (e != null && e.getContent() != null) {
                try {
                    ModelSnapshotEntity x = ObjectJsonParser.parse(CompressUtils.decompress(e.getContent()), ModelSnapshotEntity.class);
                    if (x != null) {
                        result.add(x);
                    }
                } catch (Exception x) {
                    logger.warn("Parser Content Failed.");
                }
            }
        });

        return result;
    }

    @Override
    public List<ModelSnapshotEntity> get(Long slbId, Long fromVersion, Long toVersion) throws Exception {
        AssertUtils.assertNotNull(slbId, "Slb Id is Null.");
        AssertUtils.assertNotNull(fromVersion, "fromVersion is Null.");
        AssertUtils.assertNotNull(toVersion, "toVersion is Null.");
        List<ModelSnapshotEntity> result = new ArrayList<>();

        List<NginxModelSnapshot> snapshots;
        if (fromVersion.equals(toVersion)) {
            snapshots = nginxModelSnapshotMapper.selectByExampleWithBLOBs(
                    NginxModelSnapshotExample.newAndCreateCriteria().andSlbIdEqualTo(slbId).andVersionEqualTo(toVersion).example());
        } else {
            snapshots = nginxModelSnapshotMapper.selectByExampleWithBLOBs(
                    NginxModelSnapshotExample.newAndCreateCriteria().andSlbIdEqualTo(slbId).andVersionLessThanOrEqualTo(toVersion)
                            .andVersionGreaterThan(fromVersion).example());
        }
        if (snapshots == null) {
            return result;
        }
        snapshots.forEach(e -> {
            if (e != null && e.getContent() != null) {
                try {
                    ModelSnapshotEntity x = ObjectJsonParser.parse(CompressUtils.decompress(e.getContent()), ModelSnapshotEntity.class);
                    if (x != null) {
                        result.add(x);
                    }
                } catch (Exception x) {
                    logger.warn("Parser Content Failed.");
                }
            }
        });
        return result;
    }

    @Override
    public ModelSnapshotEntity getCanary(Long slbId, Long version) throws Exception {
        ModelSnapshotEntity res = get(slbId, version);
        if (res.getCanaryGroups() == null || res.getCanaryGroups().isEmpty()) {
            return res;
        }
        replaceWithCanaryGroups(res);
        return res;
    }

    @Override
    public void replaceWithCanaryGroups(ModelSnapshotEntity original) {
        for (List<ExtendedView.ExtendedGroup> groupList : original.getModels().getGroupReferrerOfVses().values()) {
            for (int i = 0; i < groupList.size(); i++) {
                if (original.getCanaryGroups().containsKey(groupList.get(i).getId())) {
                    groupList.set(i, original.getCanaryGroups().get(groupList.get(i).getId()));
                }
            }
        }
    }

    /**
     * All snapshots to be merged MUST have same targetSlbId
     * @param snapshots
     * @return
     */
    @Override
    public ModelSnapshotEntity merge(List<ModelSnapshotEntity> snapshots) throws Exception {
        if (snapshots == null || snapshots.size() == 0) {
            return null;
        }
        ModelSnapshotEntity result = new ModelSnapshotEntity();
        result.setFullUpdate(false);

        Long targetSlbId = null;
        List<Commit> commits = new ArrayList<>(snapshots.size());
        for (ModelSnapshotEntity snapshot: snapshots) {
            assert snapshot.getTargetSlbId() != null;
            if (targetSlbId == null) {
                targetSlbId = snapshot.getTargetSlbId();
                result.setTargetSlbId(targetSlbId);
            } else if (!targetSlbId.equals(snapshot.getTargetSlbId())) {
                throw new Exception("All snapshots to be merged don't share same targetSlbId");
            }

            if (!result.isFullUpdate() && snapshot.isFullUpdate()) {
                result.setFullUpdate(true);
            }

            commits.add(snapshot.getCommits());
        }
        result.setCommits(commitMergeService.mergeCommit(commits));
        assert result.getCommits() != null;
        snapshots.sort(Comparator.comparingLong(ModelSnapshotEntity::getVersion).reversed());// todo handle null situation

        ModelSnapshotEntity latestSnapshot = snapshots.get(0);
        result.setVersion(latestSnapshot.getVersion());
        result.setAllUpGroupServers(latestSnapshot.getAllUpGroupServers());
        result.setAllDownServers(latestSnapshot.getAllDownServers());

        Map<Long, ExtendedView.ExtendedGroup> canaryGroups = result.getCanaryGroups();
        for (ModelSnapshotEntity snapshot: snapshots) {
            for (Long groupId: snapshot.getCanaryGroups().keySet()) {
                canaryGroups.putIfAbsent(groupId, snapshot.getCanaryGroups().get(groupId));
            }
        }

        List<ModelEntities> modelEntitiesList = new ArrayList<>(snapshots.size());
        snapshots.forEach(snapshot -> modelEntitiesList.add(snapshot.getModels()));
        result.setModels(mergeModelEntities(modelEntitiesList, result.getCommits()));
        return result;
    }

    /**
     * Prerequisite: the list should be sorted in order of version desc
     * @param list
     * @return
     */
    private ModelEntities mergeModelEntities(List<ModelEntities> list, Commit mergedCommit) {
        assert mergedCommit != null;
        if (list == null || list.size() == 0) {
            return null;
        }

        ModelEntities result = new ModelEntities();

        result.getIncrementalVses().addAll(mergedCommit.getVsIds());
        result.getRemoveVsIds().addAll(mergedCommit.getCleanvsIds());

        ModelEntities latest = list.get(0);
        // get latest version for all non-incremental data
        result.setSlbs(latest.getSlbs());
        result.setDefaultRules(latest.getDefaultRules());
        result.setVsIdSourceGroupIdTargetSlbIdWeightMap(latest.getVsIdSourceGroupIdTargetSlbIdWeightMap());
        result.setGroupIdDrMap(latest.getGroupIdDrMap());
        result.setAllNxOnlineVsIds(latest.getAllNxOnlineVsIds());

        // Since groupReferees, policyReferees and VSes all could be full or incremental,
        // thus all should be added if fullUpdate is true
        for (ModelEntities modelEntities: list) {
            for (Long vsId: modelEntities.getGroupReferrerOfVses().keySet()) {
                if (CommitType.COMMIT_TYPE_FULL_UPDATE.equalsIgnoreCase(mergedCommit.getType())
                        || mergedCommit.getVsIds().contains(vsId)) {
                    result.getGroupReferrerOfVses().putIfAbsent(vsId, modelEntities.getGroupReferrerOfVses().get(vsId));
                }
            }
            for (Long vsId: modelEntities.getPolicyReferrerOfVses().keySet()) {
                if (CommitType.COMMIT_TYPE_FULL_UPDATE.equalsIgnoreCase(mergedCommit.getType())
                        || mergedCommit.getVsIds().contains(vsId)) {
                    result.getPolicyReferrerOfVses().putIfAbsent(vsId, modelEntities.getPolicyReferrerOfVses().get(vsId));
                }
            }
            for (Long vsId: modelEntities.getVses().keySet()) {
                if (CommitType.COMMIT_TYPE_FULL_UPDATE.equalsIgnoreCase(mergedCommit.getType())
                        || mergedCommit.getVsIds().contains(vsId)) {
                    result.getVses().putIfAbsent(vsId, modelEntities.getVses().get(vsId));
                }
            }
        }

        return result;
    }

    @Override
    public Long findLatestFullVersionBefore(Long slbId, Long version) {
        if (slbId == null) {
            return null;
        }
        if (version == null) {
            // set version to latest version if it's null
            NginxModelSnapshotExample example = NginxModelSnapshotExample.newAndCreateCriteria()
                    .andSlbIdEqualTo(slbId)
                    .example()
                    .orderBy("version desc");
            NginxModelSnapshot latest = nginxModelSnapshotMapper.selectOneByExample(example);
            if (latest == null) {
                return null;
            }
            version = latest.getVersion();
        }

        NginxModelSnapshotExample example = NginxModelSnapshotExample.newAndCreateCriteria()
                .andSnapshotTypeNotEqualTo(NginxModelSnapshotType.INCREMENTAL)// snapshot whose type is NULL is a full-version snapshot
                .andVersionLessThanOrEqualTo(version)
                .andSlbIdEqualTo(slbId)
                .example()
                .orderBy("version desc");
        NginxModelSnapshot result = nginxModelSnapshotMapper.selectOneByExample(example);
        return result == null ? null : result.getVersion();
    }
}
