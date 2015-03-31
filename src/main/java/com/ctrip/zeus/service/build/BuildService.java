package com.ctrip.zeus.service.build;


/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
public interface BuildService {
    void build(String name) throws Exception;
    /**
     * build config by slb name and ticket number
     * @param slbname the slb name
     * @param ticket the ticket number
     * @return status
     * @throws Exception
     */
    boolean build(String slbname, int ticket) throws Exception;
}
