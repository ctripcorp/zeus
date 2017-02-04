package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupList;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.service.model.common.LocationEntry;
import com.ctrip.zeus.service.model.common.MetaType;
import com.ctrip.zeus.service.model.common.ValidationContext;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.ctrip.zeus.util.TimerUtils;

import java.io.InputStream;
import java.util.*;

/**
 * Created by zhoumy on 2017/2/4.
 */
public class PathValidationV2PerformanceTest {

    public static void main(String[] args) {
        PathValidator pathValidator = new PathValidator();
        Map<Long, List<LocationEntry>> cachedData = filterLocationEntries();
        doPerformanceTest(pathValidator, cachedData);
        doCorrectnessTest(pathValidator, cachedData);
    }

    private static void doCorrectnessTest(PathValidator pathValidator, Map<Long, List<LocationEntry>> cachedData) {
        for (Map.Entry<Long, List<LocationEntry>> e : cachedData.entrySet()) {
            ValidationContext context = new ValidationContext();
            pathValidator.checkOverlapRestricition(e.getValue(), context);
            if (!context.shouldProceed()) {
                System.out.println("Locations on virtual server " + e.getKey() + " breaks path overlap rules.");
                for (Map.Entry<String, String> r : context.getErrors().entrySet()) {
                    System.out.printf("\r\t%-10s : %s\n", r.getKey(), r.getValue());
                }
            }
        }
    }

    private static void doPerformanceTest(PathValidator pathValidator, Map<Long, List<LocationEntry>> cachedData) {
        Map<Integer, Integer> tick = new HashMap<>();
        Map<Integer, Double> result = new TreeMap<>();
        for (Map.Entry<Long, List<LocationEntry>> e : cachedData.entrySet()) {
            long start = System.nanoTime();
            ValidationContext context = new ValidationContext();
            for (int i = 0; i < 1000; i++) {
                pathValidator.checkOverlapRestricition(e.getValue(), context);
            }
            double avg = TimerUtils.nanoToMilli(System.nanoTime() - start) / 1000.0;

            int size = e.getValue().size();

            Integer tv = tick.get(size);
            if (tv == null) {
                tv = 1;
            } else {
                tv += 1;
            }
            tick.put(size, tv);

            Double rv = result.get(size);
            if (rv == null) {
                rv = avg;
            } else {
                rv += avg;
            }
            result.put(size, rv);
        }

        for (Map.Entry<Integer, Double> e : result.entrySet()) {
            System.out.println("Path validation for " + e.getKey() + " location entries costs " + String.format("%.2f", e.getValue() / tick.get(e.getKey())) + " ms.");
        }
    }

    private static Map<Long, List<LocationEntry>> filterLocationEntries() {
        Map<Long, List<LocationEntry>> result = new HashMap<>();
        InputStream sourceFile = PathValidationV2PerformanceTest.class.getClassLoader().getResourceAsStream("com/ctrip/zeus/service/model/groups.json");
        for (Group g : ObjectJsonParser.parse(sourceFile, GroupList.class).getGroups()) {
            for (GroupVirtualServer gvs : g.getGroupVirtualServers()) {
                List<LocationEntry> v = result.get(gvs.getVirtualServer().getId());
                if (v == null) {
                    v = new ArrayList<>();
                    result.put(gvs.getVirtualServer().getId(), v);
                }
                v.add(new LocationEntry().setEntryId(g.getId()).setEntryType(MetaType.GROUP).setPath(gvs.getPath()).setPriority(gvs.getPriority()).setVsId(gvs.getVirtualServer().getId()));
            }
        }
        return result;
    }
}