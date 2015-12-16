package com.ctrip.zeus.logstats.analyzer;

import com.ctrip.zeus.logstats.StatsDelegate;
import com.ctrip.zeus.logstats.common.JsonStringWriter;
import com.ctrip.zeus.logstats.common.AccessLogLineFormat;
import com.ctrip.zeus.logstats.common.LineFormat;
import com.ctrip.zeus.logstats.parser.AccessLogParser;
import com.ctrip.zeus.logstats.parser.LogParser;
import com.ctrip.zeus.logstats.tracker.AccessLogTracker;
import com.ctrip.zeus.logstats.tracker.LogTracker;
import com.ctrip.zeus.logstats.tracker.LogTrackerStrategy;
import com.ctrip.zeus.service.build.conf.LogFormat;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by zhoumy on 2015/11/16.
 */
public class AccessLogStatsAnalyzer implements LogStatsAnalyzer {
    private static final String AccessLogFormat = LogFormat.getMainCompactString();
    private static final DynamicIntProperty TrackerReadSize = DynamicPropertyFactory.getInstance().getIntProperty("accesslog.tracker.readsize", 1024 * 5);
    private final LogStatsAnalyzerConfig config;
    private final LogTracker logTracker;
    private final LogParser logParser;

    public AccessLogStatsAnalyzer() {
        this(new LogStatsAnalyzerConfigBuilder()
                .isStartFromHead(false)
                .setLogFormat(new AccessLogLineFormat(AccessLogFormat).generate())
                .setLogFilename("/opt/logs/nginx/access.log")
                .setTrackerReadSize(TrackerReadSize.get())
                .build());
    }

    public AccessLogStatsAnalyzer(LogStatsAnalyzerConfig config) {
        this.config = config;
        logTracker = config.getLogTracker();
        logParser = new AccessLogParser(config.getLineFormats());
    }

    @Override
    public LogStatsAnalyzerConfig getConfig() {
        return config;
    }

    @Override
    public void start() throws IOException {
        logTracker.start();
    }

    @Override
    public void stop() throws IOException {
        logTracker.stop();
    }

    @Override
    public boolean reachFileEnd() throws IOException {
        return logTracker.reachFileEnd();
    }

    @Override
    public String analyze() throws IOException {
        String raw = logTracker.move();
        return logParser.parseToJsonString(raw);
    }

    @Override
    public void analyze(final StatsDelegate<String> delegator) throws IOException {
        logTracker.fastMove(new StatsDelegate<String>() {
            @Override
            public void delegate(String input) {
                delegator.delegate(logParser.parseToJsonString(input));
            }
        });
    }

    public static class LogStatsAnalyzerConfigBuilder {
        private LineFormat logFormat;
        private String logFilename;
        private String trackingFilename;
        private boolean allowTracking;
        private int trackerReadSize;
        private boolean startFromHead;

        public LogStatsAnalyzerConfigBuilder setLogFilename(String logFilename) {
            this.logFilename = logFilename;
            return this;
        }

        public LogStatsAnalyzerConfigBuilder setLogFormat(LineFormat logFormat) {
            this.logFormat = logFormat;
            return this;
        }

        public LogStatsAnalyzerConfigBuilder setTrackerReadSize(int size) {
            this.trackerReadSize = size;
            return this;
        }

        public LogStatsAnalyzerConfigBuilder allowTracking(String trackingFilename) {
            this.allowTracking = true;
            this.trackingFilename = trackingFilename;
            return this;
        }

        public LogStatsAnalyzerConfigBuilder isStartFromHead(boolean startFromHead) {
            this.startFromHead = startFromHead;
            return this;
        }

        public LogStatsAnalyzerConfig build() {
            File f = new File(logFilename);
            String rootDir = f.getAbsoluteFile().getParentFile().getAbsolutePath();
            LogTrackerStrategy strategy = new LogTrackerStrategy()
                    .setAllowLogRotate(true)
                    .setAllowTrackerMemo(allowTracking)
                    .setDoAsRoot(true)
                    .setStartMode(startFromHead ? LogTrackerStrategy.START_FROM_HEAD : LogTrackerStrategy.START_FROM_CURRENT)
                    .setTrackerMemoFilename(allowTracking ? rootDir + "/" + trackingFilename : null)
                    .setLogFilename(logFilename)
                    .setReadSize(trackerReadSize);
            return new LogStatsAnalyzerConfig()
                    .addFormat(logFormat)
                    .setLogTracker(new AccessLogTracker(strategy));
        }
    }
}
