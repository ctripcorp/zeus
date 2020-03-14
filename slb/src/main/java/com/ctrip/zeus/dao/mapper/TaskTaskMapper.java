package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.TaskTask;
import com.ctrip.zeus.dao.entity.TaskTaskExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaskTaskMapper {
    long countByExample(TaskTaskExample example);

    int deleteByExample(TaskTaskExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TaskTask record);

    int insertSelective(TaskTask record);

    TaskTask selectOneByExample(TaskTaskExample example);

    TaskTask selectOneByExampleSelective(@Param("example") TaskTaskExample example, @Param("selective") TaskTask.Column ... selective);

    TaskTask selectOneByExampleWithBLOBs(TaskTaskExample example);

    List<TaskTask> selectByExampleSelective(@Param("example") TaskTaskExample example, @Param("selective") TaskTask.Column ... selective);

    List<TaskTask> selectByExampleWithBLOBs(TaskTaskExample example);

    List<TaskTask> selectByExample(TaskTaskExample example);

    TaskTask selectByPrimaryKeySelective(@Param("id") Long id, @Param("selective") TaskTask.Column ... selective);

    TaskTask selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TaskTask record, @Param("example") TaskTaskExample example);

    int updateByExampleWithBLOBs(@Param("record") TaskTask record, @Param("example") TaskTaskExample example);

    int updateByExample(@Param("record") TaskTask record, @Param("example") TaskTaskExample example);

    int updateByPrimaryKeySelective(TaskTask record);

    int updateByPrimaryKeyWithBLOBs(TaskTask record);

    int updateByPrimaryKey(TaskTask record);

    int upsert(TaskTask record);

    int upsertSelective(TaskTask record);

    int upsertWithBLOBs(TaskTask record);

    /* methods added manually start */
    int insertIdIncluded(TaskTask record);
    /* methods added manually end */
}