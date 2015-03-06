package com.ctrip.zeus.support;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.*;

/**
 * @author:xingchaowang
 * @date: 3/6/2015.
 */
public class EntityDoConverter {


    public static Application toApplication(ApplicationDo d) {
        return new Application()
                .setAppId(d.getAppId())
                .setName(d.getName());
    }

    public static AppServer toAppServer(AppServerDo d) {
        return new AppServer()
                .setEnable(d.isEnable())
                .setFailTimeout(d.getFailTimeout())
                .setHealthy(d.isHealthy())
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
                .setName(d.getName())
                .setPort(d.getPort());
    }

    public static HealthCheck toHealthCheck(AppHealthCheckDo d) {
        return new HealthCheck()
                .setFails(d.getFails())
                .setInterval(d.getInterval())
                .setPasses(d.getPasses())
                .setUri(d.getUri());
    }

    public static LoadBalancingMethod toLoadBalancingMethod(AppLoadBalancingMethodDo d) {
        return new LoadBalancingMethod()
                .setType(d.getType())
                .setValue(d.getValue());
    }


    public static Server toServer(ServerDo d) {
        return new Server()
                .setHostName(d.getHostName())
                .setIp(d.getIp())
                .setUp(d.isUp());
    }

    public static SlbCluster toSlbCluster(SlbClusterDo d) {
        return new SlbCluster()
                .setName(d.getName())
                .setNginxBin(d.getNginxBin())
                .setNginxConf(d.getNginxConf())
                .setNginxWorkerProcesses(d.getNginxWorkerProcesses());
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
                .setName(d.getName())
                .setSsl(d.isIsSsl());
    }

    /*Entity to Do*/


    public static ApplicationDo toApplicationDo(Application e) {
        return new ApplicationDo().setAppId(e.getAppId())
                .setName(e.getName());
    }

    public static AppServerDo toAppServerDo(AppServer e) {
         return new AppServerDo()
                .setEnable(e.getEnable())
                .setFailTimeout(e.getFailTimeout())
                .setHealthy(e.getHealthy())
                .setMaxFails(e.getMaxFails())
                .setPort(e.getPort())
                .setWeight(e.getWeight());
    }

    public static AppSlbDo toAppSlbDo(AppSlb e) {
        return new AppSlbDo()
                .setPath(e.getPath());
    }

    public static SlbDomainDo toSlbDomainDo(Domain e) {
        return new SlbDomainDo()
                .setName(e.getName())
                .setPort(e.getPort());
    }

    public static AppHealthCheckDo toAppHealthCheckDo(HealthCheck e) {
        return new AppHealthCheckDo()
                .setUri(e.getUri())
                .setId(e.getInterval())
                .setFails(e.getFails())
                .setPasses(e.getPasses());
    }

    public static AppLoadBalancingMethodDo toAppLoadBalancingMethodDo(LoadBalancingMethod e) {
        return new AppLoadBalancingMethodDo()
                .setType(e.getType())
                .setValue(e.getValue());
    }

    public static ServerDo toServerDo(Server e) {
        return new ServerDo()
                .setHostName(e.getHostName())
                .setIp(e.getIp())
                .setUp(e.getUp());
    }

    public static SlbClusterDo toSlbClusterDo(SlbCluster e) {
        return new SlbClusterDo()
                .setName(e.getName())
                .setNginxBin(e.getNginxBin())
                .setNginxConf(e.getNginxConf())
                .setNginxWorkerProcesses(e.getNginxWorkerProcesses());
    }

    public static SlbServerDo toSlbServerDo(SlbServer e) {
        return new SlbServerDo()
                .setHostName(e.getHostName())
                .setIp(e.getIp());
    }

    public static SlbVipDo toSlbVipDo(Vip e) {
        return new SlbVipDo()
                .setIp(e.getIp());
    }

    public static SlbVirtualServerDo toSlbVirtualServerDo(VirtualServer e) {
        return new SlbVirtualServerDo()
                .setIsSsl(e.isSsl())
                .setName(e.getName());
    }

}
