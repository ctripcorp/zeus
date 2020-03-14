package com.ctrip.zeus.flow.splitvs;

import com.ctrip.zeus.flow.splitvs.model.SplitVsFlowEntity;

import java.util.List;

public interface SplitVsFlowService {
    SplitVsFlowEntity add(SplitVsFlowEntity entity) throws Exception;

    SplitVsFlowEntity update(SplitVsFlowEntity entity, boolean force) throws Exception;

    SplitVsFlowEntity updateStep(Long id, String step) throws Exception;

    SplitVsFlowEntity get(Long id) throws Exception;

    List<SplitVsFlowEntity> queryAll() throws Exception;

    SplitVsFlowEntity createAndBindNewVs(Long id) throws Exception;

    SplitVsFlowEntity splitVs(Long id) throws Exception;

    SplitVsFlowEntity rollback(Long id) throws Exception;
    SplitVsFlowEntity disable(Long id) throws Exception;

    SplitVsFlowEntity delete(Long id) throws Exception;
}
