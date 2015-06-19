package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.entity.GroupSlb;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import org.unidal.dal.jdbc.DalException;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
public interface SlbQuery {

    Slb get(String slbName) throws DalException;

    Slb getById(Long id) throws DalException;

    Slb getBySlbServer(String slbServerIp) throws DalException;

    Slb getByVirtualServer(Long virtualServerId) throws DalException;

    VirtualServer getBySlbAndName(String slbName, String virtualServerName) throws DalException;

    List<Slb> getAll() throws DalException;

    List<Slb> getByGroupServer(String groupServerIp) throws DalException;

    List<Slb> getByGroups(Long[] groupIds) throws DalException;

    List<Slb> getByGroupServerAndGroup(String groupServerIp, Long groupId) throws DalException;

    List<String> getGroupServersBySlb(String slbName) throws DalException;

    List<GroupSlb> getGroupSlbsByGroups(Long[] groupIds) throws DalException;

    List<GroupSlb> getGroupSlbsBySlb(Long slbId) throws DalException;
}