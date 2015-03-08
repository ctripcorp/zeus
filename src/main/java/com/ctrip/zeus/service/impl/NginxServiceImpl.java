package com.ctrip.zeus.service.impl;

import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.service.AppRepository;
import com.ctrip.zeus.service.NginxService;
import com.ctrip.zeus.service.SlbRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
@Service("nginxService")
public class NginxServiceImpl implements NginxService {
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private AppRepository appRepository;

    @Override
    public void load() {
        Slb slb = slbRepository.get("default");
    }
}
