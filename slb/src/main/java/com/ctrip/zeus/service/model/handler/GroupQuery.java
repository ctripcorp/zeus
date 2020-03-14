package com.ctrip.zeus.service.model.handler;

/**
 * Created by zhoumy on 2016/3/25.
 */
public interface GroupQuery {
    String getAppId(Long groupId) throws Exception;

    String getGroupName(Long groupId) throws Exception;
}
