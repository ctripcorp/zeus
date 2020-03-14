package com.ctrip.zeus.task.meta;

import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.metainfo.StatisticsInfo;
import com.ctrip.zeus.task.AbstractTask;
import com.ctrip.zeus.util.EnvHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by fanqq on 2016/7/29.
 */
@Component("statisticsUpdater")
public class StatisticsUpdater extends AbstractTask {
    @Autowired
    StatisticsInfo statisticsInfo;
    @Resource
    ConfigHandler configHandler;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void start() {

    }

    @Override
    public void run() throws Exception {
        try {
            if (EnvHelper.portal() && configHandler.getEnable("MetaInfoUpdater", null, null, null, true)) {
                statisticsInfo.updateMeta();
            }
        } catch (Throwable throwable) {
            logger.error("MetaInfo update failed.", throwable);
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public long getInterval() {
        return 60000 * 10;
    }

}
