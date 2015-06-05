package com.ctrip.zeus.util;

import com.ctrip.zeus.nginx.entity.ReqStatus;
import com.ctrip.zeus.nginx.entity.TrafficStatus;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * Created by zhoumy on 2015/5/12.
 */
public class TrafficStatusTest {

    @Test
    public void testGetDelta() {
        testGetDelta(5, 5);
        testGetDelta(13, 10);
        testGetDelta(60, 30);
        testGetDelta(200, 100);
    }

    @Test
    public void testParseReqStatus() {
        String[] reqStatues = new String[3];
        reqStatues[0] = "localhost/cluster,8348,3738,1,21,21,1,2,3,4,14,21,14,21";
        reqStatues[1] = "localhost/,2501999,3760318,141,13106,13104,0,2,0,0,13104,0,0,0";
        reqStatues[2] = "localhost/upstream,408,128,1,7,7,7,0,0,0,14,6,18,8";
        TrafficStatus trafficStatus = new TrafficStatus();
        RollingTrafficStatus obj = new RollingTrafficStatus(0, 0);
        Map<String, Long[]> map = obj.parseReqStatusEntries(reqStatues);
        RollingTrafficStatus.extractReqStatus(map, trafficStatus);

        ReqStatus ref1 = new ReqStatus().setHostName("localhost").setUpName("cluster")
                .setBytesInTotal(8348L).setBytesOutTotal(3738L)
                .setTotalRequests(21L).setResponseTime((double)14/21).setSuccessCount(21L)
                .setRedirectionCount(1L).setClientErrCount(2L).setServerErrCount(3L).setUpRequests(21L)
                .setUpResponseTime((double)14/21).setUpTries(21L);
        ReqStatus ref2 = new ReqStatus().setHostName("localhost").setUpName("")
                .setBytesInTotal(2501999L).setBytesOutTotal(3760318L)
                .setTotalRequests(13106L).setResponseTime((double) 13104/13106).setSuccessCount(13104L)
                .setRedirectionCount(0L).setClientErrCount(2L).setServerErrCount(0L).setUpRequests(0L)
                .setUpResponseTime(0.0).setUpTries(0L);
        ReqStatus ref3 = new ReqStatus().setHostName("localhost").setUpName("upstream")
                .setBytesInTotal(408L).setBytesOutTotal(128L)
                .setTotalRequests(7L).setResponseTime((double) 14/7).setSuccessCount(7L)
                .setRedirectionCount(7L).setClientErrCount(0L).setServerErrCount(0L).setUpRequests(6L)
                .setUpResponseTime((double) 18/6).setUpTries(8L);
        assertReqStatusEquals(ref1, trafficStatus.getReqStatuses().get(0));
        assertReqStatusEquals(ref2, trafficStatus.getReqStatuses().get(1));
        assertReqStatusEquals(ref3, trafficStatus.getReqStatuses().get(2));
    }

    @Test
    public void testParseStubStatus() {
        String[] stubStatus = new String[2];
        stubStatus[0] = "Active connections: 1\n" +
                "    server accepts handled requests request_time\n" +
                "     1140 1140 1140 75806\n" +
                "    Reading: 0 Writing: 1 Waiting: 0";
        stubStatus[1] = "Active connections: 2 \n" +
                "server accepts handled requests request_time\n" +
                " 166 166 13144 21\n" +
                "Reading: 0 Writing: 1 Waiting: 1 ";
        TrafficStatus trafficStatus1 = new TrafficStatus();
        TrafficStatus trafficStatus2 = new TrafficStatus();
        Long[] arr1 = RollingTrafficStatus.parseStubStatusNumber(stubStatus[0].split("\n"));
        Long[] arr2 = RollingTrafficStatus.parseStubStatusNumber(stubStatus[1].split("\n"));
        RollingTrafficStatus.extractStubStatus(arr1, trafficStatus1, arr1);
        RollingTrafficStatus.extractStubStatus(arr2, trafficStatus2, arr2);

        TrafficStatus ref1 = new TrafficStatus().setActiveConnections(1L)
                .setAccepts(1140L).setHandled(1140L).setRequests(1140L).setResponseTime((double)75806/1140)
                .setReading(0L).setWriting(1L).setWaiting(0L);
        TrafficStatus ref2 = new TrafficStatus().setActiveConnections(2L)
                .setAccepts(166L).setHandled(166L).setRequests(13144L).setResponseTime((double)21/13144)
                .setReading(0L).setWriting(1L).setWaiting(1L);
        assertStubStatusEquals(ref1, trafficStatus1);
        assertStubStatusEquals(ref2, trafficStatus2);
    }

