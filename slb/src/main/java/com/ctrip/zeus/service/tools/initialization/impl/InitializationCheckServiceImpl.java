package com.ctrip.zeus.service.tools.initialization.impl;

import com.ctrip.zeus.service.tools.initialization.InitializationCheckService;
import com.ctrip.zeus.util.MySQLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Set;

/**
 * @Discription
 **/
@Component("initializationCheckService")
public class InitializationCheckServiceImpl implements InitializationCheckService {
    
    @Resource
    private MySQLUtils mySQLUtils;

    private static InitializationCheckServiceImpl instance;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static boolean initialized = false;

    @PostConstruct
    public void init() {
        check();
        instance = this;
    }

    public static boolean staticIsInitialized() {
        if (!initialized) {
            instance.check();
        }
        return initialized;
    }

    @Override
    public void check() {
        try {
            Set<String> existedTables = mySQLUtils.getTables(null);

            if (existedTables != null && existedTables.contains("slb_slb")) {
                initialized = true;
            }
        } catch (Exception e) {
            logger.error("Exception happened when connecting to datasource. Msg: " + e.getMessage());
            initialized = false;
        }
    }
}
