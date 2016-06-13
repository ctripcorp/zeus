package com.ctrip.zeus.logstats.common;

/**
 * Created by mengyizhou on 10/18/15.
 */
public interface LineFormat<T, C> {
    /**
     * Defines log format. It recognizes and reads key information where $xxx is specified.
     * Separator is require between 2 continuous variable.
     *
     * @return
     */
    String getFormat();

    T getEngine();

    String[] getKeys();

    LineFormat setFormat(String format);

    LineFormat registerComponentForKey(String key, C component);

    LineFormat generate();
}
