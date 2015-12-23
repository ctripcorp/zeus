package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.service.Repository;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
public interface SlbRepository extends Repository {

    List<Slb> list(Long[] slbIds) throws Exception;

    Slb getById(Long slbId) throws Exception;

    Slb add(Slb slb) throws Exception;

    Slb update(Slb slb) throws Exception;

    int delete(Long slbId) throws Exception;

    @Deprecated
    List<Long> portSlbRel() throws Exception;

    @Deprecated
    List<Slb> list() throws Exception;

    @Deprecated
    Slb get(String slbName) throws Exception;

    @Deprecated
    Slb getBySlbServer(String slbServerIp) throws Exception;

    @Deprecated
    Slb getByVirtualServer(Long virtualServerId) throws Exception;

    @Deprecated
    List<Slb> listByGroupServer(String groupServerIp) throws Exception;

    @Deprecated
    List<Slb> listByGroups(Long[] groupIds) throws Exception;
}
