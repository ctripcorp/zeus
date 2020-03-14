package com.ctrip.zeus.task.log;

import com.ctrip.zeus.logstats.common.FileChangeEvent;
import com.ctrip.zeus.logstats.tracker.DefaultLogWatchService;
import com.ctrip.zeus.logstats.tracker.LogTracker;
import com.ctrip.zeus.logstats.tracker.LogWatchService;
import com.ctrip.zeus.task.AbstractTask;
import com.ctrip.zeus.task.log.AccessLogStatsReporter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.inject.Singleton;
import java.io.File;
import java.util.List;

/**
 * Created by zhoumy on 2016/6/13.
 */
@Singleton
@Component("accessLogFileMonitor")
public class AccessLogFileMonitor extends AbstractTask {

    private LogWatchService logWatchService;
    private LogTracker logTracker;

    @Resource
    private AccessLogStatsReporter accessLogStatsReporter;

    @PostConstruct
    public void setComponent() {
        logTracker = accessLogStatsReporter.getLogStatsAnalyzer().getConfig().getLogTracker();
    }

    @Override
    public void start() {
        File f = new File(accessLogStatsReporter.getLogFilename());
        String dir = f.getParent();
        logWatchService = new DefaultLogWatchService(dir);
        logWatchService.registerWatchingFile(f.getName());
    }

    @Override
    public long getInterval() {
        return 1000;
    }

    @Override
    public void run() throws Exception {
        if (logWatchService.avaiable()) {
            List<FileChangeEvent> fce = logWatchService.pollEvents();
            if (fce == null || fce.size() == 0) return;
            if (logTracker != null) logTracker.reopenOnFileChange(fce.get(fce.size() - 1).getEvent());
        } else {
            logWatchService.start();
        }
    }

    @Override
    public void stop() {
        logWatchService.close();
    }
}
