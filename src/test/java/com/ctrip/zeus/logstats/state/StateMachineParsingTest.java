package com.ctrip.zeus.logstats.state;

import com.ctrip.zeus.logstats.common.AccessLogRegexFormat;
import com.ctrip.zeus.logstats.common.AccessLogStateMachineFormat;
import com.ctrip.zeus.logstats.common.JsonStringWriter;
import com.ctrip.zeus.logstats.common.LineFormat;
import com.ctrip.zeus.logstats.parser.AccessLogRegexParser;
import com.ctrip.zeus.logstats.parser.AccessLogStateMachineParser;
import com.ctrip.zeus.logstats.parser.KeyValue;
import com.ctrip.zeus.logstats.parser.LogParser;
import com.ctrip.zeus.logstats.parser.state.AccessLogContext;
import com.ctrip.zeus.logstats.parser.state.AccessLogStateMachine;
import com.ctrip.zeus.logstats.parser.state.LogStatsStateMachine;
import com.ctrip.zeus.logstats.parser.state.extended.IpState;
import com.ctrip.zeus.logstats.parser.state.extended.UpstreamState;
import com.ctrip.zeus.logstats.parser.state.extended.XForwardForStateMachine;
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
                "upstream_addr", "upstream_status", "upstream_name"};
        LineFormat lineFormat = new AccessLogStateMachineFormat(LogFormat.getMainCompactString()).generate();
        String[] actualKeys = lineFormat.getKeys();
        Assert.assertArrayEquals(expectedKeys, actualKeys);
    }

    @Test
    public void testParser() {
        final String log = "[08/Mar/2016:15:31:39 +0800] a.com 0359 127.2.25.94 POST /Activity-Order-OrderService/api/xml/AutoOrder?format=json 80 - 127.2.28.241 127.2.42.237 HTTP/1.1 \"-\" \"-\" \"-\" 200 652 815 0.019 0.018 : 0.1 127.2.44.22:80 : 127.0.0.1:80 200 : 400 backend_6004";
        String[] expectedValues = {"08/Mar/2016:15:31:39 +0800", "a.com", "0359", "127.2.25.94", "POST", "/Activity-Order-OrderService/api/xml/AutoOrder", "80", "-", "127.2.28.241", "127.2.42.237", "HTTP/1.1", "-", "-", "-", "200", "652", "815", "0.019", "0.018 : 0.1", "127.2.44.22:80 : 127.0.0.1:80", "200 : 400", "backend_6004"};

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
        String log1 = "[17/Nov/2015:15:127:44 +0800] ws.you.localhost.com svr09191 127.8.95.27 POST /gsapi/api/xml/GetRecmdProduct \"-\" 80 - 127.8.1276.66 - HTTP/1.1 \"-\" \"-\" \"-\" ws.you.localhost.com 200 521 0.042 0.039 127.8.168.228:80 200";
        String log2 = "[02/Dec/2015:13:02:19 +0800] ws.util.you.localhost.com svr09191 127.8.95.27 POST /bgmgmt/api/json/ExecUpdateContentProcess \"-\" 80 - 127.15.114.31 127.32.65.134, 127.15.202.207 HTTP/1.1 \"python-requests/2.2.0 CPython/2.7.6 Windows/7\" \"-\" \"-\" ws.util.you.localhost.com 200 143 0.005 0.005 127.8.24.1271:80 200";
        String log3 = "[02/Dec/2015:13:43:03 +0800] ws.mobile.qiche.localhost.com svr09191 127.8.95.27 POST /app/index.php \"param=/api/home&method=config.getAppConfig&_fxpcqlniredt=0903113041271275805720\" 80 - 127.15.138.65 117.136.75.139 HTTP/1.1 \"\" \"-\" \"http://m.localhost.com/webapp/train/?allianceid=1276334&sid=728666&ouid=4&sourceid=2377\" ws.mobile.qiche.localhost.com 200 99 0.017 0.017 127.8.119.73:80 200";
        String log4 = "[02/Dec/2015:13:00:127 +0800] ws.schedule.localhost.com svr09191 127.8.95.27 POST /UbtPushApi/UserActionReceiveHandler.ashx \"-\" 80 - 127.8.91.1274 - HTTP/1.1 \"Java/THttpClient/HC\" \"-\" \"-\" ws.schedule.localhost.com 200 24 0.007 0.007 127.8.168.238:80 200";

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
    public void testXForwardForParser() {
        LogStatsStateMachine m = new XForwardForStateMachine(new IpState("x-forward-for"));
        AccessLogContext c = new AccessLogContext("127.0.0.1, 127.0.0.2, 127.0.0.3");
        m.transduce(c);
        Assert.assertTrue(c.getResult().size() == 1);
        Assert.assertEquals("127.0.0.1, 127.0.0.2, 127.0.0.3", c.getResult().get(0).getValue());
    }

    @Test
    public void testUpstreamParser() {
        AccessLogStateMachine m = new AccessLogStateMachine(new UpstreamState("upstream"));
        List<AccessLogContext> upstreamCases = new ArrayList<>();
        upstreamCases.add(new AccessLogContext("192.168.1.1:80, 192.168.1.2:80, unix:/tmp/sock : 192.168.10.1:80, 192.168.10.2:80"));
        upstreamCases.add(new AccessLogContext("-"));
        upstreamCases.add(new AccessLogContext("unix:/tmp/sock : 192.168.10.1:80"));
        upstreamCases.add(new AccessLogContext(", 127.0.0.1"));
        upstreamCases.add(new AccessLogContext("-, 127.0.0.1"));
        for (AccessLogContext c : upstreamCases) {
            m.transduce(c);
            Assert.assertEquals(1, c.getResult().size());
        }
    }

    @Test
    public void testErrorRealCases() {
        LineFormat lineFormat = new AccessLogStateMachineFormat(LogFormat.getMainCompactString()).generate();
        final LogParser parser = new AccessLogStateMachineParser(lineFormat);
        List<String> realCases = new ArrayList<>();

        realCases.add("--");
        for (String rc : realCases) {
            List<KeyValue> kvs = parser.parse(rc);
            Assert.assertTrue(kvs.size() == 0);
            for (KeyValue kv : kvs) {
                System.out.println(kv.getKey() + ", " + kv.getValue());
                Assert.assertNotNull(kv.getValue());
            }
        }
    }

    @Test
    public void testRealCases() {
        LineFormat lineFormat = new AccessLogStateMachineFormat(LogFormat.getMainCompactString()).generate();
        final LogParser parser = new AccessLogStateMachineParser(lineFormat);

        List<String> realCases = new ArrayList<>();
        realCases.add("[21/Jun/2016:09:00:05 +0800] trains.localhost.com svr14890 127.8.211.25 POST /TrainBooking/Ajax/UI2IndexHandler.ashx 80 - 222.88.238.19 - HTTP/1.1 \"Mozilla/5.0 (compatible; MSIE 127.0; Windows NT 6.2; Trident/6.0)\" \"searchlist=searchlisttop=2; Session=smartlinkcode=U130026&smartlinklanguage=zh&SmartLinkKeyWord=&SmartLinkQuary=&SmartLinkHost=; adscityen=Nanyang; _bfa=1.1466470080946.9greo.1.1466470080946.1466470080946.1.127; _bfs=1.127; _ga=GA1.2.931298142.1466470084; _gat=1; appFloatCnt=4; __zpspc=9.1.1466470085.1466470185.4%232%7Cwww.baidu.com%7C%7C%7C12306%25E7%2581%25AB%25E8%25BD%25A6%25E7%25A5%25A8%25E7%25BD%2591%25E4%25B8%258A%25E8%25AE%25A2%25E7%25A5%25A8%25E5%25AE%2598%25E7%25BD%2591%7C%23; _jzqco=%7C%7C%7C%7C1466470114557%7C1.930272362.1466470085157.1466470139625.1466470185404.1466470139625.1466470185404.undefined.0.0.4.4; Union=AllianceID=4897&SID=130026&OUID=; _bfi=p1%3D1278002%26p2%3D1278012%26v1%3D127%26v2%3D9; ASP.NET_SessionSvc=MTAuOC45Mi4yNDF8OTA5MHxqaW5xaWFvfGRlZmF1bHR8MTQ0OTEzNzU1MjQ4Mg\" \"http://trains.localhost.com/TrainBooking/Search.aspx?from=xian&to=wulumuqinan&day=14&number=&fromCn=\\xE8\\xA5\\xBF\\xE5\\xAE\\x89&toCn=\\xE4\\xB9\\x8C\\xE9\\xB2\\x81\\xE6\\x9C\\xA8\\xE9\\xBD\\x90\\xE5\\x8D\\x97&tj=1#\" 200 1406 223 0.0127 0.0127 127.8.46.173:80 200 backend_07");
        realCases.add("[01/Jun/2016:09:00:13 +0800] ws.mobile.qiche.localhost.com svr5153hw1288 127.8.0.7 GET /index.php?param=/api/home&method=product.recommendBus&isNewVersion=1&from=%E6%B8%A9%E5%B7%9E&to=%E6%B8%A9%E5%B7%9E&date=2016-06-01&channel=tieyou&partner=tieyou.app 80 - 127.28.56.26 - HTTP/1.1 \"-\" \"-\" \"-\" 200 268 362 0.045 -, -, 0.045 127.8.169.162:80, 127.8.169.164:80, 127.8.177.23:80 -, -, 200 backend_441");
        realCases.add("[08/Dec/2016:11:07:07 +0800] test.reserved.localhost.com uat0361 10.2.25.83 GET /slb/test 80 - 10.32.21.25 - HTTP/1.1 \"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36\" \"_bfa=1.1481079026000.4cxbxj.1.1481094580041.1481106875118.5.12; _bfi=p1%3D88890106%26p2%3D0%26v1%3D12%26v2%3D0\" \"-\" 502 576 732 0.007 -, 0.006 , 10.2.25.95:8888 -, 200 backend_3");
        realCases.add("[05/Dec/2016:20:00:42 +0800] gateway.m.fws.qa.nt.localhost.com qatetools0069 10.2.21.33 POST /restapi/soa2/10933/hotel/product/ambitustravelcity?subEnv=fat369&_fxpcqlniredt=09031029110001555996 443 - 10.32.151.13 - HTTP/2.0 \"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36\" \"GUID=09031029110001555996; Union=OUID=&AllianceID=4897&SID=179120&SourceID=3516&Expires=1481520330480; cticket=3CE58C7655C0138B3F8763C1549BB73DCC951D5D8D7F727EC384DB54D6021C0B; DUID=F14EA3739FE77E920CB83F6CFDFBD1F6; IsNonUser=F; _abtest_userid=974d988c-9aa6-4b30-a345-ffb5fdfaa1f2; _ga=GA1.2.2000995266.1480907610; _gat=1\" \"https://w-hotel-m.fat369.qa.nt.localhost.com/webapp/hotel/travelaround/?cityid=1&cname=%E5%8C%97%E4%BA%AC\" 499 449 0 29.563 - 10.2.38.40:8080 - backend_3873");
        for (String rc : realCases) {
            System.out.println(rc);
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
        String log = "[02/Feb/2016:17:01:02 +0800] ws.connect.qiche.localhost.com svr14669 127.8.0.22 GET /502page \"-\" 80 - 127.8.78.1272 - HTTP/1.1 \"-\" \"-\" \"-\" ws.connect.qiche.localhost.com 502 6003 0.015 - : 0.006 127.8.91.168:80 : 127.8.16.4:80 - : 200";

        LineFormat lineFormat = new AccessLogStateMachineFormat(logFormat).generate();
        final LogParser parser = new AccessLogStateMachineParser(lineFormat);
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
        final String log = "[08/Mar/2016:15:31:39 +0800] a.com 0359 127.2.25.94 POST /Activity-Order-OrderService/api/xml/AutoOrder?format=json 80 - 127.2.28.241 127.2.42.237 HTTP/1.1 \"-\" \"-\" \"-\" 200 652 815 0.019 0.018 : 0.1 127.2.44.22:80 : 127.0.0.1:80 200 : 400 backend_6004";
        String expectedJsonValue = "{\"time_local\":\"08/Mar/2016:15:31:39 +0800\",\"host\":\"a.com\",\"hostname\":\"0359\",\"server_addr\":\"127.2.25.94\",\"request_method\":\"POST\",\"request_uri\":\"/Activity-Order-OrderService/api/xml/AutoOrder\",\"server_port\":\"80\",\"remote_user\":\"-\",\"remote_addr\":\"127.2.28.241\",\"http_x_forwarded_for\":\"127.2.42.237\",\"server_protocol\":\"HTTP/1.1\",\"http_user_agent\":\"-\",\"http_cookie\":\"-\",\"http_referer\":\"-\",\"status\":\"200\",\"request_length\":\"652\",\"bytes_sent\":\"815\",\"request_time\":\"0.019\",\"upstream_response_time\":\"0.018 : 0.1\",\"upstream_addr\":\"127.2.44.22:80 : 127.0.0.1:80\",\"upstream_status\":\"200 : 400\",\"upstream_name\":\"backend_6004\"}";

        LineFormat lineFormat = new AccessLogStateMachineFormat(LogFormat.getMainCompactString()).generate();
        final LogParser parser = new AccessLogStateMachineParser(lineFormat);
        Assert.assertEquals(expectedJsonValue, new JsonStringWriter().write(parser.parse(log)));
    }
}
