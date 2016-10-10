package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.VirtualServer;

import java.util.List;

/**
 * Created by zhoumy on 2015/9/24.
 */
public interface VirtualServerValidator {

    boolean exists(Long vsId) throws Exception;

    boolean isActivated(Long vsId) throws Exception;

    void unite(List<VirtualServer> virtualServers) throws Exception;

    void removable(Long vsId) throws Exception;

    void validate(VirtualServer virtualServer) throws ValidationException;
}
