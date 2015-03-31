package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.entity.AppSlb;
import com.ctrip.zeus.model.entity.Slb;
import org.unidal.dal.jdbc.DalException;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
public interface SlbQuery {
    Slb get(String slbName) throws DalException;

    Slb getById(long id) throws DalException;

    Slb getBySlbServer(String slbServerIp) throws DalException;

    List<Slb> getAll() throws DalException;

    List<Slb> getByAppServer(String appServerIp) throws DalException;

    List<Slb> getByAppNames(String[] appNames) throws DalException;

    List<Slb> getByAppServerAndAppName(String appServerIp, String appName) throws DalException;

    List<String> getAppServersBySlb(String slbName) throws DalException;

    List<AppSlb> getAppSlbsByApps(String[] appNames) throws DalException;

    List<AppSlb> getAppSlbsBySlb(String slbName) throws DalException;
}