package com.ctrip.zeus.util;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
public class LogConfigurator {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    private String appName;
    private String environment;

    public LogConfigurator(String appName, String environment) {
        this.appName = appName;
        this.environment = environment;
    }

    public void config() {
        logger.info("To reconfigure logback.");
        try {
            LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
            try {
                JoranConfigurator joranConfigurator = new JoranConfigurator();
                joranConfigurator.setContext(lc);
                lc.reset();

                InputStream inputStream = getConfigInputStream(appName + "-logback-" + environment + ".xml");
                if (inputStream == null) {
                    inputStream = getConfigInputStream("logback-" + environment + ".xml");
                }
                if (inputStream == null) {
                    inputStream = getConfigInputStream("logback.xml");
                }

                if (inputStream == null) {
                    throw new Exception("Can't find the logback config file.");
                }

                joranConfigurator.doConfigure(inputStream);

                logger.info("Reconfigure logback.");
            } catch (JoranException e) {
                logger.error("Error while reconfigure logback.", e);
            }
            StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
        } catch (Exception e) {
            logger.warn("Failed to reconfigure logback.", e);
        }
    }

    private InputStream getConfigInputStream(String configFileName) {
        return LogConfigurator.class.getClassLoader().getResourceAsStream(configFileName);
    }
}
