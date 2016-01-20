package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.service.Repository;

import java.util.List;
import java.util.Set;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
public interface SlbRepository extends Repository {

    List<Slb> list(Long[] slbIds) throws Exception;

    List<Slb> list(IdVersion[] keys) throws Exception;

    Slb getById(Long slbId) throws Exception;

    Slb getByKey(IdVersion key) throws Exception;

    Slb add(Slb slb) throws Exception;

    Slb update(Slb slb) throws Exception;

    int delete(Long slbId) throws Exception;

    void updateStatus(Slb[] slbs) throws Exception;

    @Deprecated
    Set<Long> port(Long[] slbId) throws Exception;
}
