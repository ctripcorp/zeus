package com.ctrip.zeus.service.build;

import java.util.Set;

/**
 * Created by lu.wang on 2016/4/19.
 */
public interface ConfigHandler {

    /**
     * For nginx conf generate
     */
    String getStringValue(String key, Long slbId, Long vsId, Long groupId, String defaultValue) throws Exception;
    int getIntValue(String key, Long slbId, Long vsId, Long groupId, int defaultValue) throws Exception;
    boolean getEnable(String key, Long slbId, Long vsId, Long groupId, boolean defaultValue) throws Exception;

    /**
     * Just for get dynamic config value
     */
    String getStringValue(String key, String defaultValue);
    boolean getEnable(String key, boolean defaultValue);

    /**
     * prefix.[type1] = value1, value2, value3
     * prefix.[type2] = value4, value5, value6
     *
     * return the type according to the value, if not found the value, return null
     *
     * @param prefix
     * @param types
     * @param value
     * @return
     */
    String getKeyType(String prefix, Set<String> types, String value);

}
