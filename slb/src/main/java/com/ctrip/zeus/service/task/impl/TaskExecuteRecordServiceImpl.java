package com.ctrip.zeus.service.task.impl;

import com.ctrip.zeus.dao.entity.TaskExecuteRecord;
import com.ctrip.zeus.dao.entity.TaskExecuteRecordExample;
import com.ctrip.zeus.dao.mapper.TaskExecuteRecordMapper;
import com.ctrip.zeus.service.task.TaskExecuteRecordService;
import com.ctrip.zeus.util.AssertUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @Discription
 **/
@Component("taskExecuteRecordService")
public class TaskExecuteRecordServiceImpl implements TaskExecuteRecordService {

    @Resource
    private TaskExecuteRecordMapper taskExecuteRecordMapper;

    @Override
    public TaskExecuteRecord findByPK(Long id) throws Exception {
        return taskExecuteRecordMapper.selectByPrimaryKey(id);
    }

    @Override
    public TaskExecuteRecord findByTaskKey(String key) throws Exception {
        TaskExecuteRecordExample example = new TaskExecuteRecordExample();
        example.or().andTaskKeyEqualTo(key);
        List<TaskExecuteRecord> results = taskExecuteRecordMapper.selectByExample(example);
        if (results == null || results.size() < 1) {
            return null;
        }
        return results.get(0);
    }

    @Override
    public int deleteByPK(TaskExecuteRecord record) throws Exception {
        if (record == null || record.getId() == null) {
            return 0;
        }
        Long recordId = record.getId();
        return taskExecuteRecordMapper.deleteByPrimaryKey(recordId);
    }

    @Override
    public int insert(TaskExecuteRecord record) throws Exception {
        return taskExecuteRecordMapper.insert(record);
    }

    @Override
    public int insertOrUpdate(TaskExecuteRecord record) throws Exception {
        // Don't use insert-on-dup-key-update when there exists two unique keys in one table
        // see https://dev.mysql.com/doc/refman/8.0/en/insert-on-duplicate.html
        AssertUtils.assertNotNull(record.getTaskKey(), "TaskExecuteRecord's task key must not be null. ");
        AssertUtils.assertNotEquals("", record.getTaskKey(), "TaskExecuteRecord's task key must not be empty");
        // Remove default id value, e.g. 0, to avoid violate primary key constraint
        record.setId(null);
        boolean taskExist = (findByTaskKey(record.getTaskKey()) != null);
        if (taskExist) {
            return taskExecuteRecordMapper.updateByExampleSelective(record,
                    new TaskExecuteRecordExample().createCriteria().andTaskKeyEqualTo(record.getTaskKey()).example());
        } else {
            return taskExecuteRecordMapper.insertSelective(record);
        }
    }

    @Override
    public void markExecution(String taskName) throws Exception {
        if (taskName != null && !taskName.isEmpty()) {
            TaskExecuteRecord record = TaskExecuteRecord.builder().lastExecuteTime(new Date().getTime()).taskKey(taskName).build();
            insertOrUpdate(record);
        }
    }
}
