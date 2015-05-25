package com.ctrip.zeus.util;

import com.ctrip.zeus.nginx.entity.ReqStatus;
import com.ctrip.zeus.nginx.entity.TrafficStatus;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * Created by zhoumy on 2015/5/12.
 */
public class TrafficStatusTest {

    @Test
    public void testGetSum() {
        testGetSum(5, 5);
        testGetSum(13, 10);
        testGetSum(60, 30);
        testGetSum(200, 100);
    }

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
        Map<String, Integer[]> map = obj.parseReqStautsEntries(reqStatues);
        RollingTrafficStatus.extractReqStatus(map, trafficStatus);

        ReqStatus ref1 = new ReqStatus().setHostName("localhost").setUpName("cluster")
                .setTotalRequests(21).setResponseTime((double)14/21).setSuccessCount(21)
                .setRedirectionCount(1).setClientErrCount(2).setServerErrCount(3).setUpRequests(21)
                .setUpResponseTime((double)14/21).setUpTries(21);
        ReqStatus ref2 = new ReqStatus().setHostName("localhost").setUpName("")
                .setTotalRequests(13106).setResponseTime((double)13104/13106).setSuccessCount(13104)
                .setRedirectionCount(0).setClientErrCount(2).setServerErrCount(0).setUpRequests(0)
                .setUpResponseTime(0.0).setUpTries(0);
        ReqStatus ref3 = new ReqStatus().setHostName("localhost").setUpName("upstream")
                .setTotalRequests(7).setResponseTime((double)14/7).setSuccessCount(7)
                .setRedirectionCount(7).setClientErrCount(0).setServerErrCount(0).setUpRequests(6)
                .setUpResponseTime((double)18/6).setUpTries(8);
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
        Integer[] arr1 = RollingTrafficStatus.parseStubStatusNumber(stubStatus[0].split("\n"));
        Integer[] arr2 = RollingTrafficStatus.parseStubStatusNumber(stubStatus[1].split("\n"));
        RollingTrafficStatus.extractStubStatus(arr1, trafficStatus1, arr1);
        RollingTrafficStatus.extractStubStatus(arr2, trafficStatus2, arr2);

        TrafficStatus ref1 = new TrafficStatus().setActiveConnections(1)
                .setAccepts(1140).setHandled(1140).setRequests(1140).setResponseTime((double)75806/1140)
                .setReading(0).setWriting(1).setWaiting(0);
        TrafficStatus ref2 = new TrafficStatus().setActiveConnections(2)
                .setAccepts(166).setHandled(166).setRequests(13144).setResponseTime((double)21/13144)
                .setReading(0).setWriting(1).setWaiting(1);
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
        String rs1 = "localhost/cluster,0,0,0,1,2,3,4,5,0,6,7,8,9";
        String rs2 = "localhost/cluster,0,0,0,21,20,19,18,17,0,16,15,14,13";
        String rs3 = "localhost/cluster,0,0,0,30,32,34,36,38,0,40,42,44,46";
        obj.add(ss1, rs1);
        obj.add(ss2, rs2);
        obj.add(ss3, rs3);

        TrafficStatus ref = new TrafficStatus();
        ReqStatus reqref = new ReqStatus().setHostName("localhost").setUpName("cluster")
                .setTotalRequests(29).setSuccessCount(30)
                .setRedirectionCount(31).setClientErrCount(32).setServerErrCount(33)
                .setResponseTime((double)34/29).setUpRequests(35).setUpResponseTime((double)36/35).setUpTries(37);
        ref.setActiveConnections(1).setAccepts(10).setHandled(12).setRequests(14).setResponseTime((double)16/14)
                .setReading(0).setWriting(1).setWaiting(2);
        TrafficStatus result = obj.getAccumulatedResult();
        assertStubStatusEquals(ref, result);
        assertReqStatusEquals(reqref, result.getReqStatuses().get(0));
    }

    private void testGetSum(int length, int round) {
        AtomicIntegerArray ref = new AtomicIntegerArray(length);
        Integer[] sum = new Integer[length];
        for (int i = 0; i < round; i++) {
            Integer[] deltaArray = generateArray(length);
            for (int j = 0; j < length; j++) {
                ref.addAndGet(j, deltaArray[j]);
            }
            RollingTrafficStatus.getSum(deltaArray, sum);
        }
        assertArrayEquals(ref, sum);
    }

    private void testGetDelta(int length, int round) {
        for (int i = 0; i < round; i++) {
            AtomicIntegerArray ref = new AtomicIntegerArray(length);
            Integer[] init = generateArray(length);
            Integer[] deltaArray = generateArray(length);
            for (int j = 0; j < length; j++) {
                ref.addAndGet(j, deltaArray[j] - init[j]);
            }
            assertArrayEquals(ref, RollingTrafficStatus.getDelta(deltaArray, init));
        }
    }

    private static void assertReqStatusEquals(ReqStatus expected, ReqStatus actual) {
        Assert.assertEquals(expected.getHostName() + "/" + expected.getUpName(), actual.getHostName() + "/" + actual.getUpName());
        Assert.assertTrue(expected.getClientErrCount().intValue() == actual.getClientErrCount().intValue() &&
                expected.getRedirectionCount().intValue() == actual.getRedirectionCount().intValue() &&
                expected.getResponseTime().doubleValue() == actual.getResponseTime().doubleValue() &&
                expected.getServerErrCount().intValue() == actual.getServerErrCount().intValue() &&
                expected.getSuccessCount().intValue() == actual.getSuccessCount().intValue() &&
                expected.getTotalRequests().intValue() == actual.getTotalRequests().intValue() &&
                expected.getUpRequests().intValue() == actual.getUpRequests().intValue() &&
                expected.getUpResponseTime().doubleValue() == actual.getUpResponseTime().doubleValue() &&
                expected.getUpTries().intValue() == actual.getUpTries().intValue());
    }

    private static void assertStubStatusEquals(TrafficStatus expected, TrafficStatus actual) {
        Assert.assertTrue(expected.getActiveConnections().intValue() == actual.getActiveConnections().intValue() &&
                expected.getAccepts().intValue() == actual.getAccepts().intValue() &&
                expected.getHandled().intValue() == actual.getHandled().intValue() &&
                expected.getRequests().intValue() == actual.getRequests().intValue() &&
                expected.getResponseTime().doubleValue() == actual.getResponseTime().doubleValue() &&
                expected.getReading().intValue() == actual.getReading().intValue() &&
                expected.getWriting().intValue() == actual.getWriting().intValue() &&
                expected.getWaiting().intValue() == actual.getWaiting().intValue());
    }

    private static void assertArrayEquals(AtomicIntegerArray expected, Integer[] actual) {
        Assert.assertEquals(expected.length(), actual.length);
        for (int i = 0; i < actual.length; i++) {
            Assert.assertEquals(expected.get(i), actual[i].intValue());
        }
    }

    private static Integer[] generateArray(int length) {
        Integer[] result = new Integer[length];
        final Random rand = new Random();
        for (int i = 0; i < length; i++) {
            result[i] = rand.nextInt(1000);
        }
        return result;
    }
}
