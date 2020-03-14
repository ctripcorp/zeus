package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.SlbVsStatusR;
import com.ctrip.zeus.dao.entity.SlbVsStatusRExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SlbVsStatusRMapper {
    long countByExample(SlbVsStatusRExample example);

    int deleteByExample(SlbVsStatusRExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SlbVsStatusR record);

    int insertSelective(SlbVsStatusR record);

    SlbVsStatusR selectOneByExample(SlbVsStatusRExample example);

    SlbVsStatusR selectOneByExampleSelective(@Param("example") SlbVsStatusRExample example, @Param("selective") SlbVsStatusR.Column ... selective);

    List<SlbVsStatusR> selectByExampleSelective(@Param("example") SlbVsStatusRExample example, @Param("selective") SlbVsStatusR.Column ... selective);

    List<SlbVsStatusR> selectByExample(SlbVsStatusRExample example);

    SlbVsStatusR selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") SlbVsStatusR.Column ... selective);

    SlbVsStatusR selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SlbVsStatusR record, @Param("example") SlbVsStatusRExample example);

    int updateByExample(@Param("record") SlbVsStatusR record, @Param("example") SlbVsStatusRExample example);

    int updateByPrimaryKeySelective(SlbVsStatusR record);

    int updateByPrimaryKey(SlbVsStatusR record);

    int upsert(SlbVsStatusR record);

    int upsertSelective(SlbVsStatusR record);

    /*Self generated*/
    int upsertOfflineVersion(SlbVsStatusR record);

    int batchInsertIncludeId(List<SlbVsStatusR> records);

    int batchUpdateByVsId(List<SlbVsStatusR> record);
    /*Self generated*/
}