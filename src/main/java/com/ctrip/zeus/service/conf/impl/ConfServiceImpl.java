package com.ctrip.zeus.service.conf.impl;

import com.ctrip.zeus.service.conf.ActivateService;
import com.ctrip.zeus.service.conf.ConfService;
import org.springframework.stereotype.Service;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
@Service("confService")
public class ConfServiceImpl implements ConfService {
    @Resource
    private ActivateService activateService;

    @Override
    public void activate(List<String> slbNames, List<String> appNames) {
        try {
            for (String slbName : slbNames) {
                activateService.activeSlb(slbName);
            }
            for (String appName : appNames) {
                activateService.activeApp(appName);
            }
        } catch (DalException e) {
            throw new RuntimeException(e);
        }
    }
}
