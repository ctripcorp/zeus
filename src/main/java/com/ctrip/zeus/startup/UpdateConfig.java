package com.ctrip.zeus.startup;

import com.ctrip.zeus.service.update.SlbServerConfManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Created by fanqq on 2016/3/22.
 */
@Component("updateConfig")
public class UpdateConfig {
    @Resource
    private SlbServerConfManager slbServerConfManager;

    private final Logger logger = LoggerFactory.getLogger(UpdateConfig.class);

    @PostConstruct
    private void init() {
        try {
            slbServerConfManager.update();
        } catch (Exception e) {
            logger.error("[StartUp] Update Config Failed.", e);
        }
    }
}
