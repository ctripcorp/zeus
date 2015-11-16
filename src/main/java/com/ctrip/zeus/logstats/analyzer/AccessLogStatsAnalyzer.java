package com.ctrip.zeus.logstats.analyzer;

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
    private final LogStatsAnalyzerConfig config;
    private final LogTracker logTracker;
    private final LogParser logParser;

    public AccessLogStatsAnalyzer() throws IOException {
        config = new LogStatsAnalyzerConfigBuilder()
                .setLogFormat(AccessLogFormat)
                .setLogFilename("/opt/app/nginx/access.log")
                .build();
        logTracker = config.getLogTracker();
        logParser = new AccessLogParser(config.getLineFormats());
    }

    @Override
    public LogStatsAnalyzerConfig getConfig() {
        return config;
    }

    @Override
    public String shoot() throws IOException {
        String raw = logTracker.move();
        return JsonStringWriter.write(logParser.parse(raw));
    }

    private class LogStatsAnalyzerConfigBuilder {
        private String logFormat;
        private String rootDir;
        private String logFilename;

        public LogStatsAnalyzerConfigBuilder setLogFilename(String logFilename) throws IOException {
            this.logFilename = logFilename;
            File f = new File(logFilename);
            if (f.exists() && f.isFile()) {
                rootDir = f.getParentFile().getAbsolutePath();
            } else {
                throw new IOException(logFilename + " is not a file or does not exist.");
            }
            return this;
        }

        public LogStatsAnalyzerConfigBuilder setLogFormat(String logFormat) {
            this.logFormat = logFormat;
            return this;
        }

        public LogStatsAnalyzerConfig build() {
            LineFormat format = new AccessLogLineFormat();
            format.setFormat(logFormat);
            LogTrackerStrategy strategy = new LogTrackerStrategy()
                    .setAllowLogRotate(true)
                    .setAllowTrackerMemo(true)
                    .setDoAsRoot(true)
                    .setTrackerMemoFilename(rootDir + "/access-foot-print.log")
                    .setLogFilename(logFilename);
            return new LogStatsAnalyzerConfig()
                    .addFormat(format)
                    .setLogTracker(new AccessLogTracker(strategy));
        }
    }
}
