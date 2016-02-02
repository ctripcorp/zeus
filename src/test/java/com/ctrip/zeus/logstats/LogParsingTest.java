package com.ctrip.zeus.logstats;

import com.ctrip.zeus.logstats.common.JsonStringWriter;
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

    @Test
    public void testFormatParsing() {
        String[] expectedKeys = {"time_local", "host", "hostname", "server_addr", "request_method", "uri",
                "query_string", "server_port", "remote_user", "remote_addr", "http_x_forwarded_for",
                "server_protocol", "http_user_agent", "cookie_COOKIE", "http_referer",
                "host", "status", "body_bytes_sent", "request_time", "upstream_response_time",
                "upstream_addr", "upstream_status"};
        String expectedPatternString = "\\[(.*)\\]\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+\\\"(.*)\\\"\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+\\\"(.*)\\\"\\s+\\\"(.*)\\\"\\s+\\\"(.*)\\\"\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)";

        LineFormat lineFormat = new AccessLogLineFormat().setFormat(AccessLogFormat).generate();
        String[] actualKeys = lineFormat.getKeys();
        Assert.assertArrayEquals(expectedKeys, actualKeys);
        Assert.assertEquals(expectedPatternString, lineFormat.getPatternString());
    }

    @Test
    public void testFormatRegistry() {
        String expectedPatternString = "\\[(.*)\\]\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+\\\"(.*)\\\"\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(-|(?:[0-9.]+(?:, [0-9.]+)*))\\s+(\\S+)\\s+\\\"(.*)\\\"\\s+\\\"(.*)\\\"\\s+\\\"(.*)\\\"\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)";
        LineFormat lineFormat = new AccessLogLineFormat()
                .setFormat(AccessLogFormat)
                .registerPatternForKey("http_x_forwarded_for", "(-|(?:[0-9.]+(?:, [0-9.]+)*))")
                .generate();
        Assert.assertEquals(expectedPatternString, lineFormat.getPatternString());
    }

    @Test
    public void testParser() {
        final String log = "[17/Nov/2015:15:10:44 +0800] ws.you.ctripcorp.com vms09191 10.8.95.27 POST /gsapi/api/xml/GetRecmdProduct \"-\" 80 - 10.8.106.66 - HTTP/1.1 \"-\" \"-\" \"-\" ws.you.ctripcorp.com 200 521 0.042 0.039 10.8.168.228:80 200";
        String[] expectedValues = {"17/Nov/2015:15:10:44 +0800", "ws.you.ctripcorp.com", "vms09191", "10.8.95.27", "POST", "/gsapi/api/xml/GetRecmdProduct", "-", "80", "-", "10.8.106.66", "-", "HTTP/1.1", "-", "-", "-", "ws.you.ctripcorp.com", "200", "521", "0.042", "0.039", "10.8.168.228:80", "200"};
        LineFormat lineFormat = new AccessLogLineFormat().setFormat(AccessLogFormat).generate();
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
    public void testParser2() {
        String log1 = "[17/Nov/2015:15:10:44 +0800] ws.you.ctripcorp.com vms09191 10.8.95.27 POST /gsapi/api/xml/GetRecmdProduct \"-\" 80 - 10.8.106.66 - HTTP/1.1 \"-\" \"-\" \"-\" ws.you.ctripcorp.com 200 521 0.042 0.039 10.8.168.228:80 200";
        String log2 = "[02/Dec/2015:13:02:19 +0800] ws.util.you.ctripcorp.com vms09191 10.8.95.27 POST /bgmgmt/api/json/ExecUpdateContentProcess \"-\" 80 - 10.15.114.31 10.32.65.134, 10.15.202.207 HTTP/1.1 \"python-requests/2.2.0 CPython/2.7.6 Windows/7\" \"-\" \"-\" ws.util.you.ctripcorp.com 200 143 0.005 0.005 10.8.24.101:80 200";
        String log3 = "[02/Dec/2015:13:43:03 +0800] ws.mobile.qiche.ctripcorp.com vms09191 10.8.95.27 POST /app/index.php \"param=/api/home&method=config.getAppConfig&_fxpcqlniredt=09031130410105805720\" 80 - 10.15.138.65 117.136.75.139 HTTP/1.1 \"\" \"-\" \"http://m.ctrip.com/webapp/train/?allianceid=106334&sid=728666&ouid=4&sourceid=2377\" ws.mobile.qiche.ctripcorp.com 200 99 0.017 0.017 10.8.119.73:80 200";
        String log4 = "[02/Dec/2015:13:00:10 +0800] ws.schedule.ctripcorp.com vms09191 10.8.95.27 POST /UbtPushApi/UserActionReceiveHandler.ashx \"-\" 80 - 10.8.91.104 - HTTP/1.1 \"Java/THttpClient/HC\" \"-\" \"-\" ws.schedule.ctripcorp.com 200 24 0.007 0.007 10.8.168.238:80 200";
        LineFormat lineFormat = new AccessLogLineFormat()
                .setFormat(AccessLogFormat)
                .registerPatternForKey("request_time", "(-|\\d+\\.\\d+)")
                .registerPatternForKey("upstream_response_time", "((?:-|\\d+\\.\\d+)(?: : (?:-|\\d+\\.\\d+))?)")
                .registerPatternForKey("upstream_addr", "((?:-|\\S+)(?: : (?:-|\\S+)?))")
                .registerPatternForKey("upstream_status", "((?:-|\\d{3})(?: : (?:-|\\d{3})?))")
                .registerPatternForKey("http_x_forwarded_for", "(-|(?:[0-9.]+(?:, [0-9.]+)*))")
                .generate();
        List<LineFormat> formats = new ArrayList<>();
        formats.add(lineFormat);
        final LogParser parser = new AccessLogParser(formats);
        Assert.assertTrue(parser.parse(log1).size() > 0);
        Assert.assertTrue(parser.parse(log2).size() > 0);
        Assert.assertTrue(parser.parse(log3).size() > 0);
        Assert.assertTrue(parser.parse(log4).size() > 0);
        for (KeyValue keyValue : parser.parse(log1)) {
            Assert.assertNotNull(keyValue.getValue());
        }
        for (KeyValue keyValue : parser.parse(log2)) {
            Assert.assertNotNull(keyValue.getValue());
        }
        for (KeyValue keyValue : parser.parse(log3)) {
            Assert.assertNotNull(keyValue.getValue());
        }
        for (KeyValue keyValue : parser.parse(log4)) {
            Assert.assertNotNull(keyValue.getValue());
        }
    }

    @Test
    public void testInternalRewriteParser() {
        String log = "[02/Feb/2016:17:01:02 +0800] ws.connect.qiche.ctripcorp.com vms14669 10.8.208.22 GET /502page \"-\" 80 - 10.8.78.102 - HTTP/1.1 \"-\" \"-\" \"-\" ws.connect.qiche.ctripcorp.com 502 6003 0.015 - : 0.006 10.8.91.168:80 : 10.8.16.4:80 - : 200";
        System.out.println(log);
        LineFormat lineFormat = new AccessLogLineFormat().setFormat(AccessLogFormat)
                .registerPatternForKey("request_time", "(-|\\d+\\.\\d+)")
                .registerPatternForKey("upstream_response_time", "((?:-|\\d+\\.\\d+)(?: : (?:-|\\d+\\.\\d+))?)")
                .registerPatternForKey("upstream_addr", "((?:-|\\S+)(?: : (?:-|\\S+)?))")
                .registerPatternForKey("upstream_status", "((?:-|\\d{3})(?: : (?:-|\\d{3})?))")
                .generate();
        List<LineFormat> formats = new ArrayList<>();
        formats.add(lineFormat);
        final LogParser parser = new AccessLogParser(formats);
        Assert.assertTrue(parser.parse(log).size() > 0);
        for (KeyValue keyValue : parser.parse(log)) {
            switch (keyValue.getKey()) {
                case "upstream_response_time":
                    Assert.assertEquals("- : 0.006", keyValue.getValue());
                    break;
                case "upstream_addr":
                    Assert.assertEquals("10.8.91.168:80 : 10.8.16.4:80", keyValue.getValue());
                    break;
                case "upstream_status":
                    Assert.assertEquals("- : 200", keyValue.getValue());
                    break;
            }
        }
    }

    @Test
    public void testJsonSerializer() {
        String log = "[17/Nov/2015:15:10:44 +0800] ws.you.ctripcorp.com vms09191 10.8.95.27 POST /gsapi/api/xml/GetRecmdProduct \"-\" 80 - 10.8.106.66 - HTTP/1.1 \"-\" \"-\" \"-\" ws.you.ctripcorp.com 200 521 0.042 0.039 10.8.168.228:80 200";
        String expectedJsonValue = "{\"time_local\":\"17/Nov/2015:15:10:44 +0800\",\"host\":\"ws.you.ctripcorp.com\",\"hostname\":\"vms09191\",\"server_addr\":\"10.8.95.27\",\"request_method\":\"POST\",\"uri\":\"/gsapi/api/xml/GetRecmdProduct\",\"query_string\":\"-\",\"server_port\":\"80\",\"remote_user\":\"-\",\"remote_addr\":\"10.8.106.66\",\"http_x_forwarded_for\":\"-\",\"server_protocol\":\"HTTP/1.1\",\"http_user_agent\":\"-\",\"cookie_COOKIE\":\"-\",\"http_referer\":\"-\",\"status\":\"200\",\"body_bytes_sent\":\"521\",\"request_time\":\"0.042\",\"upstream_response_time\":\"0.039\",\"upstream_addr\":\"10.8.168.228:80\",\"upstream_status\":\"200\"}";
        LineFormat lineFormat = new AccessLogLineFormat().setFormat(AccessLogFormat).generate();
        List<LineFormat> formats = new ArrayList<>();
        formats.add(lineFormat);
        final LogParser parser = new AccessLogParser(formats);
        Assert.assertEquals(expectedJsonValue, new JsonStringWriter().write(parser.parse(log)));
    }
}
