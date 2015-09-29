package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.GroupServer;
import com.ctrip.zeus.service.model.handler.GroupServerValidator;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zhoumy on 2015/9/28.
 */
@Component("groupServerModelValidator")
public class DefaultGroupServerValidator implements GroupServerValidator {
    @Override
    public void validateGroupServers(List<GroupServer> groupServers) throws Exception {
        Set<String> check = new HashSet<>();
        for (GroupServer groupServer : groupServers) {
            if (groupServer.getIp() == null || groupServer.getIp().isEmpty() ||
                    groupServer.getPort() == null)
                throw new ValidationException("Group server ip and port cannot be null.");
            String id = groupServer.getIp() + ":" + groupServer.getPort();
            if (!check.add(id))
                throw new ValidationException("Duplicate combination of ip and port " + id + " is found in group server list.");
        }
    }
}
