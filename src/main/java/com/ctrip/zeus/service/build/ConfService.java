package com.ctrip.zeus.service.build;

/**
 * Created by lu.wang on 2016/4/19.
 */
public interface ConfService {

    String getStringValue(String key, Long slbId, Long vsId, Long groupId, String defaultValue) throws Exception;
    int getIntValue(String key, Long slbId, Long vsId, Long groupId, int defaultValue) throws Exception;
    boolean getEnable(String key, Long slbId, Long vsId, Long groupId, boolean defaultValue) throws Exception;

}