    @Test
    public void testAccumulatedResultCal() {
        RollingTrafficStatus obj = new RollingTrafficStatus(10, 1);
        String ss1 = "Active connections: 1\n" +
                "    server accepts handled requests request_time\n" +
                "     1 1 1 1\n" +
                "    Reading: 0 Writing: 1 Waiting: 0";
        String ss2 = "Active connections: 1\n" +
                "    server accepts handled requests request_time\n" +
                "     2 3 4 5\n" +
                "    Reading: 0 Writing: 1 Waiting: 1";

        String ss3 = "Active connections: 1\n" +
                "    server accepts handled requests request_time\n" +
                "     11 13 15 17\n" +
                "    Reading: 0 Writing: 1 Waiting: 2";
        String rs1 = "localhost/cluster,1200,1358,0,1,2,3,4,5,0,6,7,8,9";
        String rs2 = "localhost/cluster,1962,3592,0,21,20,19,18,17,0,16,15,14,13";
        String rs3 = "localhost/cluster,4783,7688,0,30,32,34,36,38,0,40,42,44,46";
        obj.add(ss1, rs1);
        obj.add(ss2, rs2);
        obj.add(ss3, rs3);

        TrafficStatus ref1 = new TrafficStatus();
        ReqStatus reqref1 = new ReqStatus().setHostName("localhost").setUpName("cluster")
                .setBytesInTotal(762L).setBytesOutTotal(2234L)
                .setTotalRequests(20L).setSuccessCount(18L)
                .setRedirectionCount(16L).setClientErrCount(14L).setServerErrCount(12L)
                .setResponseTime((double)10/20).setUpRequests(8L).setUpResponseTime((double)6/8).setUpTries(4L);
        ref1.setActiveConnections(1L).setAccepts(1L).setHandled(2L).setRequests(3L).setResponseTime((double)4/3)
                .setReading(0L).setWriting(1L).setWaiting(1L);

        TrafficStatus ref2 = new TrafficStatus();
        ReqStatus reqref2 = new ReqStatus().setHostName("localhost").setUpName("cluster")
                .setBytesInTotal(2821L).setBytesOutTotal(4096L)
                .setTotalRequests(9L).setSuccessCount(12L)
                .setRedirectionCount(15L).setClientErrCount(18L).setServerErrCount(21L)
                .setResponseTime((double) 24/9).setUpRequests(27L).setUpResponseTime((double) 30/27).setUpTries(33L);
        ref2.setActiveConnections(1L).setAccepts(9L).setHandled(10L).setRequests(11L).setResponseTime((double)12/11)
                .setReading(0L).setWriting(1L).setWaiting(2L);

        List<TrafficStatus> result = obj.getResult();
        Assert.assertEquals(3, result.size());
        assertStubStatusEquals(ref1, result.get(1));
        assertReqStatusEquals(reqref1, result.get(1).getReqStatuses().get(0));
        assertStubStatusEquals(ref2, result.get(2));
        assertReqStatusEquals(reqref2, result.get(2).getReqStatuses().get(0));
    }

    private void testGetDelta(int length, int round) {
        for (int i = 0; i < round; i++) {
            AtomicLongArray ref = new AtomicLongArray(length);
            Long[] init = generateArray(length);
            Long[] deltaArray = generateArray(length);
            for (int j = 0; j < length; j++) {
                ref.addAndGet(j, deltaArray[j] - init[j]);
            }
            assertArrayEquals(ref, RollingTrafficStatus.getDelta(deltaArray, init));
        }
    }

    private static void assertReqStatusEquals(ReqStatus expected, ReqStatus actual) {
        Assert.assertEquals(expected.getHostName() + "/" + expected.getUpName(), actual.getHostName() + "/" + actual.getUpName());
        Assert.assertTrue(expected.getBytesInTotal().longValue() == actual.getBytesInTotal().longValue() &&
                expected.getBytesOutTotal().longValue() == actual.getBytesOutTotal().longValue() &&
                expected.getClientErrCount().longValue() == actual.getClientErrCount().longValue() &&
                expected.getRedirectionCount().longValue() == actual.getRedirectionCount().longValue() &&
                expected.getResponseTime().doubleValue() == actual.getResponseTime().doubleValue() &&
                expected.getServerErrCount().longValue() == actual.getServerErrCount().longValue() &&
                expected.getSuccessCount().longValue() == actual.getSuccessCount().longValue() &&
                expected.getTotalRequests().longValue() == actual.getTotalRequests().longValue() &&
                expected.getUpRequests().longValue() == actual.getUpRequests().longValue() &&
                expected.getUpResponseTime().doubleValue() == actual.getUpResponseTime().doubleValue() &&
                expected.getUpTries().longValue() == actual.getUpTries().longValue());
    }

    private static void assertStubStatusEquals(TrafficStatus expected, TrafficStatus actual) {
        Assert.assertTrue(
                expected.getActiveConnections().longValue() == actual.getActiveConnections().longValue() &&
                expected.getAccepts().longValue() == actual.getAccepts().longValue() &&
                expected.getHandled().longValue() == actual.getHandled().longValue() &&
                expected.getRequests().longValue() == actual.getRequests().longValue() &&
                expected.getResponseTime().doubleValue() == actual.getResponseTime().doubleValue() &&
                expected.getReading().longValue() == actual.getReading().longValue() &&
                expected.getWriting().longValue() == actual.getWriting().longValue() &&
                expected.getWaiting().longValue() == actual.getWaiting().longValue()
        );
    }

    private static void assertArrayEquals(AtomicLongArray expected, Long[] actual) {
        Assert.assertEquals(expected.length(), actual.length);
        for (int i = 0; i < actual.length; i++) {
            Assert.assertEquals(expected.get(i), actual[i].longValue());
        }
    }

    private static Long[] generateArray(int length) {
        Long[] result = new Long[length];
        final Random rand = new Random();
        for (int i = 0; i < length; i++) {
            result[i] = rand.nextLong();
        }
        return result;
    }
}
