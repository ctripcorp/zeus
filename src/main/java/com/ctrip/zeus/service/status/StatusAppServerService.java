package com.ctrip.zeus.service.status;

import com.ctrip.zeus.dal.core.StatusAppServerDo;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/14/2015.
 */
public interface StatusAppServerService {

    List<StatusAppServerDo> list() throws Exception;

    List<StatusAppServerDo> listAllDownBySlbName(String slbName) throws Exception;

    List<StatusAppServerDo> listByAppName(String appName) throws Exception;

    List<StatusAppServerDo> listByServer(String ip) throws Exception;

    void updateStatusAppServer(StatusAppServerDo d) throws Exception;
}
