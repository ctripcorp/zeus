package com.ctrip.zeus.service.clean;

/**
 * Created by fanqq on 2015/10/20.
 */
public interface CleanFilter {
    /**
     * Filter runner.
     */
    public void runFilter()throws Exception;

    /**
     * return interval . Unit for hours. never run if return 0 .
     */
    public int interval();
}
