package com.ctrip.zeus.logstats.state;

import com.ctrip.zeus.logstats.common.AccessLogRegexFormat;
import com.ctrip.zeus.logstats.common.AccessLogStateMachineFormat;
import com.ctrip.zeus.logstats.common.JsonStringWriter;
import com.ctrip.zeus.logstats.common.LineFormat;
import com.ctrip.zeus.logstats.parser.AccessLogRegexParser;
import com.ctrip.zeus.logstats.parser.AccessLogStateMachineParser;
import com.ctrip.zeus.logstats.parser.KeyValue;
import com.ctrip.zeus.logstats.parser.LogParser;
import com.ctrip.zeus.service.build.conf.LogFormat;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhoumy on 2016/06/08.
 */
public class StateMachineParsingTest {

    @Test
    public void testFormatParsing() {
        String[] expectedKeys = {"time_local", "host", "hostname", "server_addr", "request_method", "request_uri",
                "server_port", "remote_user", "remote_addr", "http_x_forwarded_for",
                "server_protocol", "http_user_agent", "http_cookie", "http_referer",
                "status", "request_length", "bytes_sent", "request_time", "upstream_response_time",
                "upstream_addr", "upstream_status", "proxy_host"};
        LineFormat lineFormat = new AccessLogStateMachineFormat(LogFormat.getMainCompactString()).generate();
        String[] actualKeys = lineFormat.getKeys();
        Assert.assertArrayEquals(expectedKeys, actualKeys);
    }

    @Test
    public void testParser() {
        final String log = "[08/Mar/2016:15:31:39 +0800] a.com 0359 10.2.25.94 POST /Activity-Order-OrderService/api/xml/AutoOrder?format=json 80 - 10.2.28.241 10.2.42.237 HTTP/1.1 \"-\" \"-\" \"-\" 200 652 815 0.019 0.018 : 0.1 10.2.44.22:80 : 127.0.0.1:80 200 : 400 backend_6004";
        String[] expectedValues = {"08/Mar/2016:15:31:39 +0800", "a.com", "0359", "10.2.25.94", "POST", "/Activity-Order-OrderService/api/xml/AutoOrder", "80", "-", "10.2.28.241", "10.2.42.237", "HTTP/1.1", "-", "-", "-", "200", "652", "815", "0.019", "0.018 : 0.1", "10.2.44.22:80 : 127.0.0.1:80", "200 : 400", "backend_6004"};

        LineFormat lineFormat = new AccessLogStateMachineFormat(LogFormat.getMainCompactString()).generate();

        final LogParser parser = new AccessLogStateMachineParser(lineFormat);
        List<String> actualValues = new ArrayList<>();
        for (KeyValue keyValue : parser.parse(log)) {
            actualValues.add(keyValue.getValue());
        }
        Assert.assertArrayEquals(expectedValues, actualValues.toArray(new String[actualValues.size()]));
    }

    @Test
    public void testParser2() {
        String logFormat = "[$time_local] $host $hostname $server_addr $request_method $uri \"$query_string\" $server_port $remote_user $remote_addr $http_x_forwarded_for $server_protocol \"$http_user_agent\" \"$cookie_COOKIE\" \"$http_referer\" $host $status $body_bytes_sent $request_time $upstream_response_time $upstream_addr $upstream_status";
        String log1 = "[17/Nov/2015:15:10:44 +0800] ws.you.ctripcorp.com vms09191 10.8.95.27 POST /gsapi/api/xml/GetRecmdProduct \"-\" 80 - 10.8.106.66 - HTTP/1.1 \"-\" \"-\" \"-\" ws.you.ctripcorp.com 200 521 0.042 0.039 10.8.168.228:80 200";
        String log2 = "[02/Dec/2015:13:02:19 +0800] ws.util.you.ctripcorp.com vms09191 10.8.95.27 POST /bgmgmt/api/json/ExecUpdateContentProcess \"-\" 80 - 10.15.114.31 10.32.65.134, 10.15.202.207 HTTP/1.1 \"python-requests/2.2.0 CPython/2.7.6 Windows/7\" \"-\" \"-\" ws.util.you.ctripcorp.com 200 143 0.005 0.005 10.8.24.101:80 200";
        String log3 = "[02/Dec/2015:13:43:03 +0800] ws.mobile.qiche.ctripcorp.com vms09191 10.8.95.27 POST /app/index.php \"param=/api/home&method=config.getAppConfig&_fxpcqlniredt=09031130410105805720\" 80 - 10.15.138.65 117.136.75.139 HTTP/1.1 \"\" \"-\" \"http://m.ctrip.com/webapp/train/?allianceid=106334&sid=728666&ouid=4&sourceid=2377\" ws.mobile.qiche.ctripcorp.com 200 99 0.017 0.017 10.8.119.73:80 200";
        String log4 = "[02/Dec/2015:13:00:10 +0800] ws.schedule.ctripcorp.com vms09191 10.8.95.27 POST /UbtPushApi/UserActionReceiveHandler.ashx \"-\" 80 - 10.8.91.104 - HTTP/1.1 \"Java/THttpClient/HC\" \"-\" \"-\" ws.schedule.ctripcorp.com 200 24 0.007 0.007 10.8.168.238:80 200";

        LineFormat lineFormat = new AccessLogStateMachineFormat(logFormat).generate();
        final LogParser parser = new AccessLogStateMachineParser(lineFormat);

        LineFormat refLineFormat = new AccessLogRegexFormat(logFormat).generate();
        final LogParser refParser = new AccessLogRegexParser(Lists.newArrayList(refLineFormat));

        Assert.assertTrue(parser.parse(log1).size() > 0);
        Assert.assertTrue(parser.parse(log2).size() > 0);
        Assert.assertTrue(parser.parse(log3).size() > 0);
        Assert.assertTrue(parser.parse(log4).size() > 0);

        Assert.assertArrayEquals(refParser.parse(log1).toArray(new KeyValue[0]), parser.parse(log1).toArray(new KeyValue[0]));
        Assert.assertArrayEquals(refParser.parse(log2).toArray(new KeyValue[0]), parser.parse(log2).toArray(new KeyValue[0]));
        Assert.assertArrayEquals(refParser.parse(log3).toArray(new KeyValue[0]), parser.parse(log3).toArray(new KeyValue[0]));
        Assert.assertArrayEquals(refParser.parse(log4).toArray(new KeyValue[0]), parser.parse(log4).toArray(new KeyValue[0]));
    }

