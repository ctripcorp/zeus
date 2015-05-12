package com.ctrip.zeus.util;

import com.ctrip.zeus.nginx.entity.ReqStatus;
import com.ctrip.zeus.nginx.entity.TrafficStatus;
import com.google.common.base.Preconditions;

import java.util.*;

/**
 * Created by zhoumy on 2015/5/6.
 */
public class RollingTrafficStatus {
    private final CircularArray buckets;
    private final int numberOfBuckets;
    private final int interval;

    /**
     * Collecting data and offering statistics of a given time over defined interval.
     * For example, if you want to track 1-minute data per 10 minutes, it doesn't empty out every 10 minutes,
     * but continuously adds a new record to the container, when the max size (which is a fixed number) is reached,
     * the earliest one (which is the record collected 10 minutes before), will be dropped.
     *
     * In this example, numberOfBuckets values 1 (10/1), interval values 60 (seconds)
     *
     * @param numberOfBuckets defines how many data should be collected. (numberOfBuckets = time / interval)
     * @param interval the interval in seconds.
     */
    public RollingTrafficStatus(int numberOfBuckets, int interval) {
        this.numberOfBuckets = numberOfBuckets;
        this.interval = interval;
        buckets = new CircularArray(this.numberOfBuckets);
    }

    public void add(String stubStatus, String reqStatus) {
        buckets.addPair(new StatusPair(stubStatus, reqStatus));
    }

    public void clearDirty(long stamp) {
        buckets.clearDirty(stamp);
    }

    public void clear() {
        buckets.clear();
    }

    public TrafficStatus getAccumulatedResult() {
        TrafficStatus trafficStatus = new TrafficStatus();
        if (buckets.size() == 0)
            return trafficStatus;

        Integer[] stubStatusResult = new Integer[StubStatusOffset.values().length];
        Map<String, Integer[]> reqStatusResult = new HashMap<>();
        buckets.getAccumulatedStubStatus(stubStatusResult, reqStatusResult);
        extractStubStatus(stubStatusResult, trafficStatus);
        extractReqStatus(reqStatusResult, trafficStatus);
        return trafficStatus;
    }

    private static void extractReqStatus(Map<String, Integer[]> upstreamMap, TrafficStatus trafficStatus) {
        for (String key : upstreamMap.keySet()) {
            Integer[] data = upstreamMap.get(key);
            String[] hostUpstream = key.split("/");
            String hostName, upstreamName;
            hostName = upstreamName = "";
            if (hostUpstream.length > 0) {
                hostName = hostUpstream[0];
                if (hostUpstream.length > 1)
                    upstreamName = hostUpstream[1];
            }
            Integer upRequests = data[ReqStatusOffset.UpstreamReq.ordinal()];
            double upResponseTime = (upRequests == null || upRequests == 0) ? 0 : (double)data[ReqStatusOffset.UpstreamRt.ordinal()] / upRequests;
            Integer requests = data[ReqStatusOffset.ReqTotal.ordinal()];
            double responseTime = (requests == null || requests == 0) ? 0 :  (double)data[ReqStatusOffset.RtTotal.ordinal()] / requests;
            trafficStatus.addReqStatus(new ReqStatus().setHostName(hostName)
                    .setResponseTime(responseTime)
                    .setTotalRequests(requests)
                    .setUpName(upstreamName)
                    .setUpRequests(upRequests)
                    .setUpResponseTime(upResponseTime)
                    .setUpTries(data[ReqStatusOffset.UpstreamTries.ordinal()])
                    .setSuccessCount(data[ReqStatusOffset.SuccessCount.ordinal()])
                    .setRedirectionCount(data[ReqStatusOffset.RedirectionCount.ordinal()])
                    .setClientErrCount(data[ReqStatusOffset.ClientErrCount.ordinal()])
                    .setServerErrCount(data[ReqStatusOffset.ServerErrorCount.ordinal()]));
        }
    }

    private static void extractStubStatus(Integer[] data, TrafficStatus trafficStatus) {
        Integer requests = data[StubStatusOffset.Requests.ordinal()];
        double responseTime = (requests == null || requests == 0) ? 0.0 : (double)data[StubStatusOffset.RequestTime.ordinal()] / requests;
        trafficStatus.setActiveConnections(data[StubStatusOffset.ActiveConn.ordinal()])
                .setAccepts(data[StubStatusOffset.Accepts.ordinal()])
                .setHandled(data[StubStatusOffset.Handled.ordinal()])
                .setRequests(requests)
                .setResponseTime(responseTime)
                .setReading(data[StubStatusOffset.Reading.ordinal()])
                .setWriting(data[StubStatusOffset.Writing.ordinal()])
                .setWaiting(data[StubStatusOffset.Waiting.ordinal()]);
    }

    private enum StubStatusOffset {
        ActiveConn, Accepts, Handled, Requests, RequestTime, Reading, Writing, Waiting
    }

    private enum ReqStatusOffset {
        BytInTotal, BytOutTotal, ConnTotal, ReqTotal, SuccessCount, RedirectionCount,
        ClientErrCount, ServerErrorCount, Other, RtTotal, UpstreamReq, UpstreamRt, UpstreamTries
    }

    private class StatusPair {
        private final Integer[] stubStatus;
        private final Map<String, Integer[]> reqStatus;
        private Integer[] stubStatusDelta;
        private Map<String, Integer[]> reqStatusDelta;

        protected final long time;

        public StatusPair(String stubStatus, String reqStatus) {
            this.stubStatus = parseStubStatusNumber(stubStatus.split("\n"));
            this.reqStatus = parseReqStautsEntries(reqStatus.split("\n"));
            time = System.currentTimeMillis();
        }

