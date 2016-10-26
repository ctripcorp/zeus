package com.ctrip.zeus.logstats.regex;

import com.ctrip.zeus.logstats.common.JsonStringWriter;
import com.ctrip.zeus.logstats.common.AccessLogRegexFormat;
import com.ctrip.zeus.logstats.common.LineFormat;
import com.ctrip.zeus.logstats.parser.AccessLogRegexParser;
import com.ctrip.zeus.logstats.parser.KeyValue;
import com.ctrip.zeus.logstats.parser.LogParser;
import com.ctrip.zeus.service.build.conf.LogFormat;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhoumy on 2015/11/16.
 */
public class LogParsingTest {

    private static final String AccessLogFormat = LogFormat.getMainCompactString();

    @Test
    public void testFormatParsing() {
        String[] expectedKeys = {"time_local", "host", "hostname", "server_addr", "request_method", "request_uri",
                "server_port", "remote_user", "remote_addr", "http_x_forwarded_for",
                "server_protocol", "http_user_agent", "http_cookie", "http_referer",
                "status", "request_length", "bytes_sent", "request_time", "upstream_response_time",
                "upstream_addr", "upstream_status", "proxy_host"};
        String expectedPatternString = "\\[(.*)\\]\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+([^?]*)(?:.*)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(-|(?:[0-9.]+(?:, [0-9.]+)*))\\s+(\\S+)\\s+\\\"(.*)\\\"\\s+\\\"(.*)\\\"\\s+\\\"(.*)\\\"\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(-|\\d+\\.\\d+)\\s+((?:-|\\d+\\.\\d+)(?: : (?:-|\\d+\\.\\d+))?)\\s+((?:-|\\S+)(?: : (?:-|\\S+))?)\\s+((?:-|\\d{3})(?: : (?:-|\\d{3}))?)\\s+(\\S+)";

        LineFormat lineFormat = new AccessLogRegexFormat(AccessLogFormat).generate();
        String[] actualKeys = lineFormat.getKeys();
        Assert.assertArrayEquals(expectedKeys, actualKeys);
        Assert.assertEquals(expectedPatternString, ((AccessLogRegexFormat) lineFormat).getPatternString());
    }

    @Test
    public void testFormatRegistry() {
        String logFormat = "[$time_local] $host $hostname $server_addr $request_method $uri \"$query_string\" $server_port $remote_user $remote_addr $http_x_forwarded_for $server_protocol \"$http_user_agent\" \"$cookie_COOKIE\" \"$http_referer\" $host $status $body_bytes_sent $request_time $upstream_response_time $upstream_addr $upstream_status";
        String expectedPatternString = "\\[(.*)\\]\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+\\\"(.*)\\\"\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(-|(?:[0-9.]+(?:, [0-9.]+)*))\\s+(\\S+)\\s+\\\"(.*)\\\"\\s+\\\"(.*)\\\"\\s+\\\"(.*)\\\"\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)";

        LineFormat lineFormat = new AccessLogRegexFormat()
                .setFormat(logFormat)
                .registerComponentForKey("http_x_forwarded_for", "(-|(?:[0-9.]+(?:, [0-9.]+)*))")
                .generate();
        Assert.assertEquals(expectedPatternString, ((AccessLogRegexFormat) lineFormat).getPatternString());
    }

    @Test
    public void testParser() {
        final String log = "[08/Mar/2016:15:31:39 +0800] a.com 0359 127.22.25.94 POST /Activity-Order-OrderService/api/xml/AutoOrder?format=json 80 - 127.22.28.241 127.22.42.237 HTTP/1.1 \"-\" \"-\" \"-\" 200 652 815 0.019 0.018 : 0.1 127.22.44.22:80 : 127.0.0.1:80 200 : 400 backend_6004";
        String[] expectedValues = {"08/Mar/2016:15:31:39 +0800", "a.com", "0359", "127.22.25.94", "POST", "/Activity-Order-OrderService/api/xml/AutoOrder", "80", "-", "127.22.28.241", "127.22.42.237", "HTTP/1.1", "-", "-", "-", "200", "652", "815", "0.019", "0.018 : 0.1", "127.22.44.22:80 : 127.0.0.1:80", "200 : 400", "backend_6004"};

        LineFormat lineFormat = new AccessLogRegexFormat(AccessLogFormat).generate();
        List<LineFormat> formats = new ArrayList<>();
        formats.add(lineFormat);
        final LogParser parser = new AccessLogRegexParser(formats);
        List<String> actualValues = new ArrayList<>();
        for (KeyValue keyValue : parser.parse(log)) {
            actualValues.add(keyValue.getValue());
        }
        Assert.assertArrayEquals(expectedValues, actualValues.toArray(new String[actualValues.size()]));
    }

