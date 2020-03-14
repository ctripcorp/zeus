package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.FeedbackCommit;
import com.ctrip.zeus.dao.entity.FeedbackCommitExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FeedbackCommitMapper {
    long countByExample(FeedbackCommitExample example);

    int deleteByExample(FeedbackCommitExample example);

    int deleteByPrimaryKey(Long id);

    int insert(FeedbackCommit record);

    int insertSelective(FeedbackCommit record);

    FeedbackCommit selectOneByExample(FeedbackCommitExample example);

    FeedbackCommit selectOneByExampleSelective(@Param("example") FeedbackCommitExample example, @Param("selective") FeedbackCommit.Column ... selective);

    List<FeedbackCommit> selectByExampleSelective(@Param("example") FeedbackCommitExample example, @Param("selective") FeedbackCommit.Column ... selective);

    List<FeedbackCommit> selectByExample(FeedbackCommitExample example);

    FeedbackCommit selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") FeedbackCommit.Column ... selective);

    FeedbackCommit selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") FeedbackCommit record, @Param("example") FeedbackCommitExample example);

    int updateByExample(@Param("record") FeedbackCommit record, @Param("example") FeedbackCommitExample example);

    int updateByPrimaryKeySelective(FeedbackCommit record);

    int updateByPrimaryKey(FeedbackCommit record);

    int upsert(FeedbackCommit record);

    int upsertSelective(FeedbackCommit record);

    /*Self Defined*/
    int batchInsertIncludeId(List<FeedbackCommit> feedbackCommits);
    /*Self Defined*/
}