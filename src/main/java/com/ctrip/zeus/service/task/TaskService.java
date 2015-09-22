package com.ctrip.zeus.service.task;

import com.ctrip.zeus.task.entity.OpsTask;
import com.ctrip.zeus.task.entity.OpsTaskList;
import com.ctrip.zeus.task.entity.TaskResult;

import java.util.Date;
import java.util.List;

/**
 * Created by fanqq on 2015/7/28.
 */
public interface TaskService {

    public Long add(OpsTask task) throws Exception;

    public List<Long> add(List<OpsTask> task) throws Exception;

    public List<TaskResult> getResult(List<Long> taskIds) throws Exception;
    public TaskResult getResult(Long taskId) throws Exception;

    public List<OpsTask> getPendingTasks(Long slbId) throws Exception;

    public void updateTasks(List<OpsTask> tasks) throws Exception;
    public OpsTaskList find(Date fromDate,String opsType,Long targetSlbId)throws Exception;

    public boolean taskCancel(Long taskId)throws Exception;
}
