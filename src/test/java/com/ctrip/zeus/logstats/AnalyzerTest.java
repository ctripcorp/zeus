package com.ctrip.zeus.logstats;

import com.ctrip.zeus.logstats.analyzer.AccessLogStatsAnalyzer;
import com.ctrip.zeus.logstats.analyzer.LogStatsAnalyzer;
import com.ctrip.zeus.logstats.analyzer.LogStatsAnalyzerConfig;
import com.ctrip.zeus.logstats.common.AccessLogLineFormat;
import com.ctrip.zeus.logstats.common.LineFormat;
import com.ctrip.zeus.logstats.tracker.LogTracker;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhoumy on 2015/11/18.
 */
public class AnalyzerTest {

    private static final String AccessLogFormatString =
            "[$time_local] $host $hostname $server_addr $request_method $uri " +
                    "\"$query_string\" $server_port $remote_user $remote_addr $http_x_forwarded_for " +
                    "$server_protocol \"$http_user_agent\" \"$cookie_COOKIE\" \"$http_referer\" " +
                    "$host $status $body_bytes_sent $request_time $upstream_response_time " +
                    "$upstream_addr $upstream_status";
    private static final LineFormat AccessLogFormat = new AccessLogLineFormat(AccessLogFormatString).generate();
    private static final int TrackerReadSize = 2048;
    private final URL accessLogUrl = this.getClass().getClassLoader().getResource("com.ctrip.zeus.service/access.log");

    @Test
    public void testInMemoryAnalyzer() throws IOException {
        final LogStatsAnalyzerConfig config =
                new AccessLogStatsAnalyzer.LogStatsAnalyzerConfigBuilder()
                        .setLogFormat(AccessLogFormat)
                        .setLogFilename(accessLogUrl.getFile())
                        .setTrackerReadSize(TrackerReadSize)
                        .build();
        final AtomicInteger count = new AtomicInteger();
        LogStatsAnalyzer analyzer = new AccessLogStatsAnalyzer(config);
        StatsDelegate reporter = new StatsDelegate<String>() {
            @Override
            public void delegate(String input) {
                Assert.assertNotNull(input);
                count.incrementAndGet();
                System.out.println(input);
            }
        };
        InputStream s = null;
        try {
            s = accessLogUrl.openStream();
            int total = s.available();
            analyzer.start();
            for (int i = 0; i < total / TrackerReadSize + 1; i++) {
                analyzer.analyze(reporter);
            }
            analyzer.stop();
        } finally {
            if (s != null)
                s.close();
        }
        Assert.assertEquals(14, count.get());
    }

    @Test
    public void testFileTrackingAnalyzer() throws IOException {
        final AccessLogStatsAnalyzer.LogStatsAnalyzerConfigBuilder builder =
                new AccessLogStatsAnalyzer.LogStatsAnalyzerConfigBuilder()
                        .setLogFormat(AccessLogFormat)
                        .setLogFilename(accessLogUrl.getFile())
                        .setTrackerReadSize(TrackerReadSize)
                        .allowTracking("access-log-test-track.log");
        String trackingFilename = new File(accessLogUrl.getPath()).getParentFile().getAbsolutePath() + "/access-log-test-track.log";
        File f = new File(trackingFilename);
        if (f.exists())
            f.delete();

        final DateFormat dateFormat = new SimpleDateFormat("dd/MMM/yyyy:hh:mm:ss Z", Locale.ENGLISH);
        final Date lastRecord = new Date(0, 1, 1);

        final AtomicInteger successCount = new AtomicInteger();
        final AtomicInteger errorCount = new AtomicInteger();
        StatsDelegate reporter = new StatsDelegate<String>() {
            @Override
            public void delegate(String input) {
                Assert.assertNotNull(input);
                try {
                    Date d = dateFormat.parse(getTimeLocal(input));
                    Assert.assertTrue(d.getTime() >= lastRecord.getTime());
                    successCount.incrementAndGet();
                    lastRecord.setTime(d.getTime());
                } catch (ParseException e) {
                    errorCount.incrementAndGet();
                }
                System.out.println(input);
            }
        };
        InputStream s = null;
        try {
            s = accessLogUrl.openStream();
            while (true) {
                LogStatsAnalyzer analyzer = new AccessLogStatsAnalyzer(builder.build());
                analyzer.start();
                if (!analyzer.reachFileEnd()) {
                    analyzer.analyze(reporter);
                    analyzer.stop();
                } else {
                    analyzer.stop();
                    break;
                }
            }
        } finally {
            if (s != null)
                s.close();
        }

        f = new File(trackingFilename);
        if (f.exists())
            f.delete();
        Assert.assertEquals(14, successCount.get());
        Assert.assertEquals(0, errorCount.get());
    }

