package com.ctrip.zeus.logstats;

import com.ctrip.zeus.logstats.analyzer.util.JsonStringWriter;
import com.ctrip.zeus.logstats.common.AccessLogLineFormat;
import com.ctrip.zeus.logstats.common.LineFormat;
import com.ctrip.zeus.logstats.parser.AccessLogParser;
import com.ctrip.zeus.logstats.parser.KeyValue;
import com.ctrip.zeus.logstats.parser.LogParser;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhoumy on 2015/11/16.
 */
public class LogParsingTest {

    private static final String AccessLogFormat =
            "[$time_local] $host $hostname $server_addr $request_method $uri " +
                    "\"$query_string\" $server_port $remote_user $remote_addr $http_x_forwarded_for " +
                    "$server_protocol \"$http_user_agent\" \"$cookie_COOKIE\" \"$http_referer\" " +
                    "$host $status $body_bytes_sent $request_time $upstream_response_time " +
                    "$upstream_addr $upstream_status";
    private final String log = "[17/Nov/2015:15:10:44 +0800] ws.you.ctripcorp.com vms09191 10.8.95.27 POST /gsapi/api/xml/GetRecmdProduct \"-\" 80 - 10.8.106.66 - HTTP/1.1 \"-\" \"-\" \"-\" ws.you.ctripcorp.com 200 521 0.042 0.039 10.8.168.228:80 200";
    @Test
    public void testFormatParsing() {
        String[] expectedKeys = {"time_local", "host", "hostname", "server_addr", "request_method", "uri",
                "query_string", "server_port", "remote_user", "remote_addr", "http_x_forwarded_for",
                "server_protocol", "http_user_agent", "cookie_COOKIE", "http_referer",
                "host", "status", "body_bytes_sent", "request_time", "upstream_response_time",
                "upstream_addr", "upstream_status"};
        String expectedPatternString = "\\[(.+)\\]\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+\\\"(.+)\\\"\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+\\\"(.+)\\\"\\s+\\\"(.+)\\\"\\s+\\\"(.+)\\\"\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)";

        LineFormat lineFormat = new AccessLogLineFormat();
        lineFormat.setFormat(AccessLogFormat);
        String[] actualKeys = lineFormat.getKeys();
        Assert.assertArrayEquals(expectedKeys, actualKeys);
        Assert.assertEquals(expectedPatternString, lineFormat.getPatternString());
    }

    @Test
    public void testParser() {
        String[] expectedValues = {"17/Nov/2015:15:10:44 +0800", "ws.you.ctripcorp.com", "vms09191", "10.8.95.27", "POST", "/gsapi/api/xml/GetRecmdProduct", "-", "80", "-", "10.8.106.66", "-", "HTTP/1.1", "-", "-", "-", "ws.you.ctripcorp.com", "200", "521", "0.042", "0.039", "10.8.168.228:80", "200"};

        LineFormat lineFormat = new AccessLogLineFormat();
        lineFormat.setFormat(AccessLogFormat);
        List<LineFormat> formats = new ArrayList<>();
        formats.add(lineFormat);
        final LogParser parser = new AccessLogParser(formats);
        List<String> actualValues = new ArrayList<>();
        for (KeyValue keyValue : parser.parse(log)) {
            actualValues.add(keyValue.getValue());
        }
        Assert.assertArrayEquals(expectedValues, actualValues.toArray(new String[actualValues.size()]));
    }

    @Test
    public void testJsonSerializer() {
        String expectedJsonValue = "{\"time_local\":\"17/Nov/2015:15:10:44 +0800\",\"host\":\"ws.you.ctripcorp.com\",\"hostname\":\"vms09191\",\"server_addr\":\"10.8.95.27\",\"request_method\":\"POST\",\"uri\":\"/gsapi/api/xml/GetRecmdProduct\",\"query_string\":\"-\",\"server_port\":\"80\",\"remote_user\":\"-\",\"remote_addr\":\"10.8.106.66\",\"http_x_forwarded_for\":\"-\",\"server_protocol\":\"HTTP/1.1\",\"http_user_agent\":\"-\",\"cookie_COOKIE\":\"-\",\"http_referer\":\"-\",\"status\":\"200\",\"body_bytes_sent\":\"521\",\"request_time\":\"0.042\",\"upstream_response_time\":\"0.039\",\"upstream_addr\":\"10.8.168.228:80\",\"upstream_status\":\"200\"}";
        LineFormat lineFormat = new AccessLogLineFormat();
        lineFormat.setFormat(AccessLogFormat);
        List<LineFormat> formats = new ArrayList<>();
        formats.add(lineFormat);
        final LogParser parser = new AccessLogParser(formats);
        Assert.assertEquals(expectedJsonValue, JsonStringWriter.write(parser.parse(log)));
    }
}
