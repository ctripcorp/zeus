package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.SlbGroupGsR;
import com.ctrip.zeus.dao.entity.SlbGroupGsRExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SlbGroupGsRMapper {
    long countByExample(SlbGroupGsRExample example);

    int deleteByExample(SlbGroupGsRExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SlbGroupGsR record);

    int insertSelective(SlbGroupGsR record);

    SlbGroupGsR selectOneByExample(SlbGroupGsRExample example);

    SlbGroupGsR selectOneByExampleSelective(@Param("example") SlbGroupGsRExample example, @Param("selective") SlbGroupGsR.Column ... selective);

    List<SlbGroupGsR> selectByExampleSelective(@Param("example") SlbGroupGsRExample example, @Param("selective") SlbGroupGsR.Column ... selective);

    List<SlbGroupGsR> selectByExample(SlbGroupGsRExample example);

    SlbGroupGsR selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") SlbGroupGsR.Column ... selective);

    SlbGroupGsR selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SlbGroupGsR record, @Param("example") SlbGroupGsRExample example);

    int updateByExample(@Param("record") SlbGroupGsR record, @Param("example") SlbGroupGsRExample example);

    int updateByPrimaryKeySelective(SlbGroupGsR record);

    int updateByPrimaryKey(SlbGroupGsR record);

    int upsert(SlbGroupGsR record);

    int upsertSelective(SlbGroupGsR record);


    /*Self defined*/
    int batchUpdate(List<SlbGroupGsR> records);
    int batchInsert(List<SlbGroupGsR> records);
    int batchDelete(List<SlbGroupGsR> records);
    int batchInsertIncludeId(List<SlbGroupGsR> records);

    List<SlbGroupGsR> concatSelect(@Param("concats") String[] concats);
    /*Self defined*/
}