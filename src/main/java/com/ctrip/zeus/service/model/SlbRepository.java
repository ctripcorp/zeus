package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.service.Repository;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
public interface SlbRepository extends Repository {

    List<Slb> list() throws Exception;

    Slb getById(Long slbId) throws Exception;

    Slb get(String slbName) throws Exception;

    Slb getBySlbServer(String slbServerIp) throws Exception;

    Slb getByVirtualServer(Long virtualServerId) throws Exception;

    List<Slb> listByGroupServer(String groupServerIp) throws Exception;

    List<Slb> listByGroups(Long[] groupIds) throws Exception;

    Slb add(Slb slb) throws Exception;

    Slb update(Slb slb) throws Exception;

    int delete(Long slbId) throws Exception;

    Slb updateVersion(Long slbId) throws Exception;
}
