package com.ctrip.zeus.task.verification;

import com.ctrip.zeus.dao.entity.TaskExecuteRecord;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.task.TaskExecuteRecordService;
import com.ctrip.zeus.service.verify.VerifyManager;
import com.ctrip.zeus.task.AbstractTask;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Calendar;

/**
 * @Discription: slb数据治理定期任务类
 **/
@Component("verifyTask")
public class VerifyTask extends AbstractTask {

    private final String TASK_NAME = "verifyTask";

    @Resource
    private VerifyManager verifyManager;
    @Resource
    private DbLockFactory dbLockFactory;
    @Resource
    private ConfigHandler configHandler;
    @Resource
    private TaskExecuteRecordService taskExecuteRecordService;

    private static DynamicIntProperty START_HOUR = DynamicPropertyFactory.getInstance().getIntProperty("verify.task.start.time", 5);
    private final long EXECUTION_INTERVAL = 23 * 60 * 60 * 1000;

    @Override
    public void start() {

    }

    @Override
    public void run() throws Exception {
        if (!configHandler.getEnable("verify.task", true)) {
            return;
        }
        Calendar c = Calendar.getInstance();
        int hours = c.get(Calendar.HOUR_OF_DAY);
        if (hours != START_HOUR.get()) {
            return;
        }
        TaskExecuteRecord lastExecutionRecord = taskExecuteRecordService.findByTaskKey(TASK_NAME);
        long currentTime = System.currentTimeMillis();
        if (lastExecutionRecord == null || currentTime - lastExecutionRecord.getLastExecuteTime() > EXECUTION_INTERVAL) {
            DistLock lock = dbLockFactory.newLock(TASK_NAME);
            try {
                if (lock.tryLock()) {
                    // In case that task has been done by another server when this lock acquired, so check again.
                    TaskExecuteRecord doubleCheck = taskExecuteRecordService.findByTaskKey(TASK_NAME);
                    if (doubleCheck == null || System.currentTimeMillis() - doubleCheck.getLastExecuteTime() > EXECUTION_INTERVAL) {
                        verifyManager.run();
                        taskExecuteRecordService.markExecution(TASK_NAME);
                    }
                }
            } finally {
                if (lock != null) {
                    lock.unlock();
                }
            }
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public long getInterval() {
        return 60000 * 5; // 5 min
    }
}
