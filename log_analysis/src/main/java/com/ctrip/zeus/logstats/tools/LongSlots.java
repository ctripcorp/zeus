package com.ctrip.zeus.logstats.tools;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LongSlots {
    private Map<Long, String> nameMap = new HashMap<>();
    private Long[] scales = new Long[0];
    private String outOfMax = "";
    private volatile boolean needRefreshScales = false;

    public LongSlots() {}

    public LongSlots(String outOfMax) {
        this.outOfMax = outOfMax;
    }

    synchronized public LongSlots slot(String name, Long upperLimit){
        nameMap.put(upperLimit, name);
        needRefreshScales = true;
        return this;
    }

    synchronized public LongSlots refreshScales() {
        if (needRefreshScales) {
            Long[] ss = nameMap.keySet().toArray(new Long[0]);
            Arrays.sort(ss);
            scales = ss;
            needRefreshScales = false;
        }
        return this;
    }

    public String getSlot(Long value){
        if (needRefreshScales) {
            refreshScales();
        }
        for (Long scale : scales) {
            if (value <= scale) {
                return nameMap.get(scale);
            }
        }

        return outOfMax;
    }
}
