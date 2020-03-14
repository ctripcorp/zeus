package com.ctrip.zeus.config;

public interface ConfigValueService {
    /**
     * @return App owner's default mail address
     */
    String getAppDefaultOwnerMail();

    /**
     * @return Slb portal address
     */
    String getSlbPortalUrl();

    /**
     * @return Team mail address
     */
    String getTeamMail();

    String getAgentApi();
}
