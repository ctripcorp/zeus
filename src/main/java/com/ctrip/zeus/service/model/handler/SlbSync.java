package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.entity.Slb;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
public interface SlbSync {

    Long add(Slb slb) throws Exception;

    Long update(Slb slb) throws Exception;

    void updateVersion(Long slbId) throws Exception;

    int delete(Long slbId) throws Exception;
}
