package com.ctrip.zeus.logstats.common;

/**
 * Created by mengyizhou on 10/18/15.
 */
public interface LineFormat {
    /**
     * Defines log format. It recognizes and reads key information where $xxx is specified.
     * Separator is require between 2 continuous variable.
     * @return
     */
    String getFormat();
}
