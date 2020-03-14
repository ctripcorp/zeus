package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.SlbDr;
import com.ctrip.zeus.dao.entity.SlbDrExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SlbDrMapper {
    long countByExample(SlbDrExample example);

    int deleteByExample(SlbDrExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SlbDr record);

    int insertSelective(SlbDr record);

    SlbDr selectOneByExample(SlbDrExample example);

    SlbDr selectOneByExampleSelective(@Param("example") SlbDrExample example, @Param("selective") SlbDr.Column ... selective);

    List<SlbDr> selectByExampleSelective(@Param("example") SlbDrExample example, @Param("selective") SlbDr.Column ... selective);

    List<SlbDr> selectByExample(SlbDrExample example);

    SlbDr selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") SlbDr.Column ... selective);

    SlbDr selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SlbDr record, @Param("example") SlbDrExample example);

    int updateByExample(@Param("record") SlbDr record, @Param("example") SlbDrExample example);

    int updateByPrimaryKeySelective(SlbDr record);

    int updateByPrimaryKey(SlbDr record);

    int upsert(SlbDr record);

    int upsertSelective(SlbDr record);

    /*Self Defined*/
    int insertIncludeId(SlbDr record);

    int batchInsertIncludeId(List<SlbDr> records);
    /*Self Defined*/
}