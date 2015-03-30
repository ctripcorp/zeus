package com.ctrip.zeus.service.nginx;

import com.ctrip.zeus.domain.NginxResponse;
import com.ctrip.zeus.domain.NginxServerStatus;
import com.ctrip.zeus.service.SlbException;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
public interface NginxService {
    NginxResponse load() throws SlbException;

    NginxServerStatus getStatus() throws SlbException;

    List<NginxResponse> loadAll() throws SlbException;

    List<NginxServerStatus> getStatusAll() throws SlbException;

}
