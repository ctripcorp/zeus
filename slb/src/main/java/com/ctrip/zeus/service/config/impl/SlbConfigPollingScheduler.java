package com.ctrip.zeus.service.config.impl;

import com.netflix.config.FixedDelayPollingScheduler;
import com.netflix.config.PolledConfigurationSource;
import org.apache.commons.configuration.Configuration;

/**
 * @Discription
 **/
public class SlbConfigPollingScheduler extends FixedDelayPollingScheduler {

    public SlbConfigPollingScheduler(int initialDelayMillis, int delayMillis, boolean ignoreDeletesFromSource) {
        super(initialDelayMillis, delayMillis, ignoreDeletesFromSource);
    }

    @Override
    protected synchronized void initialLoad(PolledConfigurationSource source, Configuration config) {
        try {
            super.initialLoad(source, config);
        } catch (Exception e) {

        }
    }
}
