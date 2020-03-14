package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.model.Slb;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
public interface SlbSync {

    void add(Slb slb) throws Exception;

    void update(Slb slb) throws Exception;

    void updateStatus(List<Slb> slbs) throws Exception;

    int delete(Long slbId) throws Exception;
}
