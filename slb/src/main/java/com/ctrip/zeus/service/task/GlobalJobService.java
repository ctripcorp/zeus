package com.ctrip.zeus.service.task;

import com.ctrip.zeus.dao.entity.TaskGlobalJob;

/**
 * @Discription
 **/
public interface GlobalJobService {

    int deleteByPK(TaskGlobalJob proto) throws Exception;

    TaskGlobalJob findByPK(String key) throws Exception;

    TaskGlobalJob findByJobKey(String jobKey) throws Exception;

    int insert(TaskGlobalJob proto) throws Exception;

    int updateByPK(TaskGlobalJob proto) throws Exception;

    int updateByJobKey(TaskGlobalJob proto) throws Exception;
}
