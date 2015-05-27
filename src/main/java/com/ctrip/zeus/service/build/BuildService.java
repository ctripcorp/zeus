package com.ctrip.zeus.service.build;


import com.ctrip.zeus.service.Repository;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
public interface BuildService extends Repository {
    boolean build(String slbname);
    /**
     * build config by slb name and ticket number
     * @param slbId the slb id
     * @param ticket the ticket number
     * @return status
     * @throws Exception
     */
    boolean build(Long slbId, int ticket) throws Exception;
}
