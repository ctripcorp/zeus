package com.ctrip.zeus.service.config.impl;

import com.ctrip.zeus.server.config.SlbConfigurationSource;
import com.netflix.config.AbstractPollingScheduler;
import com.netflix.config.DynamicConfiguration;
import com.netflix.config.DynamicURLConfiguration;
import com.netflix.config.sources.URLConfigurationSource;

/**
 * @Discription
 **/
public class SlbConfigConfiguration extends DynamicConfiguration {

    public SlbConfigConfiguration(int initialDelayMillis, int delayMillis, boolean ignoreDeletesFromSource,
                                  String... urls) {
        super(new URLConfigurationSource(urls), new SlbConfigPollingScheduler(initialDelayMillis, delayMillis, ignoreDeletesFromSource));
    }
}
