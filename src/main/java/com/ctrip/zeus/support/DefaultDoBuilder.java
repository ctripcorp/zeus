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
    public void visitApplication(Application application) {
        getList(ApplicationDo.class).add(EntityDoConverter.toApplicationDo(application));

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
        getList(AppServerDo.class).add(EntityDoConverter.toAppServerDo(appServer));

        if (appServer.getServer() != null) {
            appServer.getServer().accept(m_visitor);
        }
    }

    @Override
    public void visitAppSlb(AppSlb appSlb) {
        getList(AppServerDo.class).add(EntityDoConverter.toAppSlbDo(appSlb));

        if (appSlb.getVirtualServer() != null) {
            appSlb.getVirtualServer().accept(m_visitor);
        }
    }

    @Override
    public void visitDomain(Domain domain) {
        getList(SlbDomainDo.class).add(EntityDoConverter.toSlbDomainDo(domain));
    }

    @Override
    public void visitHealthCheck(HealthCheck healthCheck) {
        getList(AppHealthCheckDo.class).add(EntityDoConverter.toAppHealthCheckDo(healthCheck));
    }

    @Override
    public void visitLoadBalancingMethod(LoadBalancingMethod loadBalancingMethod) {
        getList(AppLoadBalancingMethodDo.class).add(EntityDoConverter.toAppLoadBalancingMethodDo(loadBalancingMethod));
    }

    @Override
    public void visitModel(Model model) {
        if (model.getSlbCluster() != null) {
            model.getSlbCluster().accept(m_visitor);
        }

        if (model.getApplication() != null) {
            model.getApplication().accept(m_visitor);
        }
    }

    @Override
    public void visitServer(Server server) {
        getList(ServerDo.class).add(EntityDoConverter.toServerDo(server));
    }

    @Override
    public void visitSlbCluster(SlbCluster slbCluster) {
        getList(SlbClusterDo.class).add(EntityDoConverter.toSlbClusterDo(slbCluster));

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
        getList(SlbServerDo.class).add(EntityDoConverter.toSlbServerDo(slbServer));
    }

    @Override
    public void visitVip(Vip vip) {
        getList(SlbVipDo.class).add(EntityDoConverter.toSlbVipDo(vip));
    }

    @Override
    public void visitVirtualServer(VirtualServer virtualServer) {
        getList(SlbVirtualServerDo.class).add(EntityDoConverter.toSlbVirtualServerDo(virtualServer));
        if (virtualServer.getSlbCluster() != null) {
            virtualServer.getSlbCluster().accept(m_visitor);
        }

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
