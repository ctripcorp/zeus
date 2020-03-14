package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.SlbSlbStatusR;
import com.ctrip.zeus.dao.entity.SlbSlbStatusRExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SlbSlbStatusRMapper {
    long countByExample(SlbSlbStatusRExample example);

    int deleteByExample(SlbSlbStatusRExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SlbSlbStatusR record);

    int insertSelective(SlbSlbStatusR record);

    SlbSlbStatusR selectOneByExample(SlbSlbStatusRExample example);

    SlbSlbStatusR selectOneByExampleSelective(@Param("example") SlbSlbStatusRExample example, @Param("selective") SlbSlbStatusR.Column ... selective);

    List<SlbSlbStatusR> selectByExampleSelective(@Param("example") SlbSlbStatusRExample example, @Param("selective") SlbSlbStatusR.Column ... selective);

    List<SlbSlbStatusR> selectByExample(SlbSlbStatusRExample example);

    SlbSlbStatusR selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") SlbSlbStatusR.Column ... selective);

    SlbSlbStatusR selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SlbSlbStatusR record, @Param("example") SlbSlbStatusRExample example);

    int updateByExample(@Param("record") SlbSlbStatusR record, @Param("example") SlbSlbStatusRExample example);

    int updateByPrimaryKeySelective(SlbSlbStatusR record);

    int updateByPrimaryKey(SlbSlbStatusR record);

    int upsert(SlbSlbStatusR record);

    int upsertSelective(SlbSlbStatusR record);


    /*Self Defined*/
    int batchUpdateSlbOnlineVersion(List<SlbSlbStatusR> list);

    int insertOrUpdate(SlbSlbStatusR record);

    int batchInsertIncludeId(List<SlbSlbStatusR> records);
    /*Self Defined*/
}