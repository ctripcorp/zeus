package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class Slb {
    private Long m_id;

    private String m_name;

    private Integer m_version;

    private String m_nginxBin;

    private String m_nginxConf;

    private Integer m_nginxWorkerProcesses;

    private String m_status;

    private java.util.Date m_createdTime;

    private List<Vip> m_vips = new ArrayList<Vip>();

    private List<SlbServer> m_slbServers = new ArrayList<SlbServer>();

    private List<Rule> m_ruleSet = new ArrayList<Rule>();

    public Slb() {
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



    public Slb addRule(Rule rule) {
        m_ruleSet.add(rule);
        return this;
    }

    public Slb addSlbServer(SlbServer slbServer) {
        m_slbServers.add(slbServer);
        return this;
    }

    public Slb addVip(Vip vip) {
        m_vips.add(vip);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Slb) {
            Slb _o = (Slb) obj;

            if (!equals(m_id, _o.getId())) {
                return false;
            }

            if (!equals(m_name, _o.getName())) {
                return false;
            }

            if (!equals(m_version, _o.getVersion())) {
                return false;
            }

            if (!equals(m_nginxBin, _o.getNginxBin())) {
                return false;
            }

            if (!equals(m_nginxConf, _o.getNginxConf())) {
                return false;
            }

            if (!equals(m_nginxWorkerProcesses, _o.getNginxWorkerProcesses())) {
                return false;
            }

            if (!equals(m_status, _o.getStatus())) {
                return false;
            }

            if (!equals(m_createdTime, _o.getCreatedTime())) {
                return false;
            }

            if (!equals(m_vips, _o.getVips())) {
                return false;
            }

            if (!equals(m_slbServers, _o.getSlbServers())) {
                return false;
            }

            if (!equals(m_ruleSet, _o.getRuleSet())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public java.util.Date getCreatedTime() {
        return m_createdTime;
    }

    public Long getId() {
        return m_id;
    }

    public String getName() {
        return m_name;
    }

    public String getNginxBin() {
        return m_nginxBin;
    }

    public String getNginxConf() {
        return m_nginxConf;
    }

    public Integer getNginxWorkerProcesses() {
        return m_nginxWorkerProcesses;
    }

    public List<Rule> getRuleSet() {
        return m_ruleSet;
    }

    public List<SlbServer> getSlbServers() {
        return m_slbServers;
    }

    public String getStatus() {
        return m_status;
    }

    public Integer getVersion() {
        return m_version;
    }

    public List<Vip> getVips() {
        return m_vips;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());
        hash = hash * 31 + (m_name == null ? 0 : m_name.hashCode());
        hash = hash * 31 + (m_version == null ? 0 : m_version.hashCode());
        hash = hash * 31 + (m_nginxBin == null ? 0 : m_nginxBin.hashCode());
        hash = hash * 31 + (m_nginxConf == null ? 0 : m_nginxConf.hashCode());
        hash = hash * 31 + (m_nginxWorkerProcesses == null ? 0 : m_nginxWorkerProcesses.hashCode());
        hash = hash * 31 + (m_status == null ? 0 : m_status.hashCode());
        hash = hash * 31 + (m_createdTime == null ? 0 : m_createdTime.hashCode());
        hash = hash * 31 + (m_vips == null ? 0 : m_vips.hashCode());
        hash = hash * 31 + (m_slbServers == null ? 0 : m_slbServers.hashCode());
        hash = hash * 31 + (m_ruleSet == null ? 0 : m_ruleSet.hashCode());

        return hash;
    }


    public Slb setCreatedTime(java.util.Date createdTime) {
        m_createdTime = createdTime;
        return this;
    }

    public Slb setId(Long id) {
        m_id = id;
        return this;
    }

    public Slb setName(String name) {
        m_name = name;
        return this;
    }

    public Slb setNginxBin(String nginxBin) {
        m_nginxBin = nginxBin;
        return this;
    }

    public Slb setNginxConf(String nginxConf) {
        m_nginxConf = nginxConf;
        return this;
    }

    public Slb setNginxWorkerProcesses(Integer nginxWorkerProcesses) {
        m_nginxWorkerProcesses = nginxWorkerProcesses;
        return this;
    }

    public Slb setStatus(String status) {
        m_status = status;
        return this;
    }

    public Slb setVersion(Integer version) {
        m_version = version;
        return this;
    }

}
