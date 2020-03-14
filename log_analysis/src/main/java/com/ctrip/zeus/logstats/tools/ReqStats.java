package com.ctrip.zeus.logstats.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ReqStats {
    private AtomicLong count = new AtomicLong(0);

    private AtomicLong totalCost = new AtomicLong(0);
    private AtomicLong minCost = new AtomicLong(0);
    private AtomicLong maxCost = new AtomicLong(0);

    private AtomicLong totalRequestSize = new AtomicLong(0);
    private AtomicLong minRequestSize = new AtomicLong(0);
    private AtomicLong maxRequestSize = new AtomicLong(0);

    private AtomicLong totalResponseSize = new AtomicLong(0);
    private AtomicLong minResponseSize = new AtomicLong(0);
    private AtomicLong maxResponseSize = new AtomicLong(0);

    private Map<String, AtomicInteger> requestSizeSlotCountMap = new HashMap<String, AtomicInteger>();
    private Map<String, AtomicInteger> responseSizeSlotCountMap = new HashMap<String, AtomicInteger>();
    private Map<String, AtomicInteger> costSlotCountMap = new HashMap<String, AtomicInteger>();
    private Map<String, AtomicInteger> statusSlotCountMap = new HashMap<String, AtomicInteger>();

    private LongSlots requestSizeSlots;
    private LongSlots responseSizeSlots;
    private LongSlots costSlots;

    public ReqStats(LongSlots requestSizeSlots, LongSlots responseSizeSlots, LongSlots costSlots) {
        this.requestSizeSlots = requestSizeSlots;
        this.responseSizeSlots = responseSizeSlots;
        this.costSlots = costSlots;
    }

    public void addReqInfo(long requestSize, long responseSize, long cost, String status) {
        count.incrementAndGet();

        totalCost.addAndGet(cost);
        setMin(minCost, cost);
        setMax(maxCost, cost);

        totalRequestSize.addAndGet(requestSize);
        setMin(minRequestSize, requestSize);
        setMax(maxRequestSize, requestSize);

        totalResponseSize.addAndGet(responseSize);
        setMin(minResponseSize, responseSize);
        setMax(maxResponseSize, responseSize);

        addCountMap(requestSizeSlots.getSlot(requestSize), requestSizeSlotCountMap);
        addCountMap(responseSizeSlots.getSlot(responseSize), responseSizeSlotCountMap);
        addCountMap(costSlots.getSlot(cost), costSlotCountMap);
        addCountMap(status, statusSlotCountMap);
    }

    public long getCount() {
        return count.get();
    }

    public long getAvgCost() {
        long c = count.get();
        if (c > 0) {
            return totalCost.get() / c;
        }
        return 0l;
    }

    public long getMinCost() {
        return minCost.get();
    }

    public long getMaxCost() {
        return maxCost.get();
    }

    public long getAvgRequestSize() {
        long c = count.get();
        if (c > 0) {
            return totalRequestSize.get() / c;
        }
        return 0l;
    }

    public long getMinRequestSize() {
        return minRequestSize.get();
    }

    public long getMaxRequestSize() {
        return maxRequestSize.get();
    }

    public long getAvgResponseSize() {
        long c = count.get();
        if (c > 0) {
            return totalResponseSize.get() / c;
        }
        return 0l;
    }

    public long getMinResponseSize() {
        return minResponseSize.get();
    }

    public long getMaxResponseSize() {
        return maxResponseSize.get();
    }

    public Map<String, AtomicInteger> getCostSlotCountMap() {
        return costSlotCountMap;
    }

    public Map<String, AtomicInteger> getResponseSizeSlotCountMap() {
        return responseSizeSlotCountMap;
    }

    public Map<String, AtomicInteger> getRequestSizeSlotCountMap() {
        return requestSizeSlotCountMap;
    }

    public Map<String, AtomicInteger> getStatusSlotCountMap() {
        return statusSlotCountMap;
    }

    private void addCountMap(String key, Map<String, AtomicInteger> map) {
        AtomicInteger count = map.get(key);
        if (count == null) {
            count = new AtomicInteger(0);
            map.put(key, count);
        }
        count.getAndIncrement();
    }

    private void setMax(AtomicLong maxHolder, long newValue) {
        while (true) {
            long max = maxHolder.get();
            if (max == 0 || max < newValue) {
                if(maxHolder.compareAndSet(max, newValue)){
                    break;
                }
            }else {
                break;
            }
        }
    }

    private void setMin(AtomicLong minHolder, long newValue) {
        while (true) {
            long min = minHolder.get();
            if (min == 0 || min > newValue) {
                if(minHolder.compareAndSet(min, newValue)){
                    break;
                }
            }else {
                break;
            }
        }
    }
}

