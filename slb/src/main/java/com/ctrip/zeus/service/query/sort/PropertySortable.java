package com.ctrip.zeus.service.query.sort;

/**
 * Created by zhoumy on 2016/11/2.
 */
public interface PropertySortable {

    Comparable getValue(String property);
}