package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.SlbBuildCommit;
import com.ctrip.zeus.dao.entity.SlbBuildCommitExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SlbBuildCommitMapper {
    long countByExample(SlbBuildCommitExample example);

    int deleteByExample(SlbBuildCommitExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SlbBuildCommit record);

    int insertSelective(SlbBuildCommit record);

    SlbBuildCommit selectOneByExample(SlbBuildCommitExample example);

    SlbBuildCommit selectOneByExampleSelective(@Param("example") SlbBuildCommitExample example, @Param("selective") SlbBuildCommit.Column ... selective);

    List<SlbBuildCommit> selectByExampleSelective(@Param("example") SlbBuildCommitExample example, @Param("selective") SlbBuildCommit.Column ... selective);

    List<SlbBuildCommit> selectByExample(SlbBuildCommitExample example);

    SlbBuildCommit selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") SlbBuildCommit.Column ... selective);

    SlbBuildCommit selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SlbBuildCommit record, @Param("example") SlbBuildCommitExample example);

    int updateByExample(@Param("record") SlbBuildCommit record, @Param("example") SlbBuildCommitExample example);

    int updateByPrimaryKeySelective(SlbBuildCommit record);

    int updateByPrimaryKey(SlbBuildCommit record);

    int upsert(SlbBuildCommit record);

    int upsertSelective(SlbBuildCommit record);

    /*Self Defined*/
    int batchInsertIncludeId(List<SlbBuildCommit> records);

    int insertIncludeId(SlbBuildCommit record);
    /*Self Defined*/
}