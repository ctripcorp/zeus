package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Domain;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.handler.VirtualServerValidator;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2015/9/24.
 */
@Component("virtualServerModelValidator")
public class DefaultVirtualServerValidator implements VirtualServerValidator {
    private DynamicStringProperty portWhiteList = DynamicPropertyFactory.getInstance().getStringProperty("port.whitelist", "80,443");

    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private SlbVirtualServerDao slbVirtualServerDao;

    @Override
    public boolean exists(Long vsId) throws Exception {
        return slbVirtualServerDao.findByPK(vsId, SlbVirtualServerEntity.READSET_FULL) != null;
    }

    @Override
    public void validateVirtualServers(List<VirtualServer> virtualServers) throws Exception {
        Set<String> existingHost = new HashSet<>();
        for (VirtualServer virtualServer : virtualServers) {
            for (Domain domain : virtualServer.getDomains()) {
                if (!getPortWhiteList().contains(virtualServer.getPort())) {
                    throw new ValidationException("Port " + virtualServer.getPort() + " is not allowed.");
                }
                String key = domain.getName().toLowerCase() + ":" + virtualServer.getPort();
                if (existingHost.contains(key))
                    throw new ValidationException("Duplicate domain and port combination is found: " + key);
                else
                    existingHost.add(key);
            }
        }
    }

    @Override
    public void removable(VirtualServer virtualServer) throws Exception {
        if (groupCriteriaQuery.queryByVsId(virtualServer.getId()).size() > 0)
            throw new ValidationException("Virtual server with id " + virtualServer.getId() + " cannot be deleted. Dependencies exist.");
    }

    private Set<String> getPortWhiteList() {
        Set<String> result = new HashSet<>();
        String whiteList = portWhiteList.get();
        for (String s : whiteList.split(",")) {
            result.add(s.trim());
        }
        return result;
    }
}