    @Test
    public void testRealCases() {
        LineFormat lineFormat = new AccessLogStateMachineFormat(LogFormat.getMainCompactString()).generate();
        final LogParser parser = new AccessLogStateMachineParser(lineFormat);
        List<String> realCases = new ArrayList<>();
        realCases.add("[01/Jun/2016:09:00:13 +0800] ws.mobile.qiche.ctripcorp.com svr5153hw1288 10.8.208.7 GET /index.php?param=/api/home&method=product.recommendBus&isNewVersion=1&from=%E6%B8%A9%E5%B7%9E&to=%E6%B8%A9%E5%B7%9E&date=2016-06-01&channel=tieyou&partner=tieyou.app 80 - 10.28.56.26 - HTTP/1.1 \"-\" \"-\" \"-\" 200 268 362 0.045 -, -, 0.045 10.8.169.162:80, 10.8.169.164:80, 10.8.177.23:80 -, -, 200 backend_441");
        for (String rc : realCases) {
            List<KeyValue> kvs = parser.parse(rc);
            Assert.assertTrue(kvs.size() > 0);
            for (KeyValue kv : kvs) {
                System.out.println(kv.getKey() + ", " + kv.getValue());
                Assert.assertNotNull(kv.getValue());
            }
        }
    }

    @Test
    public void testInternalRewriteParser() {
        String logFormat = "[$time_local] $host $hostname $server_addr $request_method $uri \"$query_string\" $server_port $remote_user $remote_addr $http_x_forwarded_for $server_protocol \"$http_user_agent\" \"$cookie_COOKIE\" \"$http_referer\" $host $status $body_bytes_sent $request_time $upstream_response_time $upstream_addr $upstream_status";
        String log = "[02/Feb/2016:17:01:02 +0800] ws.connect.qiche.ctripcorp.com vms14669 10.8.208.22 GET /502page \"-\" 80 - 10.8.78.102 - HTTP/1.1 \"-\" \"-\" \"-\" ws.connect.qiche.ctripcorp.com 502 6003 0.015 - : 0.006 10.8.91.168:80 : 10.8.16.4:80 - : 200";

        LineFormat lineFormat = new AccessLogStateMachineFormat(logFormat).generate();
        final LogParser parser = new AccessLogStateMachineParser(lineFormat);
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
        final String log = "[08/Mar/2016:15:31:39 +0800] a.com 0359 10.2.25.94 POST /Activity-Order-OrderService/api/xml/AutoOrder?format=json 80 - 10.2.28.241 10.2.42.237 HTTP/1.1 \"-\" \"-\" \"-\" 200 652 815 0.019 0.018 : 0.1 10.2.44.22:80 : 127.0.0.1:80 200 : 400 backend_6004";
        String expectedJsonValue = "{\"time_local\":\"08/Mar/2016:15:31:39 +0800\",\"host\":\"a.com\",\"hostname\":\"0359\",\"server_addr\":\"10.2.25.94\",\"request_method\":\"POST\",\"request_uri\":\"/Activity-Order-OrderService/api/xml/AutoOrder\",\"server_port\":\"80\",\"remote_user\":\"-\",\"remote_addr\":\"10.2.28.241\",\"http_x_forwarded_for\":\"10.2.42.237\",\"server_protocol\":\"HTTP/1.1\",\"http_user_agent\":\"-\",\"http_cookie\":\"-\",\"http_referer\":\"-\",\"status\":\"200\",\"request_length\":\"652\",\"bytes_sent\":\"815\",\"request_time\":\"0.019\",\"upstream_response_time\":\"0.018 : 0.1\",\"upstream_addr\":\"10.2.44.22:80 : 127.0.0.1:80\",\"upstream_status\":\"200 : 400\",\"proxy_host\":\"backend_6004\"}";

        LineFormat lineFormat = new AccessLogStateMachineFormat(LogFormat.getMainCompactString()).generate();
        final LogParser parser = new AccessLogStateMachineParser(lineFormat);
        Assert.assertEquals(expectedJsonValue, new JsonStringWriter().write(parser.parse(log)));
    }
}
