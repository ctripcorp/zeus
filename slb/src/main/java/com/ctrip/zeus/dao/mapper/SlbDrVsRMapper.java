package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.SlbDrVsR;
import com.ctrip.zeus.dao.entity.SlbDrVsRExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SlbDrVsRMapper {
    long countByExample(SlbDrVsRExample example);

    int deleteByExample(SlbDrVsRExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SlbDrVsR record);

    int insertSelective(SlbDrVsR record);

    SlbDrVsR selectOneByExample(SlbDrVsRExample example);

    SlbDrVsR selectOneByExampleSelective(@Param("example") SlbDrVsRExample example, @Param("selective") SlbDrVsR.Column ... selective);

    List<SlbDrVsR> selectByExampleSelective(@Param("example") SlbDrVsRExample example, @Param("selective") SlbDrVsR.Column ... selective);

    List<SlbDrVsR> selectByExample(SlbDrVsRExample example);

    SlbDrVsR selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") SlbDrVsR.Column ... selective);

    SlbDrVsR selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SlbDrVsR record, @Param("example") SlbDrVsRExample example);

    int updateByExample(@Param("record") SlbDrVsR record, @Param("example") SlbDrVsRExample example);

    int updateByPrimaryKeySelective(SlbDrVsR record);

    int updateByPrimaryKey(SlbDrVsR record);

    int upsert(SlbDrVsR record);

    int upsertSelective(SlbDrVsR record);

    /*Self Defined*/
    int batchInsert(List<SlbDrVsR> records);

    int batchInsertIncludeId(List<SlbDrVsR> records);

    List<SlbDrVsR> concatSelect(@Param("concats") String[] concats);

    /*Self Defined*/
}