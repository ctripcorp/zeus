package com.ctrip.zeus.service.nginx;

import com.ctrip.zeus.nginx.NginxResponse;
import com.ctrip.zeus.nginx.NginxServerStatus;
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
