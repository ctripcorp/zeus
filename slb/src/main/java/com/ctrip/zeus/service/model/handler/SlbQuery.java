package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.model.SlbServer;

import java.util.List;
import java.util.Map;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
public interface SlbQuery {

    List<String> getSlbIps(Long slbId) throws Exception;

    Map<Long, List<SlbServer>> getServersBySlb() throws Exception;
}