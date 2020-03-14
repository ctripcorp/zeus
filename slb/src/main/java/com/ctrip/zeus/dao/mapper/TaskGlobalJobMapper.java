package com.ctrip.zeus.dao.mapper;

import com.ctrip.zeus.dao.entity.TaskGlobalJob;
import com.ctrip.zeus.dao.entity.TaskGlobalJobExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaskGlobalJobMapper {
    long countByExample(TaskGlobalJobExample example);

    int deleteByExample(TaskGlobalJobExample example);

    int deleteByPrimaryKey(String jobKey);

    int insert(TaskGlobalJob record);

    int insertSelective(TaskGlobalJob record);

    TaskGlobalJob selectOneByExample(TaskGlobalJobExample example);

    TaskGlobalJob selectOneByExampleSelective(@Param("example") TaskGlobalJobExample example, @Param("selective") TaskGlobalJob.Column ... selective);

    List<TaskGlobalJob> selectByExampleSelective(@Param("example") TaskGlobalJobExample example, @Param("selective") TaskGlobalJob.Column ... selective);

    List<TaskGlobalJob> selectByExample(TaskGlobalJobExample example);

    TaskGlobalJob selectByPrimaryKeySelective(@Param("jobKey") String jobKey, @Param("selective") TaskGlobalJob.Column ... selective);

    TaskGlobalJob selectByPrimaryKey(String jobKey);

    int updateByExampleSelective(@Param("record") TaskGlobalJob record, @Param("example") TaskGlobalJobExample example);

    int updateByExample(@Param("record") TaskGlobalJob record, @Param("example") TaskGlobalJobExample example);

    int updateByPrimaryKeySelective(TaskGlobalJob record);

    int updateByPrimaryKey(TaskGlobalJob record);

    int upsert(TaskGlobalJob record);

    int upsertSelective(TaskGlobalJob record);
}