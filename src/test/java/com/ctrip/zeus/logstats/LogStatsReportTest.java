package com.ctrip.zeus.logstats;

import com.ctrip.zeus.service.report.stats.AccessLogRecord;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by zhoumy on 2015/11/24.
 */
public class LogStatsReportTest {

    @Test
    public void testGenerateAccessLogRecordGen() {
        final String value = "{\"time_local\":\"18/Nov/2015:17:10:41 +0800\",\"host\":\"contents.ctrip.com\",\"hostname\":\"vms09922\",\"server_addr\":\"10.15.150.37\",\"request_method\":\"GET\",\"uri\":\"/market-channel-apppromotion/data.aspx\",\"query_string\":\"source=gdt&appid=379395415&app_type=ios&click_id=ovaeyvq4aaaayjvps43q&click_time=1447837841&muid=c37d9a4c30cb02b9dd491f23bc8b8d3c&advertiser_id=956419\",\"server_port\":\"80\",\"remote_user\":\"-\",\"remote_addr\":\"14.17.33.36\",\"http_x_forwarded_for\":\"-\",\"server_protocol\":\"HTTP/1.1\",\"http_user_agent\":\"-\",\"cookie_COOKIE\":\"-\",\"http_referer\":\"-\",\"status\":\"200\",\"body_bytes_sent\":\"36\",\"request_time\":\"0.016\",\"upstream_response_time\":\"0.016\",\"upstream_addr\":\"10.15.133.34:80\",\"upstream_status\":\"200\"}";
        final String value2 = "{\"time_local\":\"02/Dec/2015:13:00:12 +0800\",\"host\":\"ws.connect.qiche.ctripcorp.com\",\"hostname\":\"vms09191\",\"server_addr\":\"10.8.95.27\",\"request_method\":\"GET\",\"uri\":\"/index.php\",\"query_string\":\"param=/data/ticketStatus&date=2015-12-04&strJson=[{\\x22fromCity\\x22:\\x22\\x5Cu6c88\\x5Cu9633\\x22,\\x22toCity\\x22:\\x22\\x5Cu519c\\x5Cu5b89\\x22,\\x22website\\x22:\\x22www.84100.com\\x22,\\x22date\\x22:\\x222015-12-04\\x22}]\",\"server_port\":\"80\",\"remote_user\":\"-\",\"remote_addr\":\"10.8.112.232\",\"http_x_forwarded_for\":\"-\",\"server_protocol\":\"HTTP/1.1\",\"http_user_agent\":\"PHP/5.3.17\",\"cookie_COOKIE\":\"-\",\"http_referer\":\"-\",\"status\":\"200\",\"body_bytes_sent\":\"123\",\"request_time\":\"0.015\",\"upstream_response_time\":\"0.015\",\"upstream_addr\":\"10.8.91.167:80\",\"upstream_status\":\"200\"}";
        AccessLogRecord r = new AccessLogRecord(1L, value);
        Assert.assertNotNull(r);
        Assert.assertEquals(16, r.getCost());

        r = new AccessLogRecord(1L, value2);
        Assert.assertNotNull(r);
    }
}