package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.SlbVsSlbR;
import com.ctrip.zeus.dao.entity.SlbVsSlbRExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SlbVsSlbRMapper {
    long countByExample(SlbVsSlbRExample example);

    int deleteByExample(SlbVsSlbRExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SlbVsSlbR record);

    int insertSelective(SlbVsSlbR record);

    SlbVsSlbR selectOneByExample(SlbVsSlbRExample example);

    SlbVsSlbR selectOneByExampleSelective(@Param("example") SlbVsSlbRExample example, @Param("selective") SlbVsSlbR.Column ... selective);

    List<SlbVsSlbR> selectByExampleSelective(@Param("example") SlbVsSlbRExample example, @Param("selective") SlbVsSlbR.Column ... selective);

    List<SlbVsSlbR> selectByExample(SlbVsSlbRExample example);

    SlbVsSlbR selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") SlbVsSlbR.Column ... selective);

    SlbVsSlbR selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SlbVsSlbR record, @Param("example") SlbVsSlbRExample example);

    int updateByExample(@Param("record") SlbVsSlbR record, @Param("example") SlbVsSlbRExample example);

    int updateByPrimaryKeySelective(SlbVsSlbR record);

    int updateByPrimaryKey(SlbVsSlbR record);

    int upsert(SlbVsSlbR record);

    int upsertSelective(SlbVsSlbR record);

    /*Self Defined*/
    int batchInsert(List<SlbVsSlbR> records);

    int batchInsertIncludeId(List<SlbVsSlbR> records);

    int batchUpdate(List<SlbVsSlbR> records);

    int batchDelete(List<SlbVsSlbR> records);
    /*Self Defined*/
}