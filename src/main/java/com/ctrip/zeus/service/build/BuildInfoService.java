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
     * @param slbname the slb name
     * @return ticket number
     * @throws Exception
     */
    int getTicket(String slbname) throws Exception;

    /**
     * update ticket by slb name
     * @param slbname the slb name
     * @param ticket the ticket number
     * @return status
     * @throws Exception
     */
    boolean updateTicket(String slbname, int ticket) throws Exception;

    /**
     * get needed slb names  by slb name and app names
     * @param slbname the slb name list
     * @param appname the app name list
     * @return slb name set
     * @throws Exception
     */
    Set<String> getAllNeededSlb(List<String> slbname, List<String> appname) throws Exception;

    /**
     * get current ticket by slb name
     * @param slbname the slb name
     * @return current ticket
     * @throws Exception
     */
    int getCurrentTicket(String slbname) throws Exception;
}
