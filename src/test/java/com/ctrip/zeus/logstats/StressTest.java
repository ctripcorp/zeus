package com.ctrip.zeus.logstats;

import com.ctrip.zeus.logstats.analyzer.LogStatsAnalyzer;
import com.ctrip.zeus.logstats.analyzer.nginx.AccessLogStatsAnalyzer;
import com.ctrip.zeus.logstats.common.AccessLogStateMachineFormat;
import com.ctrip.zeus.logstats.common.JsonStringWriter;
import com.ctrip.zeus.logstats.parser.KeyValue;
import com.ctrip.zeus.service.build.conf.LogFormat;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zhoumy on 2016/6/6.
 */
public class StressTest {
    public static void main(String[] args) throws IOException {
        final File accessLogFile = new File("D:/opt/logs/nginx/access.log");
        final AtomicLong errorCount = new AtomicLong();
        final AtomicLong succCount = new AtomicLong();
        final int analyzerWorkers = 2;
        final int readBufferSize = 5;

        final ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>() {
            @Override
            protected DateFormat initialValue() {
                return new SimpleDateFormat("dd/MMM/yyyy:hh:mm:ss Z", Locale.ENGLISH);
            }
        };

        final AccessLogStatsAnalyzer.LogStatsAnalyzerConfigBuilder builder = new AccessLogStatsAnalyzer.LogStatsAnalyzerConfigBuilder()
                .setLogFormat(new AccessLogStateMachineFormat(LogFormat.getMainCompactString()).generate())
                .setLogFilename(accessLogFile.getAbsolutePath())
                .setNumberOfConsumers(analyzerWorkers)
                .setReadBufferSize(1024 * readBufferSize)
                .isStartFromHead(true)
                .registerLogStatsDelegator(new StatsDelegate<List<KeyValue>>() {
                    @Override
                    public void delegate(List<KeyValue> input) {
                        if (input != null && input.size() > 0) {
                            try {
                                dateFormat.get().parse(input.get(0).getValue());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            succCount.incrementAndGet();
                            if (succCount.get() % 1000000 == 0) {
                                System.out.println("Sample survey: " + toJsonString(input));
                            }
                        } else {
                            errorCount.incrementAndGet();
                        }
                    }

                    @Override
                    public void delegate(String raw, List<KeyValue> input) {

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
        System.out.println("Parsing " + formatFileSize(accessLogFile.length()) + " access.log metrics takes " + (System.nanoTime() - now) / (1000 * 1000 * 1000) + " s.");
        System.out.println("the number of analyzer workers: " + analyzerWorkers);
        System.out.println("read buffer size: " + readBufferSize);
        System.out.println("success count: " + succCount.get());
        System.out.println("error count: " + errorCount.get());
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

    public static String formatFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
