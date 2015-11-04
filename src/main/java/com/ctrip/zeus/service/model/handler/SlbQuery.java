package com.ctrip.zeus.service.model.handler;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
public interface SlbQuery {

    List<String> getSlbIps(Long slbId) throws Exception;
}