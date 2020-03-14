package com.ctrip.zeus.service.update;

import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.springframework.stereotype.Component;

/**
 * @Discription
 **/
@Component
public class AgentApiRefactorSwitches {

    public boolean isSwitchOn(Long slbId) {
        DynamicBooleanProperty enable = DynamicPropertyFactory.getInstance().getBooleanProperty("agent.refactor.fetch.from.api.enable" + slbId, false);
        DynamicBooleanProperty enableAll = DynamicPropertyFactory.getInstance().getBooleanProperty("agent.refactor.fetch.from.api.enable.all", false);

        return enable.get() || enableAll.get();
    }
}
