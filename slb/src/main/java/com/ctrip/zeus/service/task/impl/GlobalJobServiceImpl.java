package com.ctrip.zeus.service.task.impl;

import com.ctrip.zeus.dao.entity.TaskGlobalJob;
import com.ctrip.zeus.dao.mapper.TaskGlobalJobMapper;
import com.ctrip.zeus.service.task.GlobalJobService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Discription
 **/
@Component("globalJobService")
public class GlobalJobServiceImpl implements GlobalJobService {

    @Resource
    private TaskGlobalJobMapper taskGlobalJobMapper;

    @Override
    public int deleteByPK(TaskGlobalJob proto) throws Exception {
        if (proto == null || proto.getJobKey() == null) {
            return 0;
        }
        return taskGlobalJobMapper.deleteByPrimaryKey(proto.getJobKey());
    }

    @Override
    public TaskGlobalJob findByPK(String key) throws Exception {
        return taskGlobalJobMapper.selectByPrimaryKey(key);
    }

    @Override
    public TaskGlobalJob findByJobKey(String jobKey) throws Exception {
        return findByPK(jobKey);
    }

    @Override
    public int insert(TaskGlobalJob proto) throws Exception {
        return taskGlobalJobMapper.insert(proto);
    }

    @Override
    public int updateByPK(TaskGlobalJob proto) throws Exception {
       return taskGlobalJobMapper.updateByPrimaryKey(proto);
    }

    @Override
    public int updateByJobKey(TaskGlobalJob proto) throws Exception {
        return updateByPK(proto);
    }
}