    @Test
    public void testTrackerWhenLogRotating() throws Exception {
        final String logRotateFilename = "log-rotate-access.log";
        final String logRotateTrackingFilename = "log-rotate-tracker.log";
        File f = new File(logRotateFilename);
        if (f.exists())
            f.delete();
        f = new File(logRotateTrackingFilename);
        if (f.exists())
            f.delete();

        final long endTime = System.currentTimeMillis() + 60 * 1000L;
        final AtomicInteger writerCount = new AtomicInteger();
        final AtomicInteger trackerCount = new AtomicInteger();
        final CountDownLatch writerLatch = new CountDownLatch(1);
        final CountDownLatch trackerLatch = new CountDownLatch(1);

        Thread writer = new Thread() {
            @Override
            public void run() {
                TestLogWriter writer = new TestLogWriter(logRotateFilename, 10 * 1000L);
                try {
                    writer.run(endTime);
                    writer.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                writerCount.set(writer.getCount());
                writerLatch.countDown();
            }
        };

        Thread reader = new Thread() {
            @Override
            public void run() {
                final AccessLogStatsAnalyzer.LogStatsAnalyzerConfigBuilder builder;
                try {
                    Thread.sleep(30L);
                    builder = new AccessLogStatsAnalyzer.LogStatsAnalyzerConfigBuilder()
                            .setLogFormat(AccessLogFormat)
                            .setLogFilename(logRotateFilename)
                            .setTrackerReadSize(TrackerReadSize)
                            .allowTracking(logRotateTrackingFilename);
                    File f = new File(logRotateTrackingFilename);
                    if (f.exists())
                        f.delete();
                    StatsDelegate reporter = new StatsDelegate<String>() {
                        @Override
                        public void delegate(String input) {
                            trackerCount.incrementAndGet();
                        }
                    };
                    LogTracker tracker = builder.build().getLogTracker();
                    tracker.start();
                    while (System.currentTimeMillis() < endTime + 30L) {
                        tracker.fastMove(reporter);
                    }
                    trackerLatch.countDown();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        writer.start();
        reader.start();
        writerLatch.await();
        trackerLatch.await();

        f = new File(logRotateFilename);
        if (f.exists())
            f.delete();
        f = new File(logRotateTrackingFilename);
        if (f.exists())
            f.delete();
        Assert.assertEquals(writerCount.get(), trackerCount.get());
    }

    @Test
    public void testAnalyzerPerformanceWhenLogRotating() throws Exception {
        final String logRotateFilename = "log-rotate-perf-access.log";
        final String logRotateTrackingFilename = "log-rotate-perf-tracker.log";
        File f = new File(logRotateFilename);
        if (f.exists())
            f.delete();
        f = new File(logRotateTrackingFilename);
        if (f.exists())
            f.delete();

        final long endTime = System.currentTimeMillis() + 60 * 1000L;
        final AtomicInteger writerCount = new AtomicInteger();
        final AtomicInteger readerCount = new AtomicInteger();
        final CountDownLatch writerLatch = new CountDownLatch(1);
        final CountDownLatch readerLatch = new CountDownLatch(1);

        Thread writer = new Thread() {
            @Override
            public void run() {
                TestLogWriter writer = new TestLogWriter(logRotateFilename, 10 * 1000L);
                try {
                    writer.run(endTime);
                    writer.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                writerCount.set(writer.getCount());
                writerLatch.countDown();
            }
        };

        Thread reader = new Thread() {
            @Override
            public void run() {
                final AccessLogStatsAnalyzer.LogStatsAnalyzerConfigBuilder builder;
                try {
                    builder = new AccessLogStatsAnalyzer.LogStatsAnalyzerConfigBuilder()
                            .setLogFormat(AccessLogFormat)
                            .setLogFilename(logRotateFilename)
                            .setTrackerReadSize(TrackerReadSize)
                            .allowTracking(logRotateTrackingFilename);
                    File f = new File(logRotateTrackingFilename);
                    if (f.exists())
                        f.delete();
                    StatsDelegate reporter = new StatsDelegate<String>() {
                        @Override
                        public void delegate(String input) {
                            readerCount.incrementAndGet();
                        }
                    };
                    LogStatsAnalyzer analyzer = new AccessLogStatsAnalyzer(builder.build());
                    analyzer.start();
                    while (System.currentTimeMillis() < endTime + 100L) {
                        analyzer.analyze(reporter);
                    }
                    readerLatch.countDown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        writer.start();
        reader.start();
        writerLatch.await();
        readerLatch.await();

        f = new File(logRotateFilename);
        if (f.exists())
            f.delete();
        f = new File(logRotateTrackingFilename);
        if (f.exists())
            f.delete();
        System.out.println("writer count: " + writerCount.get());
        System.out.println("reader count: " + readerCount.get());
        Assert.assertTrue((readerCount.get() / 60) > 20000);
    }

    private static String getTimeLocal(String value) {
        return value.substring(15, 41);
    }
}
