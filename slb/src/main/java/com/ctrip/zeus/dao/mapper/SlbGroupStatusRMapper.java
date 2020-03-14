package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.SlbGroupStatusR;
import com.ctrip.zeus.dao.entity.SlbGroupStatusRExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SlbGroupStatusRMapper {
    long countByExample(SlbGroupStatusRExample example);

    int deleteByExample(SlbGroupStatusRExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SlbGroupStatusR record);

    int insertSelective(SlbGroupStatusR record);

    SlbGroupStatusR selectOneByExample(SlbGroupStatusRExample example);

    SlbGroupStatusR selectOneByExampleSelective(@Param("example") SlbGroupStatusRExample example, @Param("selective") SlbGroupStatusR.Column ... selective);

    List<SlbGroupStatusR> selectByExampleSelective(@Param("example") SlbGroupStatusRExample example, @Param("selective") SlbGroupStatusR.Column ... selective);

    List<SlbGroupStatusR> selectByExample(SlbGroupStatusRExample example);

    SlbGroupStatusR selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") SlbGroupStatusR.Column ... selective);

    SlbGroupStatusR selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SlbGroupStatusR record, @Param("example") SlbGroupStatusRExample example);

    int updateByExample(@Param("record") SlbGroupStatusR record, @Param("example") SlbGroupStatusRExample example);

    int updateByPrimaryKeySelective(SlbGroupStatusR record);

    int updateByPrimaryKey(SlbGroupStatusR record);

    int upsert(SlbGroupStatusR record);

    int upsertSelective(SlbGroupStatusR record);

    /*Self Defined*/
    int updateOnlineVersionByGroup(List<SlbGroupStatusR> records);

    int insertOrUpdate(SlbGroupStatusR record);

    int batchInsertIncludeId(List<SlbGroupStatusR> records);
    /*Self Defined*/
}