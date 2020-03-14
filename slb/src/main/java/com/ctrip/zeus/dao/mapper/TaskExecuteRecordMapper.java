package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.TaskExecuteRecord;
import com.ctrip.zeus.dao.entity.TaskExecuteRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaskExecuteRecordMapper {
    long countByExample(TaskExecuteRecordExample example);

    int deleteByExample(TaskExecuteRecordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TaskExecuteRecord record);

    int insertSelective(TaskExecuteRecord record);

    TaskExecuteRecord selectOneByExample(TaskExecuteRecordExample example);

    TaskExecuteRecord selectOneByExampleSelective(@Param("example") TaskExecuteRecordExample example, @Param("selective") TaskExecuteRecord.Column ... selective);

    List<TaskExecuteRecord> selectByExampleSelective(@Param("example") TaskExecuteRecordExample example, @Param("selective") TaskExecuteRecord.Column ... selective);

    List<TaskExecuteRecord> selectByExample(TaskExecuteRecordExample example);

    TaskExecuteRecord selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") TaskExecuteRecord.Column ... selective);

    TaskExecuteRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TaskExecuteRecord record, @Param("example") TaskExecuteRecordExample example);

    int updateByExample(@Param("record") TaskExecuteRecord record, @Param("example") TaskExecuteRecordExample example);

    int updateByPrimaryKeySelective(TaskExecuteRecord record);

    int updateByPrimaryKey(TaskExecuteRecord record);

    int upsert(TaskExecuteRecord record);

    int upsertSelective(TaskExecuteRecord record);
}