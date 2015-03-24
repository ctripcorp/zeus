package com.ctrip.zeus.service.model;

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

    List<Slb> getByNames(String[] names) throws DalException;

    List<Slb> getByServer(String serverIp) throws DalException;

    List<Slb> getByMemberAndAppName(String memberIp, String[] appNames) throws DalException;

    List<Slb> getAll() throws DalException;
}