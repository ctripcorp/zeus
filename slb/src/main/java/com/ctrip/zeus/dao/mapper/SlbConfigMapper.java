package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.SlbConfig;
import com.ctrip.zeus.dao.entity.SlbConfigExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SlbConfigMapper {
    long countByExample(SlbConfigExample example);

    int deleteByExample(SlbConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SlbConfig record);

    int insertSelective(SlbConfig record);

    SlbConfig selectOneByExample(SlbConfigExample example);

    SlbConfig selectOneByExampleSelective(@Param("example") SlbConfigExample example, @Param("selective") SlbConfig.Column... selective);

    List<SlbConfig> selectByExampleSelective(@Param("example") SlbConfigExample example, @Param("selective") SlbConfig.Column... selective);

    List<SlbConfig> selectByExample(SlbConfigExample example);

    SlbConfig selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") SlbConfig.Column... selective);

    SlbConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SlbConfig record, @Param("example") SlbConfigExample example);

    int updateByExample(@Param("record") SlbConfig record, @Param("example") SlbConfigExample example);

    int updateByPrimaryKeySelective(SlbConfig record);

    int updateByPrimaryKey(SlbConfig record);

    int upsert(SlbConfig record);

    int upsertSelective(SlbConfig record);

    /* added methods */
    int batchUpdate(List<SlbConfig> records);

    int batchInsert(List<SlbConfig> records);

    int batchUpsertValue(List<SlbConfig> records);
    /* added methods */
}