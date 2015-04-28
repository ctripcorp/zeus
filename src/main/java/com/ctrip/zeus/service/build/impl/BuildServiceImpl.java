package com.ctrip.zeus.service.build.impl;

import com.ctrip.zeus.dal.core.NginxServerDao;
import com.ctrip.zeus.service.build.BuildInfoService;
import com.ctrip.zeus.service.build.BuildService;
import com.ctrip.zeus.service.build.NginxConfService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
@Service("buildService")
public class BuildServiceImpl implements BuildService {
    @Resource
    private BuildInfoService buildInfoService;

    @Resource
    private NginxConfService nginxConfService;

    @Resource
    private NginxServerDao nginxServerDao;

    @Override
    public boolean build(String slbname) {
        return false;
    }

    @Override
    public boolean build(String name , int ticket) throws Exception {
        int paddingTicket = buildInfoService.getPaddingTicket(name);
        ticket = paddingTicket>ticket?paddingTicket:ticket;
        if (!buildInfoService.updateTicket(name, ticket))
        {
            return false;
        }
        nginxConfService.build(name, ticket);
        return  true;
    }
}
