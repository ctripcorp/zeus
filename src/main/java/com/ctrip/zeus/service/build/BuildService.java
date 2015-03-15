package com.ctrip.zeus.service.build;

import org.unidal.dal.jdbc.DalException;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
public interface BuildService {
    void build(String name) throws DalException;
}
