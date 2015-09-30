package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.entity.Slb;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
public interface SlbSync {

    void add(Slb slb) throws Exception;

    void update(Slb slb) throws Exception;

    void updateVersion(Long slbId) throws Exception;

    int delete(Long slbId) throws Exception;

    @Deprecated
    List<Long> port(Slb[] slbs) throws Exception;

    @Deprecated
    void port(Slb slb) throws Exception;
}
