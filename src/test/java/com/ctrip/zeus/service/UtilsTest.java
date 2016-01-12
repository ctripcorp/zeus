package com.ctrip.zeus.service;

import com.ctrip.zeus.service.model.VersionUtils;
import org.junit.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by zhoumy on 2016/1/6.
 */
public class UtilsTest {
    @Test
    public void testIdVersionHashCollision() {
        Map<Integer, Integer> counter = new HashMap<>();
        long maxId = 999999;
        int maxVersion = 99;
        for (long i = 1; i < maxId; i++) {
            for (int j = 1; j < maxVersion; j++) {
                Integer key = VersionUtils.getHash(i, j);
                if (counter.containsKey(key)) {
                    int value = counter.get(key);
                    counter.put(key, value + 1);
                } else {
                    counter.put(key, 1);
                }
            }
        }
        HashSet<Integer> colcounter = new HashSet<>();
        for (Integer v : counter.values()) {
            if (colcounter.contains(v)) continue;
            else {
                colcounter.add(v);
                System.out.print(" " + v);
            }
        }
    }

    @Test
    public void testIdVersionHashCollision2() {
        int value1 = VersionUtils.getHash(1L, 2);
        int value2 = VersionUtils.getHash(2L, 1);
        System.out.println(value1 + "," + value2);
    }
}
