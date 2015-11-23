package com.ctrip.zeus.logstats;

/**
 * Created by zhoumy on 2015/11/18.
 */
public interface StatsDelegate<T> {

    void delegate(T input);
}
