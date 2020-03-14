package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.SlbApp;
import com.ctrip.zeus.dao.entity.SlbAppExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SlbAppMapper {
    long countByExample(SlbAppExample example);

    int deleteByExample(SlbAppExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SlbApp record);

    int insertSelective(SlbApp record);

    SlbApp selectOneByExample(SlbAppExample example);

    SlbApp selectOneByExampleSelective(@Param("example") SlbAppExample example, @Param("selective") SlbApp.Column... selective);

    SlbApp selectOneByExampleWithBLOBs(SlbAppExample example);

    List<SlbApp> selectByExampleSelective(@Param("example") SlbAppExample example, @Param("selective") SlbApp.Column... selective);

    List<SlbApp> selectByExampleWithBLOBs(SlbAppExample example);

    List<SlbApp> selectByExample(SlbAppExample example);

    SlbApp selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") SlbApp.Column... selective);

    SlbApp selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SlbApp record, @Param("example") SlbAppExample example);

    int updateByExampleWithBLOBs(@Param("record") SlbApp record, @Param("example") SlbAppExample example);

    int updateByExample(@Param("record") SlbApp record, @Param("example") SlbAppExample example);

    int updateByPrimaryKeySelective(SlbApp record);

    int updateByPrimaryKeyWithBLOBs(SlbApp record);

    int updateByPrimaryKey(SlbApp record);

    int upsert(SlbApp record);

    int upsertSelective(SlbApp record);

    int upsertWithBLOBs(SlbApp record);

    /*Self Defined*/
    int batchUpsertWithBLOBs(List<SlbApp> record);

    int insertOrUpdateByAppId(SlbApp record);

    int batchInsertIncludeId(List<SlbApp> record);
    /* Batch upsert end */

}