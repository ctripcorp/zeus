package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class GroupVirtualServer {
    private Integer m_priority;

    private String m_path;

    private String m_name;

    private String m_rewrite;

    private String m_redirect;

    private String m_customConf;

    private VirtualServer m_virtualServer;

    private List<RouteRule> m_routeRules = new ArrayList<RouteRule>();

    public GroupVirtualServer() {
    }

    protected boolean equals(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        } else if (o2 == null) {
            return false;
        } else {
            return o1.equals(o2);
        }
    }



    public GroupVirtualServer addRouteRule(RouteRule routeRule) {
        m_routeRules.add(routeRule);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GroupVirtualServer) {
            GroupVirtualServer _o = (GroupVirtualServer) obj;

            if (!equals(m_priority, _o.getPriority())) {
                return false;
            }

            if (!equals(m_path, _o.getPath())) {
                return false;
            }

            if (!equals(m_name, _o.getName())) {
                return false;
            }

            if (!equals(m_rewrite, _o.getRewrite())) {
                return false;
            }

            if (!equals(m_redirect, _o.getRedirect())) {
                return false;
            }

            if (!equals(m_customConf, _o.getCustomConf())) {
                return false;
            }

            if (!equals(m_virtualServer, _o.getVirtualServer())) {
                return false;
            }

            if (!equals(m_routeRules, _o.getRouteRules())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getCustomConf() {
        return m_customConf;
    }

    public String getName() {
        return m_name;
    }

    public String getPath() {
        return m_path;
    }

    public Integer getPriority() {
        return m_priority;
    }

    public String getRedirect() {
        return m_redirect;
    }

    public String getRewrite() {
        return m_rewrite;
    }

    public List<RouteRule> getRouteRules() {
        return m_routeRules;
    }

    public VirtualServer getVirtualServer() {
        return m_virtualServer;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_priority == null ? 0 : m_priority.hashCode());
        hash = hash * 31 + (m_path == null ? 0 : m_path.hashCode());
        hash = hash * 31 + (m_name == null ? 0 : m_name.hashCode());
        hash = hash * 31 + (m_rewrite == null ? 0 : m_rewrite.hashCode());
        hash = hash * 31 + (m_redirect == null ? 0 : m_redirect.hashCode());
        hash = hash * 31 + (m_customConf == null ? 0 : m_customConf.hashCode());
        hash = hash * 31 + (m_virtualServer == null ? 0 : m_virtualServer.hashCode());
        hash = hash * 31 + (m_routeRules == null ? 0 : m_routeRules.hashCode());

        return hash;
    }


    public GroupVirtualServer setCustomConf(String customConf) {
        m_customConf = customConf;
        return this;
    }

    public GroupVirtualServer setName(String name) {
        m_name = name;
        return this;
    }

    public GroupVirtualServer setPath(String path) {
        m_path = path;
        return this;
    }

    public GroupVirtualServer setPriority(Integer priority) {
        m_priority = priority;
        return this;
    }

    public GroupVirtualServer setRedirect(String redirect) {
        m_redirect = redirect;
        return this;
    }

    public GroupVirtualServer setRewrite(String rewrite) {
        m_rewrite = rewrite;
        return this;
    }

    public GroupVirtualServer setVirtualServer(VirtualServer virtualServer) {
        m_virtualServer = virtualServer;
        return this;
    }

}
