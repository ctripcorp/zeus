package com.ctrip.zeus.support;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.IEntity;
import com.ctrip.zeus.model.IVisitor;
import com.ctrip.zeus.model.entity.*;

import java.util.*;

/**
 * @author:xingchaowang
 * @date: 3/6/2015.
 */
public class DefaultDoBuilder implements IVisitor {
    private IVisitor m_visitor;
    private Map<Class<?>,List> m_map;

    public DefaultDoBuilder() {
        m_map = new HashMap<>();
        m_visitor = this;
    }

    public Map<Class<?>, List> build(IEntity<?> entity) {
        entity.accept(this);
        return m_map;
    }

    @Override
    public void visitApp(App application) {
        getList(AppDo.class).add(C.toAppDo(application));

        if (!application.getAppSlbs().isEmpty()) {
            for (AppSlb appSlb : application.getAppSlbs()) {
                appSlb.accept(m_visitor);
            }
        }

        if (application.getHealthCheck() != null) {
            application.getHealthCheck().accept(m_visitor);
        }

        if (application.getLoadBalancingMethod() != null) {
            application.getLoadBalancingMethod().accept(m_visitor);
        }

        if (!application.getAppServers().isEmpty()) {
            for (AppServer appServer : application.getAppServers()) {
                appServer.accept(m_visitor);
            }
        }
    }

    @Override
    public void visitAppServer(AppServer appServer) {
        getList(AppServerDo.class).add(C.toAppServerDo(appServer));

        if (appServer.getServer() != null) {
            appServer.getServer().accept(m_visitor);
        }
    }

    @Override
    public void visitAppSlb(AppSlb appSlb) {
        getList(AppServerDo.class).add(C.toAppSlbDo(appSlb));

        if (appSlb.getVirtualServer() != null) {
            appSlb.getVirtualServer().accept(m_visitor);
        }
    }

    @Override
    public void visitDomain(Domain domain) {
        getList(SlbDomainDo.class).add(C.toSlbDomainDo(domain));
    }

    @Override
    public void visitHealthCheck(HealthCheck healthCheck) {
        getList(AppHealthCheckDo.class).add(C.toAppHealthCheckDo(healthCheck));
    }

    @Override
    public void visitLoadBalancingMethod(LoadBalancingMethod loadBalancingMethod) {
        getList(AppLoadBalancingMethodDo.class).add(C.toAppLoadBalancingMethodDo(loadBalancingMethod));
    }

    @Override
    public void visitModel(Model model) {
        if (model.getSlb() != null) {
            model.getSlb().accept(m_visitor);
        }

        if (model.getApp() != null) {
            model.getApp().accept(m_visitor);
        }
    }

    @Override
    public void visitServer(Server server) {
        getList(ServerDo.class).add(C.toServerDo(server));
    }

    @Override
    public void visitSlb(Slb slbCluster) {
        getList(SlbDo.class).add(C.toSlbDo(slbCluster));

        if (!slbCluster.getVips().isEmpty()) {
            for (Vip vip : slbCluster.getVips()) {
                vip.accept(m_visitor);
            }
        }

        if (!slbCluster.getSlbServers().isEmpty()) {
            for (SlbServer slbServer : slbCluster.getSlbServers()) {
                slbServer.accept(m_visitor);
            }
        }

        if (!slbCluster.getVirtualServers().isEmpty()) {
            for (VirtualServer virtualServer : slbCluster.getVirtualServers()) {
                virtualServer.accept(m_visitor);
            }
        }
    }

    @Override
    public void visitSlbServer(SlbServer slbServer) {
        getList(SlbServerDo.class).add(C.toSlbServerDo(slbServer));
    }

    @Override
    public void visitVip(Vip vip) {
        getList(SlbVipDo.class).add(C.toSlbVipDo(vip));
    }

    @Override
    public void visitVirtualServer(VirtualServer virtualServer) {
        getList(SlbVirtualServerDo.class).add(C.toSlbVirtualServerDo(virtualServer));

        if (!virtualServer.getDomains().isEmpty()) {
            for (Domain domain : virtualServer.getDomains()) {
                domain.accept(m_visitor);
            }
        }
    }

    private List getList(Class<?> clazz) {
        List list = m_map.get(clazz);
        if (list == null) {
            list = new ArrayList();
            m_map.put(clazz, list);
        }
        return list;
    }
}
