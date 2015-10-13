package com.ctrip.zeus.restful.filter;

import java.util.Set;

/**
 * Created by zhoumy on 2015/10/13.
 */
public interface FilterSet<T extends Comparable> {
    Set<T> filter(Set<T> input) throws Exception;
}
