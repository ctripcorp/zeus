package com.ctrip.zeus.support;

import com.ctrip.zeus.model.entity.*;

import java.util.List;
import java.util.Map;

/**
 * @author:xingchaowang
 * @date: 3/6/2015.
 */
public interface DoParser {

    public App parseApp(Map<Class<?>, List> map);

    public AppServer parseAppServer(Map<Class<?>, List> map);

    public AppSlb parseAppSlb(Map<Class<?>, List> map);

    public Domain parseDomain(Map<Class<?>, List> map);

    public HealthCheck parseHealthCheck(Map<Class<?>, List> map);

    public LoadBalancingMethod parseLoadBalancingMethod(Map<Class<?>, List> map);

    public Model parseModel(Map<Class<?>, List> map);

    public Server parseServer(Map<Class<?>, List> map);

    public Slb parseSlb(Map<Class<?>, List> map);

    public SlbServer parseSlbServer(Map<Class<?>, List> map);

    public Vip parseVip(Map<Class<?>, List> map);

    public VirtualServer parseVirtualServer(Map<Class<?>, List> map);

}
