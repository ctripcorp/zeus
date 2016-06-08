package com.ctrip.zeus.logstats;

import com.ctrip.zeus.logstats.analyzer.LogStatsAnalyzer;
import com.ctrip.zeus.logstats.analyzer.LogStatsAnalyzerConfig;
import com.ctrip.zeus.logstats.analyzer.nginx.AccessLogStatsAnalyzer;
import com.ctrip.zeus.logstats.common.AccessLogStateMachineFormat;
import com.ctrip.zeus.logstats.common.JsonStringWriter;
import com.ctrip.zeus.logstats.common.LineFormat;
import com.ctrip.zeus.logstats.parser.KeyValue;
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
import java.util.List;
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
    private static final LineFormat AccessLogFormat = new AccessLogStateMachineFormat(AccessLogFormatString).generate();
    private static final int TrackerReadSize = 2048;
    private final URL accessLogUrl = this.getClass().getClassLoader().getResource("com.ctrip.zeus.service/access.log");

    @Test
    public void testInMemoryAnalyzer() throws IOException {
        final AtomicInteger count = new AtomicInteger();
        final LogStatsAnalyzerConfig config =
                new AccessLogStatsAnalyzer.LogStatsAnalyzerConfigBuilder()
                        .isStartFromHead(true)
                        .setLogFormat(AccessLogFormat)
                        .setLogFilename(accessLogUrl.getFile())
                        .setTrackerReadSize(TrackerReadSize)
                        .registerLogStatsDelegator(new StatsDelegate<List<KeyValue>>() {
                            @Override
                            public void delegate(List<KeyValue> input) {
                                Assert.assertTrue(input.size() > 0);
                                count.incrementAndGet();
                                System.out.println(toJsonString(input));
                            }
                        })
                        .build();

        LogStatsAnalyzer analyzer = new AccessLogStatsAnalyzer(config);
        InputStream s = null;
        try {
            s = accessLogUrl.openStream();
            int total = s.available();
            analyzer.start();
            for (int i = 0; i < total / TrackerReadSize + 1; i++) {
                analyzer.run();
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
        final ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>() {
            @Override
            protected DateFormat initialValue() {
                return new SimpleDateFormat("dd/MMM/yyyy:hh:mm:ss Z", Locale.ENGLISH);
            }
        };
        final Date lastRecord = new Date(0, 1, 1);

        final AtomicInteger successCount = new AtomicInteger();
        final AtomicInteger errorCount = new AtomicInteger();

        final AccessLogStatsAnalyzer.LogStatsAnalyzerConfigBuilder builder =
                new AccessLogStatsAnalyzer.LogStatsAnalyzerConfigBuilder()
                        .isStartFromHead(true)
                        .setLogFormat(AccessLogFormat)
                        .setLogFilename(accessLogUrl.getFile())
                        .setTrackerReadSize(TrackerReadSize)
                        .setNumberOfConsumers(1)
                        .allowTracking("access-log-test-track.log")
                        .registerLogStatsDelegator(new StatsDelegate<List<KeyValue>>() {
                            @Override
                            public void delegate(List<KeyValue> input) {
                                Assert.assertTrue(input.size() > 0);
                                String value = toJsonString(input);
                                try {
                                    Date d = dateFormat.get().parse(getTimeLocal(value));
                                    Assert.assertTrue(d.getTime() >= lastRecord.getTime());
                                    successCount.incrementAndGet();
                                    lastRecord.setTime(d.getTime());
                                } catch (ParseException e) {
                                    errorCount.incrementAndGet();
                                }
                                System.out.println(value);
                            }
                        });

        String trackingFilename = new File(accessLogUrl.getPath()).getParentFile().getAbsolutePath() + "/access-log-test-track.log";
        File f = new File(trackingFilename);
        if (f.exists())
            f.delete();

        InputStream s = null;
        try {
            s = accessLogUrl.openStream();
            while (true) {
                LogStatsAnalyzer analyzer = new AccessLogStatsAnalyzer(builder.build());
                analyzer.start();
                if (!analyzer.reachFileEnd()) {
                    analyzer.run();
                    analyzer.stop();
                } else {
                    analyzer.stop();
                    break;
                }
            }
            Assert.assertEquals(14, successCount.get());
            Assert.assertEquals(0, errorCount.get());
        } finally {
            if (s != null)
                s.close();
        }

        f = new File(trackingFilename);
        if (f.exists())
            f.delete();
    }

    @Test
    public void testAnalyzerWithMultiConsumers() throws IOException {
        final ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>() {
            @Override
            protected DateFormat initialValue() {
                return new SimpleDateFormat("dd/MMM/yyyy:hh:mm:ss Z", Locale.ENGLISH);
            }
        };
        final AtomicInteger successCount = new AtomicInteger();
        final AtomicInteger errorCount = new AtomicInteger();

        final AccessLogStatsAnalyzer.LogStatsAnalyzerConfigBuilder builder =
                new AccessLogStatsAnalyzer.LogStatsAnalyzerConfigBuilder()
                        .isStartFromHead(true)
                        .setLogFormat(AccessLogFormat)
                        .setLogFilename(accessLogUrl.getFile())
                        .setTrackerReadSize(TrackerReadSize)
                        .setNumberOfConsumers(5)
                        .allowTracking("access-log-test-track.log")
                        .registerLogStatsDelegator(new StatsDelegate<List<KeyValue>>() {
                            @Override
                            public void delegate(List<KeyValue> input) {
                                Assert.assertTrue(input.size() > 0);
                                String date = input.get(0).getValue();
                                try {
                                    dateFormat.get().parse(date);
                                    successCount.incrementAndGet();
                                } catch (Exception e) {
                                    errorCount.incrementAndGet();
                                }
                                System.out.println(toJsonString(input));
                            }
                        });

        String trackingFilename = new File(accessLogUrl.getPath()).getParentFile().getAbsolutePath() + "/access-log-test-track.log";
        File f = new File(trackingFilename);
        if (f.exists())
            f.delete();

        InputStream s = null;
        try {
            s = accessLogUrl.openStream();
            while (true) {
                LogStatsAnalyzer analyzer = new AccessLogStatsAnalyzer(builder.build());
                analyzer.start();
                if (!analyzer.reachFileEnd()) {
                    analyzer.run();
                    analyzer.stop();
                } else {
                    analyzer.stop();
                    break;
                }
            }
            Assert.assertEquals(14, successCount.get());
            Assert.assertEquals(0, errorCount.get());
        } finally {
            if (s != null)
                s.close();
        }

        f = new File(trackingFilename);
        if (f.exists())
            f.delete();
    }

    @Test
    public void testTrackerStartModeCurrent() throws IOException {
        final AtomicInteger count = new AtomicInteger();
        final LogStatsAnalyzerConfig config =
                new AccessLogStatsAnalyzer.LogStatsAnalyzerConfigBuilder()
                        .isStartFromHead(false)
                        .setLogFormat(AccessLogFormat)
                        .setLogFilename(accessLogUrl.getFile())
                        .setTrackerReadSize(TrackerReadSize)
                        .registerLogStatsDelegator(new StatsDelegate<List<KeyValue>>() {
                            @Override
                            public void delegate(List<KeyValue> input) {
                                Assert.assertTrue(input.size() > 0);
                                count.incrementAndGet();
                                System.out.println(toJsonString(input));
                            }
                        })
                        .build();
        LogStatsAnalyzer analyzer = new AccessLogStatsAnalyzer(config);
        InputStream s = null;
        try {
            s = accessLogUrl.openStream();
            int total = s.available();
            analyzer.start();
            analyzer.run();
            for (int i = 0; i < total / TrackerReadSize + 1; i++) {
                analyzer.run();
            }
            Assert.assertTrue(analyzer.reachFileEnd());
            analyzer.stop();
        } finally {
            if (s != null)
                s.close();
        }
        Assert.assertEquals(0, count.get());
    }

    @Test
    public void testTrackerWhenLogRotating() throws Exception {
        System.out.println("------------ testTrackerWhenLogRotating starts ------------");
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
                long now = System.currentTimeMillis();
                try {
                    writer.run(endTime);
                    writer.stop();
                    System.out.println("writer takes " + (System.currentTimeMillis() - now) + " ms.");
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
                            .isStartFromHead(true)
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
                    long now = System.currentTimeMillis();
                    tracker.start();
                    while (System.currentTimeMillis() < endTime + 30L) {
                        tracker.fastMove(reporter);
                    }
                    System.out.println("reader takes " + (System.currentTimeMillis() - now) + " ms.");
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
        System.out.println("------------ testTrackerWhenLogRotating ends ------------");
        Assert.assertEquals(writerCount.get(), trackerCount.get());
    }

    @Test
    public void testAnalyzerPerformanceWhenLogRotating() throws Exception {
        System.out.println("------------ testAnalyzerPerformanceWhenLogRotating starts ------------");
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
                long now = System.currentTimeMillis();
                try {
                    writer.run(endTime);
                    writer.stop();
                    System.out.println("writer takes " + (System.currentTimeMillis() - now) + " ms.");
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
                            .isStartFromHead(true)
                            .setLogFormat(AccessLogFormat)
                            .setLogFilename(logRotateFilename)
                            .setTrackerReadSize(TrackerReadSize)
                            .allowTracking(logRotateTrackingFilename)
                            .registerLogStatsDelegator(new StatsDelegate<List<KeyValue>>() {
                                @Override
                                public void delegate(List<KeyValue> input) {
                                    readerCount.incrementAndGet();
                                }
                            });
                    File f = new File(logRotateTrackingFilename);
                    if (f.exists())
                        f.delete();
                    LogStatsAnalyzer analyzer = new AccessLogStatsAnalyzer(builder.build());
                    long now = System.currentTimeMillis();
                    analyzer.start();
                    while (System.currentTimeMillis() < endTime + 100L) {
                        analyzer.run();
                    }
                    System.out.println("reader takes " + (System.currentTimeMillis() - now) + " ms.");
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
        System.out.println("------------ testAnalyzerPerformanceWhenLogRotating ends ------------");
        Assert.assertTrue((readerCount.get() / 60) > 20000);
    }

    private static String getTimeLocal(String value) {
        return value.substring(15, 41);
    }

    private static String toJsonString(List<KeyValue> input) {
        JsonStringWriter sw = new JsonStringWriter();
        sw.start();
        for (KeyValue kv : input) {
            sw.writeNode(kv.getKey(), kv.getValue());
        }
        sw.end();
        return sw.get();
    }
}