        public Integer[] compareAndGetStubStatusDelta(Integer[] previous) {
            stubStatusDelta = stubStatusDelta == null ? getDelta(stubStatus, previous) : stubStatusDelta;
            return stubStatusDelta;
        }

        public Map<String, Integer[]> compareAndGetReqStatusDelta(Map<String, Integer[]> previous) {
            reqStatusDelta = reqStatusDelta == null ? getDelta(reqStatus, previous) : reqStatusDelta;
            return reqStatusDelta;
        }
    }

    private static void getSum(Integer[] current, Integer[] sum) {
        if (current == null)
            return;
        Preconditions.checkState(current.length == sum.length);
        for (int i = 0; i < sum.length; i++) {
            sum[i] = sum[i] == null ? current[i] : sum[i] + current[i];
        }
    }

    private static Integer[] getDelta(Integer[] current, Integer[] previous) {
        Preconditions.checkState(current.length == previous.length);
        Integer[] ans = new Integer[current.length];
        for (int i = 0; i < ans.length; i++) {
            ans[i] = current[i] - previous[i];
        }
        return ans;
    }

    private static void getSum(Map<String, Integer[]> currentMap, Map<String, Integer[]> sumMap) {
        for (String key : currentMap.keySet()) {
            Integer[] current = currentMap.get(key);
            Integer[] sum = sumMap.get(key);
            if (sum == null) {
                sumMap.put(key, current);
            } else {
                getSum(current, sum);
            }
        }
    }

    private Map<String, Integer[]> getDelta(Map<String, Integer[]> currentMap, Map<String, Integer[]> previousMap) {
        Map<String, Integer[]> ans = new HashMap<>();
        for (String key : currentMap.keySet()) {
            Integer[] current = currentMap.get(key);
            Integer[] previous = previousMap.get(key);
            if (previous == null) {
                ans.put(key, current);
            } else {
                ans.put(key, getDelta(current, previous));
            }
        }
        return ans;
    }

    private static Integer[] parseStubStatusNumber(String[] values) {
        Preconditions.checkState(values.length == 4);
        final String activeConnectionKey = "Active connections: ";
        final String readingKey = "Reading: ";
        final String writingKey = "Writing: ";
        final String waitingKey = "Waiting: ";

        Integer[] result = new Integer[StubStatusOffset.values().length];
        // Active Conn chooses the latest value
        result[0] = Integer.parseInt(values[0].trim().substring(activeConnectionKey.length()));
        String[] reqSrc = values[2].trim().split(" ");
        for (int i = 0; i < reqSrc.length; i++) {
            result[i + 1] = Integer.parseInt(reqSrc[i]);
        }
        String stateSrc = values[3].trim();
        // Reading, Writing, Waiting chooses the latest value
        result[5] = Integer.parseInt(stateSrc.substring(readingKey.length(), stateSrc.indexOf(writingKey) - 1));
        result[6] = Integer.parseInt(stateSrc.substring(stateSrc.indexOf(writingKey) + writingKey.length(), stateSrc.indexOf(waitingKey) - 1));
        result[7] = Integer.parseInt(stateSrc.substring(stateSrc.indexOf(waitingKey) + waitingKey.length()));
        return result;
    }

    private Map<String, Integer[]> parseReqStautsEntries(String[] upstreamValues) {
        Map<String, Integer[]> result = new HashMap<>();
        for (int i = 0; i < upstreamValues.length; i++) {
            String[] values = upstreamValues[i].split(",");
            Preconditions.checkState(values != null && values.length == ReqStatusOffset.values().length + 1);
            Integer[] data = new Integer[ReqStatusOffset.values().length];
            for (int j = 0; j < data.length; j++) {
                data[j] = Integer.parseInt(values[j + 1]);
            }
            result.put(values[0], data);
        }
        return result;
    }

    private class CircularArray implements Iterable<StatusPair> {
        private final LinkedList<StatusPair> buckets;
        private final int length;

        public CircularArray(int length) {
            buckets = new LinkedList<>();
            this.length = length + 1;
        }

        public void addPair(StatusPair data) {
            if (buckets.isEmpty()) {
                buckets.add(data);
                return;
            }
            data.compareAndGetStubStatusDelta(buckets.getLast().stubStatus);
            data.compareAndGetReqStatusDelta(buckets.getLast().reqStatus);
            buckets.add(data);
            if (buckets.size() == length) {
                buckets.removeFirst();
            }
        }

        public void clearDirty(long stamp) {
            long expectedEarlist = stamp - interval * 1000 * numberOfBuckets;
            while(!buckets.isEmpty()) {
                if (buckets.getFirst().time > expectedEarlist) {
                    break;
                } else {
                    buckets.removeFirst();
                }
            }
        }

        public void getAccumulatedStubStatus(Integer[] stubStatusResult, Map<String, Integer[]> reqStatusResult) {
            if (stubStatusResult == null) {
                stubStatusResult = new Integer[StubStatusOffset.values().length];
            }
            if (reqStatusResult == null) {
                reqStatusResult = new HashMap<>();
            }
            for (int i = 1; i < buckets.size(); i++) {
                StatusPair sp = buckets.get(i);
                getSum(sp.stubStatusDelta, stubStatusResult);
                getSum(sp.reqStatusDelta, reqStatusResult);
            }
        }

        public int size() {
            return buckets.size();
        }

        public void clear() {
            buckets.clear();
        }

        @Override
        public Iterator<StatusPair> iterator() {
            return Collections.unmodifiableList(buckets).iterator();
        }
    }
}
