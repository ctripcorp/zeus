package com.ctrip.zeus.logstats.analyzer;

import com.ctrip.zeus.logstats.StatsDelegate;
import com.ctrip.zeus.logstats.analyzer.util.JsonStringWriter;
import com.ctrip.zeus.logstats.common.AccessLogLineFormat;
import com.ctrip.zeus.logstats.common.LineFormat;
import com.ctrip.zeus.logstats.parser.AccessLogParser;
import com.ctrip.zeus.logstats.parser.LogParser;
import com.ctrip.zeus.logstats.tracker.AccessLogTracker;
import com.ctrip.zeus.logstats.tracker.LogTracker;
import com.ctrip.zeus.logstats.tracker.LogTrackerStrategy;

import java.io.File;
import java.io.IOException;

/**
 * Created by zhoumy on 2015/11/16.
 */
public class AccessLogStatsAnalyzer implements LogStatsAnalyzer {
    private static final String AccessLogFormat =
            "[$time_local] $host $hostname $server_addr $request_method $uri " +
                    "\"$query_string\" $server_port $remote_user $remote_addr $http_x_forwarded_for " +
                    "$server_protocol \"$http_user_agent\" \"$cookie_COOKIE\" \"$http_referer\" " +
                    "$host $status $body_bytes_sent $request_time $upstream_response_time ";
    private static final int TrackerReadSize = 2048;
    private final LogStatsAnalyzerConfig config;
    private final LogTracker logTracker;
    private final LogParser logParser;

    public AccessLogStatsAnalyzer() throws IOException {
        config = new LogStatsAnalyzerConfigBuilder()
                .setLogFormat(AccessLogFormat)
                .setLogFilename("/opt/app/nginx/access.log")
                .setTrackerReadSize(TrackerReadSize)
                .build();
        logTracker = config.getLogTracker();
        logParser = new AccessLogParser(config.getLineFormats());
    }

    @Override
    public LogStatsAnalyzerConfig getConfig() {
        return config;
    }

    @Override
    public String analyze() throws IOException {
        String raw = logTracker.move();
        return JsonStringWriter.write(logParser.parse(raw));
    }

    @Override
    public void analyze(final StatsDelegate<String> delegate) throws IOException {
        logTracker.fastMove(new StatsDelegate<String>() {
            @Override
            public void delegate(String input) {
                delegate(JsonStringWriter.write(logParser.parse(input)));
            }
        });
    }

    private class LogStatsAnalyzerConfigBuilder {
        private String logFormat;
        private String logFilename;
        private int trackerReadSize;

        public LogStatsAnalyzerConfigBuilder setLogFilename(String logFilename) throws IOException {
            this.logFilename = logFilename;
            return this;
        }

        public LogStatsAnalyzerConfigBuilder setLogFormat(String logFormat) {
            this.logFormat = logFormat;
            return this;
        }

        public LogStatsAnalyzerConfigBuilder setTrackerReadSize(int size) {
            this.trackerReadSize = size;
            return this;
        }

        public LogStatsAnalyzerConfig build() throws IOException {
            LineFormat format = new AccessLogLineFormat();
            format.setFormat(logFormat);
            File f = new File(logFilename);
            if (f.exists() && f.isFile()) {
                String rootDir = f.getParentFile().getAbsolutePath();
                LogTrackerStrategy strategy = new LogTrackerStrategy()
                        .setAllowLogRotate(true)
                        .setAllowTrackerMemo(true)
                        .setDoAsRoot(true)
                        .setTrackerMemoFilename(rootDir + "/access-foot-print.log")
                        .setLogFilename(logFilename)
                        .setReadSize(trackerReadSize);
                return new LogStatsAnalyzerConfig()
                        .addFormat(format)
                        .setLogTracker(new AccessLogTracker(strategy));
            } else {
                throw new IOException(logFilename + " is not a file or does not exist.");
            }
        }
    }
}
