package com.ctrip.zeus.service.query.filter;

import java.util.Set;

/**
 * Created by zhoumy on 2015/10/13.
 */
public interface FilterSet<T extends Comparable> {

    boolean shouldFilter() throws Exception;

    Set<T> filter() throws Exception;
}