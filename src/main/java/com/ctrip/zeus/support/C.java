package com.ctrip.zeus.support;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.*;

/**
 * @author:xingchaowang
 * @date: 3/6/2015.
 */
public class C {


    public static App toApp(AppDo d) {
        return new App()
                .setAppId(d.getAppId())
                .setName(d.getName());
    }

    public static AppServer toAppServer(AppServerDo d) {
        return new AppServer()
                .setIp(d.getIp())
                .setFailTimeout(d.getFailTimeout())
                .setMaxFails(d.getMaxFails())
                .setPort(d.getPort())
                .setWeight(d.getWeight());
    }

    public static AppSlb toAppSlb(AppSlbDo d) {
        return new AppSlb()
                .setPath(d.getPath());
    }

    public static Domain toDomain(SlbDomainDo d) {
        return new Domain()
                .setName(d.getName());
    }

    public static HealthCheck toHealthCheck(AppHealthCheckDo d) {
        return new HealthCheck()
                .setFails(d.getFails())
                .setIntervals(d.getIntervals())
                .setPasses(d.getPasses())
                .setUri(d.getUri());
    }

    public static LoadBalancingMethod toLoadBalancingMethod(AppLoadBalancingMethodDo d) {
        return new LoadBalancingMethod()
                .setType(d.getType())
                .setValue(d.getValue());
    }


    public static Slb toSlb(SlbDo d) {
        return new Slb()
                .setName(d.getName())
                .setNginxBin(d.getNginxBin())
                .setNginxConf(d.getNginxConf())
                .setNginxWorkerProcesses(d.getNginxWorkerProcesses())
                .setStatus(d.getStatus());
    }

    public static SlbServer toSlbServer(SlbServerDo d) {
        return new SlbServer()
                .setEnable(d.isEnable())
                .setHostName(d.getHostName())
                .setIp(d.getIp());
    }

    public static Vip toVip(SlbVipDo d) {
        return new Vip()
                .setIp(d.getIp());
    }

    public static VirtualServer toVirtualServer(SlbVirtualServerDo d) {
        return new VirtualServer()
                .setPort(d.getPort())
                .setName(d.getName())
                .setSsl(d.isIsSsl());
    }

    /*Entity to Do*/


    public static AppDo toAppDo(App e) {
        return new AppDo().setAppId(e.getAppId())
                .setName(e.getName());
    }

    public static AppServerDo toAppServerDo(AppServer e) {
        return new AppServerDo()
                .setIp(e.getIp())
                .setFailTimeout(e.getFailTimeout())
                .setMaxFails(e.getMaxFails())
                .setPort(e.getPort())
                .setWeight(e.getWeight());
    }

    public static AppSlbDo toAppSlbDo(AppSlb e) {
        return new AppSlbDo()
                .setSlbName(e.getSlbName())
                .setSlbVirtualServerName(e.getVirtualServer().getName())
                .setPath(e.getPath());
    }

    public static SlbDomainDo toSlbDomainDo(Domain e) {
        return new SlbDomainDo()
                .setName(e.getName());
    }

    public static AppHealthCheckDo toAppHealthCheckDo(HealthCheck e) {
        return new AppHealthCheckDo()
                .setUri(e.getUri())
                .setIntervals(e.getIntervals())
                .setFails(e.getFails())
                .setPasses(e.getPasses());
    }

    public static AppLoadBalancingMethodDo toAppLoadBalancingMethodDo(LoadBalancingMethod e) {
        return new AppLoadBalancingMethodDo()
                .setType(e.getType())
                .setValue(e.getValue());
    }

    public static SlbDo toSlbDo(Slb e) {
        return new SlbDo()
                .setName(e.getName())
                .setNginxBin(e.getNginxBin())
                .setNginxConf(e.getNginxConf())
                .setNginxWorkerProcesses(e.getNginxWorkerProcesses())
                .setStatus(e.getStatus());
    }

    public static SlbServerDo toSlbServerDo(SlbServer e) {
        return new SlbServerDo()
                .setHostName(e.getHostName())
                .setIp(e.getIp())
                .setEnable(e.getEnable());
    }

    public static SlbVipDo toSlbVipDo(Vip e) {
        return new SlbVipDo()
                .setIp(e.getIp());
    }

    public static SlbVirtualServerDo toSlbVirtualServerDo(VirtualServer e) {
        return new SlbVirtualServerDo()
                .setPort(e.getPort())
                .setIsSsl(e.isSsl())
                .setName(e.getName());
    }

}
