package com.ctrip.zeus.service.build;


import com.ctrip.zeus.service.Repository;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
public interface BuildInfoService extends Repository {

    /**
     * get ticket by slb name
     * @param slbId the slb name
     * @return ticket number
     * @throws Exception
     */
    int getTicket(Long slbId) throws Exception;
}
