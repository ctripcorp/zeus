package com.ctrip.zeus.service.status.handler;

import com.ctrip.zeus.dal.core.StatusServerDo;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/14/2015.
 */
public interface StatusServerService {

    List<StatusServerDo> list() throws Exception;

    List<StatusServerDo> listAllDown() throws Exception;

    List<StatusServerDo> listByIp(String ip) throws Exception;

    void updateStatusServer(StatusServerDo statusServerDo) throws Exception;
}
