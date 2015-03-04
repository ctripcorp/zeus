package com.ctrip.zeus.server;

import com.ctrip.zeus.util.LogConfigurator;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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
            loadConfiguration();
            configLog();

            init();

            logger.info("Has initialized the {}.", appName);
        } catch (Exception e) {
            logger.error("Failed to initialize the " + appName + ".", e);
            throw e;
        }
    }

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

    private void loadConfiguration() {
        System.setProperty(DynamicPropertyFactory.ENABLE_JMX, "true");

        // Loading properties via archaius.
        if (null != appName) {
            try {
                logger.info(String.format("Loading application properties with app id: %s and environment: %s", appName,
                        ConfigurationManager.getDeploymentContext().getDeploymentEnvironment()));
                ConfigurationManager.loadCascadedPropertiesFromResources(appName);
            } catch (IOException e) {
                logger.error(String.format(
                        "Failed to load properties for application id: %s and environment: %s. This is ok, if you do not have application level properties.",
                        appName,
                        ConfigurationManager.getDeploymentContext().getDeploymentEnvironment()), e);
            }
        } else {
            logger.warn(
                    "Application identifier not defined, skipping application level properties loading. You must set a property 'archaius.deployment.applicationId' to be able to load application level properties.");
        }
    }

    private void configLog() {
        logConfigurator = new LogConfigurator(appName, ConfigurationManager.getDeploymentContext().getDeploymentEnvironment());
        logConfigurator.config();
    }
}
