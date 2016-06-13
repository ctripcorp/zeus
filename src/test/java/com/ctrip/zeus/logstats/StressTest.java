package com.ctrip.zeus.logstats;

import com.ctrip.zeus.logstats.analyzer.LogStatsAnalyzer;
import com.ctrip.zeus.logstats.analyzer.nginx.AccessLogStatsAnalyzer;
import com.ctrip.zeus.logstats.common.AccessLogRegexFormat;
import com.ctrip.zeus.logstats.parser.KeyValue;
import com.ctrip.zeus.service.build.conf.LogFormat;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zhoumy on 2016/6/6.
 */
public class StressTest {
    public static void main(String[] args) throws IOException {
        final File accessLogFile = new File("D:/opt/logs/nginx/prod-access.log");
        final AtomicLong errorCount = new AtomicLong();
        final AtomicLong succCount = new AtomicLong();

        final AccessLogStatsAnalyzer.LogStatsAnalyzerConfigBuilder builder = new AccessLogStatsAnalyzer.LogStatsAnalyzerConfigBuilder()
                .setLogFormat(new AccessLogRegexFormat(LogFormat.getMainCompactString()).generate())
                .setLogFilename(accessLogFile.getAbsolutePath())
                .setTrackerReadSize(1024 * 25)
                .isStartFromHead(true)
                .registerLogStatsDelegator(new StatsDelegate<List<KeyValue>>() {
                    @Override
                    public void delegate(List<KeyValue> input) {
                        if (input != null && input.size() > 0) {
                            succCount.incrementAndGet();
                        } else {
                            errorCount.incrementAndGet();
                        }
                    }
                });

        long now = System.nanoTime();

        AtomicInteger notify = new AtomicInteger(1000);
        LogStatsAnalyzer analyzer = new AccessLogStatsAnalyzer(builder.build());
        analyzer.start();
        while (!analyzer.reachFileEnd()) {
            notify.decrementAndGet();
            analyzer.run();
            if (notify.get() == 0) {
                System.out.println("time since start " + (System.nanoTime() - now) / (1000 * 1000) + " ms.");
                System.out.println("success count " + succCount.get());
                System.out.println("error count " + errorCount.get());
                notify.set(1000);
            }
        }
        analyzer.stop();
        System.out.println("Parsing 7.93 GB access.log metrics takes " + (System.nanoTime() - now) / (1000 * 1000 * 1000) + " s.");
        System.out.println("success count: " + succCount.get());
        System.out.println("error count: " + errorCount.get());
        Assert.assertEquals(0, errorCount.get());
    }
}
