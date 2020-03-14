package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.SlbGroup;
import com.ctrip.zeus.dao.entity.SlbGroupExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SlbGroupMapper {
    long countByExample(SlbGroupExample example);

    int deleteByExample(SlbGroupExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SlbGroup record);

    int insertSelective(SlbGroup record);

    SlbGroup selectOneByExample(SlbGroupExample example);

    SlbGroup selectOneByExampleSelective(@Param("example") SlbGroupExample example, @Param("selective") SlbGroup.Column... selective);

    List<SlbGroup> selectByExampleSelective(@Param("example") SlbGroupExample example, @Param("selective") SlbGroup.Column... selective);

    List<SlbGroup> selectByExample(SlbGroupExample example);

    SlbGroup selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") SlbGroup.Column... selective);

    SlbGroup selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SlbGroup record, @Param("example") SlbGroupExample example);

    int updateByExample(@Param("record") SlbGroup record, @Param("example") SlbGroupExample example);

    int updateByPrimaryKeySelective(SlbGroup record);

    int updateByPrimaryKey(SlbGroup record);

    int upsert(SlbGroup record);

    int upsertSelective(SlbGroup record);

    /*Self Defined*/
    int insertIncludeId(SlbGroup record);

    int batchInsertIncludeId(List<SlbGroup> records);
    /*Self Defined*/
}