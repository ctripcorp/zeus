package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.entity.GroupServer;

import java.util.List;

/**
 * Created by zhoumy on 2015/9/28.
 */
public interface GroupServerValidator {

    void validateGroupServers(List<GroupServer> groupServers) throws Exception;
}
