package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.SlbDrStatusR;
import com.ctrip.zeus.dao.entity.SlbDrStatusRExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SlbDrStatusRMapper {
    long countByExample(SlbDrStatusRExample example);

    int deleteByExample(SlbDrStatusRExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SlbDrStatusR record);

    int insertSelective(SlbDrStatusR record);

    SlbDrStatusR selectOneByExample(SlbDrStatusRExample example);

    SlbDrStatusR selectOneByExampleSelective(@Param("example") SlbDrStatusRExample example, @Param("selective") SlbDrStatusR.Column... selective);

    List<SlbDrStatusR> selectByExampleSelective(@Param("example") SlbDrStatusRExample example, @Param("selective") SlbDrStatusR.Column... selective);

    List<SlbDrStatusR> selectByExample(SlbDrStatusRExample example);

    SlbDrStatusR selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") SlbDrStatusR.Column... selective);

    SlbDrStatusR selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SlbDrStatusR record, @Param("example") SlbDrStatusRExample example);

    int updateByExample(@Param("record") SlbDrStatusR record, @Param("example") SlbDrStatusRExample example);

    int updateByPrimaryKeySelective(SlbDrStatusR record);

    int updateByPrimaryKey(SlbDrStatusR record);

    int upsert(SlbDrStatusR record);

    int upsertSelective(SlbDrStatusR record);

    /*Self Defined*/
    int updateOnlineVersionByDr(List<SlbDrStatusR> records);

    int batchInsertIncludeId(List<SlbDrStatusR> records);

    List<SlbDrStatusR> concatSelect(@Param("concats") String[] concats);
    /*Self Defined*/
}