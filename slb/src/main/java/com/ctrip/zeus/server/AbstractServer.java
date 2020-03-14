package com.ctrip.zeus.server;

import com.ctrip.zeus.util.LogConfigurator;
import com.netflix.config.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
public abstract class AbstractServer implements Server {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    protected LogConfigurator logConfigurator;
    protected String appName;

    public AbstractServer() throws Exception {
        appName = ConfigurationManager.getDeploymentContext().getApplicationId();

        logger.info("Initializing the  {} ...", appName);
        try {
            preLoad();

            configLog();

            init();

            logger.info("Has initialized the {}.", appName);
        } catch (Exception e) {
            logger.error("Failed to initialize the " + appName + ".", e);
            throw e;
        }
    }

    protected abstract void preLoad();

    protected abstract void init() throws Exception;

    protected abstract void doStart() throws Exception;

    protected abstract void doClose() throws Exception;

    public void start() throws Exception {
        logger.info("Starting the  {} ...", appName);

        try {
            doStart();
        } catch (Exception e) {
            logger.error("Failed to start " + appName + ".", e);
            throw e;
        }

        logger.info("Started the {}.", appName);
    }

    public void close() {
        logger.info("Stopping the  {} ...", appName);

        try {
            doClose();
        } catch (Exception e) {
            logger.error("Error stopping httpServer ...", e);
        }
    }



    private void configLog() {
        logConfigurator = new LogConfigurator(appName, ConfigurationManager.getDeploymentContext().getDeploymentEnvironment());
        logConfigurator.config();
    }
}
