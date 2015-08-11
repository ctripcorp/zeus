package com.ctrip.zeus.service.build;


import com.ctrip.zeus.service.Repository;

import java.util.List;
import java.util.Set;

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

    /**
     * update ticket by slb name
     * @param slbId the slb name
     * @param ticket the ticket number
     * @return status
     * @throws Exception
     */
    boolean updateTicket(Long slbId, int ticket) throws Exception;


    /**
     * get current ticket by slb name
     * @param slbId the slb name
     * @return current ticket
     * @throws Exception
     */
    int getCurrentTicket(Long slbId) throws Exception;
    /**
     * get current ticket by slb name
     * @param slbId the slb name
     * @return padding ticket
     * @throws Exception
     */
    int getPaddingTicket(Long slbId) throws Exception;

    /**
     * reset current ticket by slb name
     * @param slbId the slb name
     * @return padding ticket
     * @throws Exception
     */
    int resetPaddingTicket(Long slbId) throws Exception;
}
