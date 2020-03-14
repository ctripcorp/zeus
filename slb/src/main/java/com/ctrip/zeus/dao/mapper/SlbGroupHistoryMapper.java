package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.SlbGroupHistory;
import com.ctrip.zeus.dao.entity.SlbGroupHistoryExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SlbGroupHistoryMapper {
    long countByExample(SlbGroupHistoryExample example);

    int deleteByExample(SlbGroupHistoryExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SlbGroupHistory record);

    int insertSelective(SlbGroupHistory record);

    SlbGroupHistory selectOneByExample(SlbGroupHistoryExample example);

    SlbGroupHistory selectOneByExampleSelective(@Param("example") SlbGroupHistoryExample example, @Param("selective") SlbGroupHistory.Column ... selective);

    List<SlbGroupHistory> selectByExampleSelective(@Param("example") SlbGroupHistoryExample example, @Param("selective") SlbGroupHistory.Column ... selective);

    List<SlbGroupHistory> selectByExample(SlbGroupHistoryExample example);

    SlbGroupHistory selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") SlbGroupHistory.Column ... selective);

    SlbGroupHistory selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SlbGroupHistory record, @Param("example") SlbGroupHistoryExample example);

    int updateByExample(@Param("record") SlbGroupHistory record, @Param("example") SlbGroupHistoryExample example);

    int updateByPrimaryKeySelective(SlbGroupHistory record);

    int updateByPrimaryKey(SlbGroupHistory record);

    int upsert(SlbGroupHistory record);

    int upsertSelective(SlbGroupHistory record);

    /*Self Defined*/
    int batchInsertIncludeId(List<SlbGroupHistory> records);
    /*Self Defined*/
}