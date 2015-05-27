package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.entity.GroupSlb;
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

    List<Slb> getByGroupServer(String appServerIp) throws DalException;

    List<Slb> getByGroupIds(long[] groupIds) throws DalException;
    List<Slb> getByGroupNames(String[] grpupNames) throws DalException;

    List<Slb> getByGroupServerAndGroupName(String appServerIp, String appName) throws DalException;

    List<String> getGroupServersBySlb(String slbName) throws DalException;

    List<GroupSlb> getGroupSlbsByGroups(long[] groupIds) throws DalException;

    List<GroupSlb> getGroupSlbsBySlb(long slbId) throws DalException;
}