package com.ctrip.zeus.service.task;

import com.ctrip.zeus.dao.entity.TaskExecuteRecord;

/**
 * @Discription
 **/
public interface TaskExecuteRecordService {

    TaskExecuteRecord findByPK(Long id) throws Exception;

    TaskExecuteRecord findByTaskKey(String key) throws Exception;

    int deleteByPK(TaskExecuteRecord record) throws Exception;

    int insert(TaskExecuteRecord record) throws Exception;

    int insertOrUpdate(TaskExecuteRecord record) throws Exception;

    void markExecution(String taskName) throws Exception;
}