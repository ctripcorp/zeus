package com.ctrip.zeus.support;

import com.ctrip.zeus.dal.core.ApplicationDo;
import com.ctrip.zeus.model.entity.*;

import java.util.List;
import java.util.Map;

/**
 * @author:xingchaowang
 * @date: 3/6/2015.
 */
public class DefaultDoParser implements DoParser{
    @Override
    public Application parseApplication(Map<Class<?>, List> map) {
        List<ApplicationDo> list = map.get(ApplicationDo.class);
        ApplicationDo ad = list.get(0);
        return null;
    }

    @Override
    public AppServer parseAppServer(Map<Class<?>, List> map) {
        return null;
    }

    @Override
    public AppSlb parseAppSlb(Map<Class<?>, List> map) {
        return null;
    }

    @Override
    public Domain parseDomain(Map<Class<?>, List> map) {
        return null;
    }

    @Override
    public HealthCheck parseHealthCheck(Map<Class<?>, List> map) {
        return null;
    }

    @Override
    public LoadBalancingMethod parseLoadBalancingMethod(Map<Class<?>, List> map) {
        return null;
    }

    @Override
    public Model parseModel(Map<Class<?>, List> map) {
        return null;
    }

    @Override
    public Server parseServer(Map<Class<?>, List> map) {
        return null;
    }

    @Override
    public SlbCluster parseSlbCluster(Map<Class<?>, List> map) {
        return null;
    }

    @Override
    public SlbServer parseSlbServer(Map<Class<?>, List> map) {
        return null;
    }

    @Override
    public Vip parseVip(Map<Class<?>, List> map) {
        return null;
    }

    @Override
    public VirtualServer parseVirtualServer(Map<Class<?>, List> map) {
        return null;
    }
}
