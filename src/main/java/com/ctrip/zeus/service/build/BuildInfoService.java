package com.ctrip.zeus.service.build;

import org.unidal.dal.jdbc.DalException;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
public interface BuildInfoService {
    int getTicket(String name) throws DalException;

    void updateTicket(String name, int ticket) throws DalException;
}
