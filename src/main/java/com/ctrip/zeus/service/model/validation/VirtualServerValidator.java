package com.ctrip.zeus.service.model.validation;

import com.ctrip.zeus.model.entity.VirtualServer;

import java.util.List;

/**
 * Created by zhoumy on 2015/9/24.
 */
public interface VirtualServerValidator extends ModelValidator<VirtualServer> {

    boolean isActivated(Long vsId) throws Exception;

    void unite(List<VirtualServer> virtualServers) throws Exception;
}
