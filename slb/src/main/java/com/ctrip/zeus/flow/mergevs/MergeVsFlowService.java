package com.ctrip.zeus.flow.mergevs;

import com.ctrip.zeus.flow.mergevs.model.MergeVsFlowEntity;

import java.util.List;

public interface MergeVsFlowService {
    MergeVsFlowEntity add(MergeVsFlowEntity entity) throws Exception;

    MergeVsFlowEntity update(MergeVsFlowEntity entity, boolean force) throws Exception;

    MergeVsFlowEntity updateStep(Long id, String step) throws Exception;

    MergeVsFlowEntity get(Long id) throws Exception;

    List<MergeVsFlowEntity> queryAll() throws Exception;

    MergeVsFlowEntity createAndBindNewVs(Long id) throws Exception;

    MergeVsFlowEntity mergeVs(Long id) throws Exception;

    MergeVsFlowEntity rollback(Long id) throws Exception;

    MergeVsFlowEntity disable(Long id) throws Exception;

    MergeVsFlowEntity delete(Long id) throws Exception;

    MergeVsFlowEntity clean(Long id) throws Exception;
}
