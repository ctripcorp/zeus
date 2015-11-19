package com.ctrip.zeus.logstats;

import com.ctrip.zeus.logstats.analyzer.AccessLogStatsAnalyzer;
import com.ctrip.zeus.logstats.analyzer.LogStatsAnalyzer;
import com.ctrip.zeus.logstats.analyzer.LogStatsAnalyzerConfig;
import com.sun.el.stream.Stream;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhoumy on 2015/11/18.
 */
public class AnalyzerTest {

    private static final String AccessLogFormat =
            "[$time_local] $host $hostname $server_addr $request_method $uri " +
                    "\"$query_string\" $server_port $remote_user $remote_addr $http_x_forwarded_for " +
                    "$server_protocol \"$http_user_agent\" \"$cookie_COOKIE\" \"$http_referer\" " +
                    "$host $status $body_bytes_sent $request_time $upstream_response_time " +
                    "$upstream_addr $upstream_status";
    private static final int TrackerReadSize = 2048;
    private final URL accessLogUrl = this.getClass().getClassLoader().getResource("com.ctrip.zeus.service/access.log");

    @Test
    public void testAnalyzer() throws IOException {
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
}
