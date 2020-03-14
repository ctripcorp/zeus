package com.ctrip.zeus.task.monitor;

import com.ctrip.zeus.lock.LockService;
import com.ctrip.zeus.model.lock.LockStatus;
import com.ctrip.zeus.support.ObjectJsonWriter;
import com.ctrip.zeus.task.AbstractTask;
import com.ctrip.zeus.util.EnvHelper;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicLongProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service("diskLockMonitor")
public class DiskLockMonitor extends AbstractTask {
    @Resource
    private LockService lockService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final DynamicBooleanProperty enable = DynamicPropertyFactory.getInstance().getBooleanProperty("disk.lock.auto.unlock", false);
    private static final DynamicLongProperty timeout = DynamicPropertyFactory.getInstance().getLongProperty("disk.lock.timeout", 10 * 60000);
    private static final DynamicStringProperty regex = DynamicPropertyFactory.getInstance().getStringProperty("disk.lock.auto.unlock.key.regex", "Task.*");

    @Override
    public void start() {

    }

    @Override
    public void run() throws Exception {
        if (!EnvHelper.portal() || !enable.get()) {
            return;
        }
        long currentDate = System.currentTimeMillis();
        for (LockStatus ls : lockService.getLockStatus()) {
            long timeoutLong = timeout.get();
            logger.info("[[AutoUnlock=start]]Start Check Auto Unlocked.LockInfo:" + ObjectJsonWriter.write(ls) + ";Timeout:" + timeoutLong);
            if (currentDate - ls.getCreatedTime().getTime() > timeoutLong) {
                Pattern pattern = Pattern.compile(regex.get());
                Matcher matcher = pattern.matcher(ls.getKey());
                if (matcher.find()) {
                    lockService.forceUnlock(ls.getKey());
                    logger.info("[[AutoUnlock=action]]Success Auto Unlocked.LockInfo:" + ObjectJsonWriter.write(ls) + ";Timeout:" + timeoutLong);
                }
            }
        }
    }


    @Override
    public void stop() {

    }

    @Override
    public long getInterval() {
        return 60000;
    }

}
