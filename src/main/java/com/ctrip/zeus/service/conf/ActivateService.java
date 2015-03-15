package com.ctrip.zeus.service.conf;

import org.unidal.dal.jdbc.DalException;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
public interface ActivateService {

    public void activeSlb(String name) throws DalException;
    public void activeApp(String name) throws DalException;

}
