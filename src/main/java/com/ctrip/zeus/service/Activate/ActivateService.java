package com.ctrip.zeus.service.Activate;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
public interface ActivateService {

    /**
     * update active data by slbname
     * @param slbname the slb name
     * @return
     * @throws Exception
     */
    public void activeSlb(String slbname) throws Exception;
    /**
     * update active data by slbname
     * @param appname the slb name
     * @return
     * @throws Exception
     */
    public void activeApp(String appname) throws Exception;

    /**
     * update active data by slbnames and appnames
     * @param slbNames the slb names
     * @param appNames the app names
     * @return
     * @throws Exception
     */
    public void activate(List<String> slbNames, List<String> appNames)throws Exception;

}
