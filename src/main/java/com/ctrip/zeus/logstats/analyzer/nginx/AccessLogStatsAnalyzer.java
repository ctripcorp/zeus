package com.ctrip.zeus.logstats.analyzer.nginx;

import com.ctrip.zeus.logstats.StatsDelegate;
import com.ctrip.zeus.logstats.analyzer.LogStatsAnalyzer;
import com.ctrip.zeus.logstats.analyzer.LogStatsAnalyzerConfig;
import com.ctrip.zeus.logstats.common.AccessLogStateMachineFormat;
import com.ctrip.zeus.logstats.common.LineFormat;
import com.ctrip.zeus.logstats.parser.AccessLogStateMachineParser;
import com.ctrip.zeus.logstats.parser.KeyValue;
import com.ctrip.zeus.logstats.parser.LogParser;
import com.ctrip.zeus.logstats.tracker.AccessLogTracker;
import com.ctrip.zeus.logstats.tracker.LogTracker;
import com.ctrip.zeus.logstats.tracker.LogTrackerStrategy;
import com.ctrip.zeus.service.build.conf.LogFormat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zhoumy on 2015/11/16.
 */
public class AccessLogStatsAnalyzer implements LogStatsAnalyzer {
    private static final String AccessLogFormat = LogFormat.getMainCompactString();

    private final LogStatsAnalyzerConfig config;
    private final LogTracker logTracker;
    private final LogParser logParser;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private AccessLogConsumers consumers;

    public AccessLogStatsAnalyzer() {
        this(new LogStatsAnalyzerConfigBuilder()
                .isStartFromHead(false)
                .setLogFormat(new AccessLogStateMachineFormat(AccessLogFormat).generate())
                .setLogFilename("/opt/logs/nginx/access.log")
                .setReadBufferSize(1024 * 5)
                .build());
    }

    public AccessLogStatsAnalyzer(LogStatsAnalyzerConfig config) {
        this.config = config;
        logTracker = config.getLogTracker();
        logParser = new AccessLogStateMachineParser(config.getLineFormats().get(0));
        if (config.allowDelegate()) {
            consumers = new AccessLogConsumers(this, logParser, config.getNumberOfConsumers());
        }
    }

    @Override
    public LogStatsAnalyzerConfig getConfig() {
        return config;
    }

    @Override
    public void start() throws IOException {
        logTracker.start();
        if (consumers != null) {
            consumers.start();
        }
        running.set(true);
    }

    @Override
    public void stop() throws IOException {
        if (running.compareAndSet(true, false)) {
            if (consumers != null) {
                consumers.shutDown();
            }
            logTracker.stop();
        }
    }

    @Override
    public boolean reachFileEnd() throws IOException {
        if (running.get()) return logTracker.reachFileEnd();
        return true;
    }

    @Override
    public void run() throws IOException {
        logTracker.fastMove(new StatsDelegate<String>() {
            @Override
            public void delegate(String input) {
                if (running.get()) {
                    if (consumers != null) {
                        consumers.accept(input);
                    }
                }
            }

            @Override
            public void delegate(String raw, String input) {

            }
        });
    }

    @Override
    public String analyze() throws IOException {
        if (running.get()) {
            String raw = logTracker.move();
            return logParser.parseToJsonString(raw);
        } else {
            return "";
        }
    }

    public static class LogStatsAnalyzerConfigBuilder {
        private LineFormat logFormat;
        private String logFilename;

        private boolean startFromHead;
        private Boolean dropOnFileChange;
        private int readBufferSize = 5;

        private StatsDelegate statsDelegator;
        private int numberOfConsumers = 2;

        private boolean allowTracking;
        private String trackingFilename;

        public LogStatsAnalyzerConfigBuilder setLogFilename(String logFilename) {
            this.logFilename = logFilename;
            return this;
        }

        public LogStatsAnalyzerConfigBuilder setLogFormat(LineFormat logFormat) {
            this.logFormat = logFormat;
            return this;
        }

        public LogStatsAnalyzerConfigBuilder setReadBufferSize(int size) {
            this.readBufferSize = size;
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

        public LogStatsAnalyzerConfigBuilder registerLogStatsDelegator(StatsDelegate<List<KeyValue>> statsDelegator) {
            this.statsDelegator = statsDelegator;
            return this;
        }

        public LogStatsAnalyzerConfigBuilder setNumberOfConsumers(int count) {
            this.numberOfConsumers = count;
            return this;
        }

        public LogStatsAnalyzerConfigBuilder isDropOnFileChange(boolean dropOnFileChange) {
            this.dropOnFileChange = dropOnFileChange;
            return this;
        }

        public LogStatsAnalyzerConfig build() {
            File f = new File(logFilename);
            String rootDir = f.getAbsoluteFile().getParentFile().getAbsolutePath();
            LogTrackerStrategy strategy = new LogTrackerStrategy()
                    .setLogFilename(logFilename)
                    .setAllowLogRotate(true, LogTrackerStrategy.LOGROTATE_RENAME)
                    .setReadBufferSize(readBufferSize)
                    .setStartMode(startFromHead ? LogTrackerStrategy.START_FROM_HEAD : LogTrackerStrategy.START_FROM_CURRENT)
                    .setAllowTrackerMemo(allowTracking)
                    .setTrackerMemoFilename(allowTracking ? rootDir + "/" + trackingFilename : null)
                    .setDoAsRoot(true);

            if (dropOnFileChange != null) {
                strategy.isDropOnFileChange(dropOnFileChange);
            }

            List<StatsDelegate> delegatorRegistry = new ArrayList<>();
            delegatorRegistry.add(statsDelegator);
            return new LogStatsAnalyzerConfig(delegatorRegistry)
                    .setNumberOfConsumers(numberOfConsumers)
                    .addFormat(logFormat)
                    .setLogTracker(new AccessLogTracker(strategy));
        }
    }
}
