package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.entity.Slb;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
public interface SlbQuery {

    Slb get(String slbName) throws Exception;

    Slb getById(Long id) throws Exception;

    Slb getBySlbServer(String slbServerIp) throws Exception;

    List<Slb> batchGet(Long[] ids) throws Exception;

    List<Slb> getAll() throws Exception;

    List<Slb> getByGroups(Long[] groupIds) throws Exception;
}