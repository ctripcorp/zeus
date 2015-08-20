package com.ctrip.zeus.nginx;

import com.ctrip.zeus.nginx.entity.ReqStatus;
import com.ctrip.zeus.nginx.entity.TrafficStatus;
import com.google.common.base.Preconditions;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by zhoumy on 2015/5/6.
 */
@Component("rollingTrafficStatus")
public class RollingTrafficStatus {
    private final CircularArray buckets;
    private final int numberOfBuckets;
    private final int interval;

    public RollingTrafficStatus() {
        this(10, 60);
    }

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
        buckets.addPair(stubStatus, reqStatus);
    }

    public void clearDirty(long stamp) {
        buckets.clearDirty(stamp);
    }

    public void clear() {
        buckets.clear();
    }

    public List<TrafficStatus> getResult() {
        return new LinkedList<>(buckets.getResult());
    }

    private class CircularArray implements Iterable<TrafficStatus> {
        private final LinkedList<TrafficStatus> buckets;
        private final int length;
        private Long[] lastStubStatus;
        private Map<String, Long[]> lastReqStatus;

        public CircularArray(int length) {
            buckets = new LinkedList<>();
            this.length = length + 1;
        }

        public void addPair(String rawStubStatus, String rawReqStatus) {
            TrafficStatus ts = new TrafficStatus().setTime(new Date());
            lastStubStatus = compareAndSetStubStatusDelta(rawStubStatus, ts);
            lastReqStatus = compareAndSetReqStatusDelta(rawReqStatus, ts);
            buckets.add(ts);
            if (buckets.size() == length) {
                buckets.removeFirst();
            }
        }

        public void clearDirty(long stamp) {
            long expectedEarlist = stamp - interval * 1000 * numberOfBuckets;
            while(!buckets.isEmpty()) {
                if (buckets.getFirst().getTime().getTime() > expectedEarlist) {
                    break;
                } else {
                    buckets.removeFirst();
                }
            }
        }

        public List<TrafficStatus> getResult() {
            return buckets;
        }

        public int size() {
            return buckets.size();
        }

        public void clear() {
            buckets.clear();
        }

        @Override
        public Iterator<TrafficStatus> iterator() {
            return Collections.unmodifiableList(buckets).iterator();
        }

        private Long[] compareAndSetStubStatusDelta(String rawStubStatus, TrafficStatus trafficStatus) {
            Long[] stubStatus = parseStubStatusNumber(rawStubStatus.split("\n"));
            extractStubStatus(getDelta(stubStatus, lastStubStatus), trafficStatus, stubStatus);
            return stubStatus;
        }

        private Map<String, Long[]> compareAndSetReqStatusDelta(String rawReqStatus, TrafficStatus trafficStatus) {
            Map<String, Long[]> reqStatus = parseReqStatusEntries(rawReqStatus.split("\n"));
            extractReqStatus(getDelta(reqStatus, lastReqStatus), trafficStatus);
            return reqStatus;
        }
    }

    protected static void extractReqStatus(Map<String, Long[]> upstreamMap, TrafficStatus trafficStatus) {
        for (String key : upstreamMap.keySet()) {
            Long[] data = upstreamMap.get(key);
            String[] hostUpstream = key.split("/");
            String hostName, groupId;
            hostName = groupId = "";
            if (hostUpstream.length > 0) {
                hostName = hostUpstream[0];
                if (hostUpstream.length > 1)
                    groupId = hostUpstream[1].replaceFirst("backend_", "");
            }
            if (groupId.equals(""))
                continue;
            Long upRequests = data[ReqStatusOffset.UpstreamReq.ordinal()];
            double upResponseTime = (upRequests == null || upRequests == 0) ? 0 : (double) data[ReqStatusOffset.UpstreamRt.ordinal()] / upRequests;
            Long requests = data[ReqStatusOffset.ReqTotal.ordinal()];
            double responseTime = (requests == null || requests == 0) ? 0 : (double) data[ReqStatusOffset.RtTotal.ordinal()] / requests;
            long parsedGroupId = -1L;
            try {
                parsedGroupId = Long.parseLong(groupId);
            } catch (Exception ex) {
                continue;
            }
            trafficStatus.addReqStatus(new ReqStatus().setHostName(hostName)
                    .setBytesInTotal(data[ReqStatusOffset.BytInTotal.ordinal()])
                    .setBytesOutTotal(data[ReqStatusOffset.BytOutTotal.ordinal()])
                    .setResponseTime(responseTime)
                    .setTotalRequests(requests)
                    .setGroupId(parsedGroupId)
                    .setUpRequests(upRequests)
                    .setUpResponseTime(upResponseTime)
                    .setUpTries(data[ReqStatusOffset.UpstreamTries.ordinal()])
                    .setSuccessCount(data[ReqStatusOffset.SuccessCount.ordinal()])
                    .setRedirectionCount(data[ReqStatusOffset.RedirectionCount.ordinal()])
                    .setClientErrCount(data[ReqStatusOffset.ClientErrCount.ordinal()])
                    .setServerErrCount(data[ReqStatusOffset.ServerErrorCount.ordinal()])
                    .setTime(trafficStatus.getTime()));
        }
    }

    protected static void extractStubStatus(Long[] data, TrafficStatus trafficStatus, Long[] current) {
        Long requests = data[StubStatusOffset.Requests.ordinal()];
        double responseTime = (requests == null || requests == 0) ? 0.0 : (double)data[StubStatusOffset.RequestTime.ordinal()] / requests;
        trafficStatus.setActiveConnections(current[StubStatusOffset.ActiveConn.ordinal()])
                .setAccepts(data[StubStatusOffset.Accepts.ordinal()])
                .setHandled(data[StubStatusOffset.Handled.ordinal()])
                .setRequests(requests)
                .setResponseTime(responseTime)
                .setReading(current[StubStatusOffset.Reading.ordinal()])
                .setWriting(current[StubStatusOffset.Writing.ordinal()])
                .setWaiting(current[StubStatusOffset.Waiting.ordinal()]);
    }

    private enum StubStatusOffset {
        ActiveConn, Accepts, Handled, Requests, RequestTime, Reading, Writing, Waiting
    }

    private enum ReqStatusOffset {
        BytInTotal, BytOutTotal, ConnTotal, ReqTotal, SuccessCount, RedirectionCount,
        ClientErrCount, ServerErrorCount, Other, RtTotal, UpstreamReq, UpstreamRt, UpstreamTries
    }

    protected static Long[] getDelta(Long[] current, Long[] previous) {
        if (previous == null)
            return current;
        Preconditions.checkState(current.length == previous.length);
        Long[] ans = new Long[current.length];
        for (int i = 0; i < ans.length; i++) {
            ans[i] = current[i] - previous[i];
        }
        return ans;
    }

    private Map<String, Long[]> getDelta(Map<String, Long[]> currentMap, Map<String, Long[]> previousMap) {
        if (previousMap == null)
            return currentMap;
        Map<String, Long[]> ans = new HashMap<>();
        for (String key : currentMap.keySet()) {
            Long[] current = currentMap.get(key);
            Long[] previous = previousMap.get(key);
            if (previous == null) {
                ans.put(key, current);
            } else {
                ans.put(key, getDelta(current, previous));
            }
        }
        return ans;
    }

    protected static Long[] parseStubStatusNumber(String[] values) {
        Preconditions.checkState(values.length == 4);
        final String activeConnectionKey = "Active connections: ";
        final String readingKey = "Reading: ";
        final String writingKey = "Writing: ";
        final String waitingKey = "Waiting: ";

        Long[] result = new Long[StubStatusOffset.values().length];
        result[0] = Long.parseLong(values[0].trim().substring(activeConnectionKey.length()));
        String[] reqSrc = values[2].trim().split(" ");
        for (int i = 0; i < reqSrc.length; i++) {
            result[i + 1] = Long.parseLong(reqSrc[i]);
        }
        String stateSrc = values[3].trim();
        result[5] = Long.parseLong(stateSrc.substring(readingKey.length(), stateSrc.indexOf(writingKey) - 1));
        result[6] = Long.parseLong(stateSrc.substring(stateSrc.indexOf(writingKey) + writingKey.length(), stateSrc.indexOf(waitingKey) - 1));
        result[7] = Long.parseLong(stateSrc.substring(stateSrc.indexOf(waitingKey) + waitingKey.length()));
        return result;
    }

    protected Map<String, Long[]> parseReqStatusEntries(String[] upstreamValues) {
        Map<String, Long[]> result = new HashMap<>();
        for (int i = 0; i < upstreamValues.length; i++) {
            String[] values = upstreamValues[i].split(",");
            Preconditions.checkState(values != null);
            Long[] data = new Long[ReqStatusOffset.values().length];
            for (int j = 0; j < data.length; j++) {
                data[j] = Long.parseLong(values[j + 1]);
            }
            result.put(values[0], data);
        }
        return result;
    }
}