    @Test
    public void testParser2() {
        String logFormat = "[$time_local] $host $hostname $server_addr $request_method $uri \"$query_string\" $server_port $remote_user $remote_addr $http_x_forwarded_for $server_protocol \"$http_user_agent\" \"$cookie_COOKIE\" \"$http_referer\" $host $status $body_bytes_sent $request_time $upstream_response_time $upstream_addr $upstream_status";
        String log1 = "[17/Nov/2015:15:10:44 +0800] ws.you.localhost.com svr09191 127.8.95.27 POST /gsapi/api/xml/GetRecmdProduct \"-\" 80 - 127.8.106.66 - HTTP/1.1 \"-\" \"-\" \"-\" ws.you.localhost.com 200 521 0.042 0.039 127.8.168.228:80 200";
        String log2 = "[02/Dec/2015:13:02:19 +0800] ws.util.you.localhost.com svr09191 127.8.95.27 POST /bgmgmt/api/json/ExecUpdateContentProcess \"-\" 80 - 127.15.114.31 127.32.65.134, 127.15.202.207 HTTP/1.1 \"python-requests/2.2.0 CPython/2.7.6 Windows/7\" \"-\" \"-\" ws.util.you.localhost.com 200 143 0.005 0.005 127.8.24.101:80 200";
        String log3 = "[02/Dec/2015:13:43:03 +0800] ws.mobile.qiche.localhost.com svr09191 127.8.95.27 POST /app/index.php \"param=/api/home&method=config.getAppConfig&_fxpcqlniredt=09031130410105805720\" 80 - 127.15.138.65 117.136.75.139 HTTP/1.1 \"\" \"-\" \"http://m.localhost.com/webapp/train/?allianceid=106334&sid=728666&ouid=4&sourceid=2377\" ws.mobile.qiche.localhost.com 200 99 0.017 0.017 127.8.119.73:80 200";
        String log4 = "[02/Dec/2015:13:00:10 +0800] ws.schedule.localhost.com svr09191 127.8.95.27 POST /UbtPushApi/UserActionReceiveHandler.ashx \"-\" 80 - 127.8.91.104 - HTTP/1.1 \"Java/THttpClient/HC\" \"-\" \"-\" ws.schedule.localhost.com 200 24 0.007 0.007 127.8.168.238:80 200";

        LineFormat lineFormat = new AccessLogRegexFormat()
                .setFormat(logFormat)
                .registerComponentForKey("request_time", "(-|\\d+\\.\\d+)")
                .registerComponentForKey("upstream_response_time", "((?:-|\\d+\\.\\d+)(?: : (?:-|\\d+\\.\\d+))?)")
                .registerComponentForKey("upstream_addr", "((?:-|\\S+)(?: : (?:-|\\S+))?)")
                .registerComponentForKey("upstream_status", "((?:-|\\d{3})(?: : (?:-|\\d{3}))?)")
                .registerComponentForKey("http_x_forwarded_for", "(-|(?:[0-9.]+(?:, [0-9.]+)*))")
                .generate();
        List<LineFormat> formats = new ArrayList<>();
        formats.add(lineFormat);
        final LogParser parser = new AccessLogRegexParser(formats);
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
    public void testRealCases() {
        LineFormat lineFormat = new AccessLogRegexFormat()
                .setFormat(AccessLogFormat)
                .registerComponentForKey("request_time", "(-|\\d+\\.\\d+)")
                .registerComponentForKey("upstream_response_time", "((?:-|\\d+\\.\\d+)(?: : (?:-|\\d+\\.\\d+))?)")
                .registerComponentForKey("upstream_addr", "((?:-|\\S+)(?: : (?:-|\\S+))?)")
                .registerComponentForKey("upstream_status", "((?:-|\\d{3})(?: : (?:-|\\d{3}))?)")
                .registerComponentForKey("http_x_forwarded_for", "(-|(?:[0-9.]+(?:, [0-9.]+)*))")
                .generate();
        List<LineFormat> formats = new ArrayList<>();
        formats.add(lineFormat);
        final LogParser parser = new AccessLogRegexParser(formats);
        List<String> realCases = new ArrayList<>();
        realCases.add("[01/Jun/2016:09:00:13 +0800] ws.mobile.qiche.localhost.com svr5153hw1288 127.8.208.7 GET /index.php?param=/api/home&method=product.recommendBus&isNewVersion=1&from=%E6%B8%A9%E5%B7%9E&to=%E6%B8%A9%E5%B7%9E&date=2016-06-01&channel=tieyou&partner=tieyou.app 80 - 127.228.56.26 - HTTP/1.1 \"-\" \"-\" \"-\" 200 268 362 0.045 -, -, 0.045 127.8.169.162:80, 127.8.169.164:80, 127.8.177.23:80 -, -, 200 backend_441");
        for (String rc : realCases) {
            List<KeyValue> kvs = parser.parse(rc);
            Assert.assertTrue(kvs.size() > 0);
            for (KeyValue kv : kvs) {
                Assert.assertNotNull(kv.getValue());
            }
        }
    }

    @Test
    public void testInternalRewriteParser() {
        String logFormat = "[$time_local] $host $hostname $server_addr $request_method $uri \"$query_string\" $server_port $remote_user $remote_addr $http_x_forwarded_for $server_protocol \"$http_user_agent\" \"$cookie_COOKIE\" \"$http_referer\" $host $status $body_bytes_sent $request_time $upstream_response_time $upstream_addr $upstream_status";
        String log = "[02/Feb/2016:17:01:02 +0800] ws.connect.qiche.localhost.com svr14669 127.8.208.22 GET /502page \"-\" 80 - 127.8.78.102 - HTTP/1.1 \"-\" \"-\" \"-\" ws.connect.qiche.localhost.com 502 6003 0.015 - : 0.006 127.8.91.168:80 : 127.8.16.4:80 - : 200";

        LineFormat lineFormat = new AccessLogRegexFormat().setFormat(logFormat)
                .registerComponentForKey("request_time", "(-|\\d+\\.\\d+)")
                .registerComponentForKey("upstream_response_time", "((?:-|\\d+\\.\\d+)(?: : (?:-|\\d+\\.\\d+))?)")
                .registerComponentForKey("upstream_addr", "((?:-|\\S+)(?: : (?:-|\\S+))?)")
                .registerComponentForKey("upstream_status", "((?:-|\\d{3})(?: : (?:-|\\d{3}))?)")
                .generate();
        List<LineFormat> formats = new ArrayList<>();
        formats.add(lineFormat);
        final LogParser parser = new AccessLogRegexParser(formats);
        Assert.assertTrue(parser.parse(log).size() > 0);
        for (KeyValue keyValue : parser.parse(log)) {
            switch (keyValue.getKey()) {
                case "upstream_response_time":
                    Assert.assertEquals("- : 0.006", keyValue.getValue());
                    break;
                case "upstream_addr":
                    Assert.assertEquals("127.8.91.168:80 : 127.8.16.4:80", keyValue.getValue());
                    break;
                case "upstream_status":
                    Assert.assertEquals("- : 200", keyValue.getValue());
                    break;
            }
        }
    }

    @Test
    public void testJsonSerializer() {
        final String log = "[08/Mar/2016:15:31:39 +0800] a.com 0359 127.22.25.94 POST /Activity-Order-OrderService/api/xml/AutoOrder?format=json 80 - 127.22.28.241 127.22.42.237 HTTP/1.1 \"-\" \"-\" \"-\" 200 652 815 0.019 0.018 : 0.1 127.22.44.22:80 : 127.0.0.1:80 200 : 400 backend_6004";
        String expectedJsonValue = "{\"time_local\":\"08/Mar/2016:15:31:39 +0800\",\"host\":\"a.com\",\"hostname\":\"0359\",\"server_addr\":\"127.22.25.94\",\"request_method\":\"POST\",\"request_uri\":\"/Activity-Order-OrderService/api/xml/AutoOrder\",\"server_port\":\"80\",\"remote_user\":\"-\",\"remote_addr\":\"127.22.28.241\",\"http_x_forwarded_for\":\"127.22.42.237\",\"server_protocol\":\"HTTP/1.1\",\"http_user_agent\":\"-\",\"http_cookie\":\"-\",\"http_referer\":\"-\",\"status\":\"200\",\"request_length\":\"652\",\"bytes_sent\":\"815\",\"request_time\":\"0.019\",\"upstream_response_time\":\"0.018 : 0.1\",\"upstream_addr\":\"127.22.44.22:80 : 127.0.0.1:80\",\"upstream_status\":\"200 : 400\",\"proxy_host\":\"backend_6004\"}";

        LineFormat lineFormat = new AccessLogRegexFormat(AccessLogFormat).generate();
        List<LineFormat> formats = new ArrayList<>();
        formats.add(lineFormat);
        final LogParser parser = new AccessLogRegexParser(formats);
        Assert.assertEquals(expectedJsonValue, new JsonStringWriter().write(parser.parse(log)));
    }
}
