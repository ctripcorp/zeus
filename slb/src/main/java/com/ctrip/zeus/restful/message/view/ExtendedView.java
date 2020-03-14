package com.ctrip.zeus.restful.message.view;

import com.ctrip.zeus.model.Certificate;
import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.service.query.sort.PropertySortable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

;

/**
 * Created by zhoumy on 2016/7/25.
 */
public interface ExtendedView<T> {

    Long getId();

    void setTags(List<String> tags);

    void setProperties(List<Property> properties);

    @JsonView(ViewConstraints.Extended.class)
    List<String> getTags();

    @JsonView(ViewConstraints.Extended.class)
    List<Property> getProperties();

    @JsonIgnore
    T getInstance();

    class ExtendedGroup extends GroupView implements ExtendedView<Group>, PropertySortable {
        private List<String> tags;
        private List<Property> properties;
        private Group instance;

        public ExtendedGroup() {
            this(new Group());
        }

        public ExtendedGroup(Group instance) {
            this.instance = instance;
        }

        @Override
        public Long getId() {
            return instance.getId();
        }

        @Override
        public String getName() {
            return instance.getName();
        }

        @Override
        public String getType() {
            return instance.getType();
        }

        @Override
        public String getAppId() {
            return instance.getAppId();
        }

        @Override
        HealthCheck getHealthCheck() {
            return instance.getHealthCheck();
        }

        @Override
        LoadBalancingMethod getLoadBalancingMethod() {
            return instance.getLoadBalancingMethod();
        }

        @Override
        Boolean getSsl() {
            return instance.getSsl();
        }

        @Override
        Integer getVersion() {
            return instance.getVersion();
        }

        @Override
        Boolean getVirtual() {
            return instance.getVirtual();
        }

        @Override
        void setId(Long id) {
            instance.setId(id);
        }

        @Override
        void setName(String name) {
            instance.setName(name);
        }

        @Override
        void setType(String type) {
            instance.setType(type);
        }

        @Override
        void setAppId(String appId) {
            instance.setAppId(appId);
        }

        @Override
        void setSsl(Boolean ssl) {
            instance.setSsl(ssl);
        }

        @Override
        void setVersion(Integer version) {
            instance.setVersion(version);
        }

        @Override
        void setHealthCheck(HealthCheck hc) {
            instance.setHealthCheck(hc);
        }

        @Override
        void setLoadBalancingMethod(LoadBalancingMethod loadBalancingMethod) {
            instance.setLoadBalancingMethod(loadBalancingMethod);
        }

        @Override
        void setGroupServers(List<GroupServer> servers) {
            instance.getGroupServers().clear();
            instance.getGroupServers().addAll(servers);
        }

        @Override
        void setRuleSet(List<Rule> rules) {
            instance.getRuleSet().clear();
            instance.getRuleSet().addAll(rules);
        }

        @Override
        void setCreatedTime(Date date) {
            instance.setCreatedTime(date);
        }

        @Override
        void setGroupVirtualServers(List<GroupVirtualServer> gvs) {
            instance.getGroupVirtualServers().clear();
            instance.getGroupVirtualServers().addAll(gvs);
        }

        @Override
        void setVirtual(Boolean virtual) {
            instance.setVirtual(virtual);
        }

        @Override
        List<GroupServer> getGroupServers() {
            return instance.getGroupServers();
        }

        @Override
        Date getCreatedTime() {
            return instance.getCreatedTime();
        }

        @Override
        List<GroupVirtualServer> getGroupVirtualServers() {
            return instance.getGroupVirtualServers();
        }

        @Override
        List<Rule> getRuleSet() {
            return instance.getRuleSet();
        }

        @Override
        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        @Override
        public void setProperties(List<Property> properties) {
            this.properties = properties;
        }

        @Override
        public List<String> getTags() {
            return tags;
        }

        @Override
        public List<Property> getProperties() {
            return properties;
        }

        @Override
        public Group getInstance() {
            return instance;
        }

        @Override
        public Comparable getValue(String property) {
            switch (property) {
                case "id":
                    return getId();
                case "name":
                    return getName();
                case "created-time":
                    return getCreatedTime();
                default:
                    if (property.startsWith("property:")) {
                        if (getProperties() == null) return null;

                        String propName = property.substring("property:".length());
                        for (Property p : getProperties()) {
                            if (p.getName().equals(propName)) {
                                return p.getValue();
                            }
                        }
                        return null;
                    }
                    return null;
            }
        }
    }

    class ExtendedVs extends VsView implements ExtendedView<VirtualServer>, PropertySortable {
        private List<String> tags;
        private List<Property> properties;
        private VirtualServer instance;

        public ExtendedVs() {
            this(new VirtualServer());
        }

        public ExtendedVs(VirtualServer instance) {
            this.instance = instance;
        }

        @Override
        public Long getId() {
            return instance.getId();
        }

        @Override
        String getName() {
            return instance.getName();
        }

        @Override
        String getPort() {
            return instance.getPort();
        }

        @Override
        List<Long> getSlbIds() {
            return instance.getSlbIds();
        }

        @Override
        Boolean getSsl() {
            return instance.getSsl();
        }

        @Override
        Integer getVersion() {
            return instance.getVersion();
        }

        @Override
        List<Domain> getDomains() {
            return instance.getDomains();
        }

        @Override
        List<Rule> getRuleSet() {
            return instance.getRuleSet();
        }

        @Override
        Date getCreatedTime() {
            return instance.getCreatedTime();
        }

        @Override
        void setId(Long id) {
            instance.setId(id);
        }

        @Override
        void setName(String name) {
            instance.setName(name);
        }

        @Override
        void setPort(String port) {
            instance.setPort(port);
        }

        @Override
        void setSlbIds(List<Long> slbIds) {
            instance.getSlbIds().clear();
            instance.getSlbIds().addAll(slbIds);
        }

        @Override
        void setSsl(Boolean ssl) {
            instance.setSsl(ssl);
        }

        @Override
        void setVersion(Integer v) {
            instance.setVersion(v);
        }

        @Override
        void setDomains(List<Domain> domains) {
            instance.getDomains().clear();
            instance.getDomains().addAll(domains);
        }

        @Override
        void setRuleSet(List<Rule> ruleSet) {
            instance.getRuleSet().clear();
            instance.getRuleSet().addAll(ruleSet);
        }

        @Override
        void setCreatedTime(Date date) {
            instance.setCreatedTime(date);
        }

        @Override
        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        @Override
        public void setProperties(List<Property> properties) {
            this.properties = properties;
        }

        @Override
        public List<String> getTags() {
            return tags;
        }

        @Override
        public List<Property> getProperties() {
            return properties;
        }

        @Override
        public VirtualServer getInstance() {
            return instance;
        }

        @Override
        public Comparable getValue(String property) {
            switch (property) {
                case "id":
                    return getId();
                case "name":
                    return getName();
                case "domain":
                    return getDomains().size() > 0 ? getDomains().get(0).getName() : null;
                case "ssl":
                    return getSsl();
                case "created-time":
                    return getCreatedTime();
                default:
                    if (property.startsWith("property:")) {
                        if (getProperties() == null) return null;

                        String propName = property.substring("property:".length());
                        for (Property p : getProperties()) {
                            if (p.getName().equals(propName)) {
                                return p.getValue();
                            }
                        }
                        return null;
                    }
                    return null;
            }
        }
    }

    class ExtendedSlb extends SlbView implements ExtendedView<Slb>, PropertySortable {
        private List<String> tags;
        private List<Property> properties;
        private Slb instance;

        public ExtendedSlb() {
            this(new Slb());
        }

        public ExtendedSlb(Slb instance) {
            this.instance = instance;
        }

        @Override
        public Long getId() {
            return instance.getId();
        }

        @Override
        String getName() {
            return instance.getName();
        }

        @Override
        String getNginxBin() {
            return instance.getNginxBin();
        }

        @Override
        String getNginxConf() {
            return instance.getNginxConf();
        }

        @Override
        Integer getNginxWorkerProcesses() {
            return instance.getNginxWorkerProcesses();
        }

        @Override
        List<SlbServer> getSlbServers() {
            return instance.getSlbServers();
        }

        @Override
        String getStatus() {
            return instance.getStatus();
        }

        @Override
        Integer getVersion() {
            return instance.getVersion();
        }

        @Override
        List<Vip> getVips() {
            return instance.getVips();
        }

        @Override
        Date getCreatedTime() {
            return instance.getCreatedTime();
        }

        @Override
        void setId(Long id) {
            instance.setId(id);
        }

        @Override
        void setName(String name) {
            instance.setName(name);
        }

        @Override
        void setNginxBin(String bin) {
            instance.setNginxBin(bin);
        }

        @Override
        void setNginxConf(String conf) {
            instance.setNginxConf(conf);
        }

        @Override
        void setNginxWorkerProcesses(Integer w) {
            instance.setNginxWorkerProcesses(w);
        }

        @Override
        void setSlbServers(List<SlbServer> servers) {
            instance.getSlbServers().clear();
            instance.getSlbServers().addAll(servers);
        }

        @Override
        void setStatus(String status) {
            instance.setStatus(status);
        }

        @Override
        void setVersion(Integer version) {
            instance.setVersion(version);
        }

        @Override
        void setVips(List<Vip> vips) {
            instance.getVips().clear();
            instance.getVips().addAll(vips);
        }

        @Override
        void setRuleSet(List<Rule> rules) {
            instance.getRuleSet().clear();
            instance.getRuleSet().addAll(rules);

        }

        @Override
        void setCreatedTime(Date time) {
            instance.setCreatedTime(time);
        }

        @Override
        List<Rule> getRuleSet() {
            return instance.getRuleSet();
        }

        @Override
        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        @Override
        public void setProperties(List<Property> properties) {
            this.properties = properties;
        }

        @Override
        public List<String> getTags() {
            return tags;
        }

        @Override
        public List<Property> getProperties() {
            return properties;
        }

        @Override
        public Slb getInstance() {
            return instance;
        }

        @Override
        public Comparable getValue(String property) {
            switch (property) {
                case "id":
                    return getId();
                case "name":
                    return getName();
                case "created-time":
                    return getCreatedTime();
                default:
                    if (property.startsWith("property:")) {
                        if (getProperties() == null) return null;

                        String propName = property.substring("property:".length());
                        for (Property p : getProperties()) {
                            if (p.getName().equals(propName)) {
                                return p.getValue();
                            }
                        }
                        return null;
                    }
                    return null;
            }
        }
    }

    class ExtendedTrafficPolicy extends TrafficPolicyView implements ExtendedView<TrafficPolicy> {
        private List<String> tags;
        private List<Property> properties;
        private final TrafficPolicy instance;

        public ExtendedTrafficPolicy() {
            this(new TrafficPolicy());
        }

        public ExtendedTrafficPolicy(TrafficPolicy instance) {
            this.instance = instance;
        }

        @Override
        public Long getId() {
            return instance.getId();
        }

        public List<TrafficControl> getControls() {
            return instance.getControls();
        }

        public String getName() {
            return instance.getName();
        }

        public List<PolicyVirtualServer> getPolicyVirtualServers() {
            return instance.getPolicyVirtualServers();
        }

        public Integer getVersion() {
            return instance.getVersion();
        }

        public Date getCreatedTime() {
            return instance.getCreatedTime();
        }

        @Override
        void setId(Long id) {
            instance.setId(id);
        }

        @Override
        void setName(String name) {
            instance.setName(name);
        }

        @Override
        void setVersion(Integer version) {
            instance.setVersion(version);
        }

        @Override
        void setControls(List<TrafficControl> controls) {
            instance.getControls().clear();
            instance.getControls().addAll(controls);
        }

        @Override
        void setPolicyVirtualServers(List<PolicyVirtualServer> pvs) {
            instance.getPolicyVirtualServers().clear();
            instance.getPolicyVirtualServers().addAll(pvs);
        }

        @Override
        void setCreatedTime(Date time) {
            instance.setCreatedTime(time);
        }

        @Override
        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        @Override
        public void setProperties(List<Property> properties) {
            this.properties = properties;
        }

        @Override
        public List<String> getTags() {
            return tags;
        }

        @Override
        public List<Property> getProperties() {
            return properties;
        }

        @Override
        public TrafficPolicy getInstance() {
            return instance;
        }
    }

    class ExtendedDr extends DrView implements ExtendedView<Dr>, PropertySortable {
        private List<String> tags;
        private List<Property> properties;
        private Dr instance;

        public ExtendedDr() {
            this(new Dr());
        }

        public ExtendedDr(Dr instance) {
            this.instance = instance;
        }

        @Override
        public Long getId() {
            return instance.getId();
        }

        public List<DrTraffic> getDrTraffics() {
            return instance.getDrTraffics();
        }

        public String getName() {
            return instance.getName();
        }

        public Integer getVersion() {
            return instance.getVersion();
        }

        public Date getCreatedTime() {
            return instance.getCreatedTime();
        }

        @Override
        void setId(Long id) {
            instance.setId(id);
        }

        @Override
        void setName(String name) {
            instance.setName(name);
        }

        @Override
        void setVersion(Integer version) {
            instance.setVersion(version);
        }

        @Override
        void getDrTraffics(List<DrTraffic> traffic) {
            instance.getDrTraffics().clear();
            instance.getDrTraffics().addAll(traffic);
        }

        @Override
        void setCreatedTime(Date time) {
            instance.setCreatedTime(time);
        }

        @Override
        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        @Override
        public void setProperties(List<Property> properties) {
            this.properties = properties;
        }

        @Override
        public List<String> getTags() {
            return tags;
        }

        @Override
        public List<Property> getProperties() {
            return properties;
        }

        @Override
        public Dr getInstance() {
            return instance;
        }

        @Override
        public Comparable getValue(String property) {
            switch (property) {
                case "id":
                    return getId();
                case "name":
                    return getName();
                case "created-time":
                    return getCreatedTime();
                default:
                    if (property.startsWith("property:")) {
                        if (getProperties() == null) return null;

                        String propName = property.substring("property:".length());
                        for (Property p : getProperties()) {
                            if (p.getName().equals(propName)) {
                                return p.getValue();
                            }
                        }
                        return null;
                    }
                    return null;
            }
        }
    }

    class ExtendedRule implements ExtendedView<Rule>, PropertySortable {
        private List<String> tags;
        private List<Property> properties;
        private Rule instance;

        public ExtendedRule() {
            this(new Rule());
        }

        public ExtendedRule(Rule instance) {
            this.instance = instance;
        }

        @Override
        public Long getId() {
            return instance.getId();
        }

        public String getName() {
            return instance.getName();
        }

        ;

        public String getRuleType() {
            return instance.getRuleType();
        }

        public String getAttributes() {
            return instance.getAttributes();
        }

        public String getTargetType() {
            return instance.getTargetType();
        }

        public String getTargetId() {
            return instance.getTargetId();
        }

        @Override
        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        @Override
        public void setProperties(List<Property> properties) {
            this.properties = properties;
        }

        @Override
        public List<String> getTags() {
            return tags;
        }

        @Override
        public List<Property> getProperties() {
            return properties;
        }

        @Override
        public Rule getInstance() {
            return instance;
        }

        @Override
        public Comparable getValue(String property) {
            switch (property) {
                case "name":
                    return getName();
                case "id":
                    return getId();
                case "rule-type":
                    return getRuleType();
                case "attributes":
                    return getAttributes();
                case "target-type":
                    return getTargetType();
                case "target-id":
                    return getTargetId();
                default:
                    if (property.startsWith("property:")) {
                        if (getProperties() == null) return null;

                        String propName = property.substring("property:".length());
                        for (Property p : getProperties()) {
                            if (p.getName().equals(propName)) {
                                return p.getValue();
                            }
                        }
                        return null;
                    }
                    return null;
            }
        }
    }

    class ExtendedCertificate extends CertificateView implements ExtendedView<Certificate>, PropertySortable {
        private Certificate instance;
        private List<String> tags = new ArrayList<>();
        private List<Property> properties = new ArrayList<>();

        public ExtendedCertificate() {
            this.instance= new Certificate();
        }

        public ExtendedCertificate(Certificate instance) {
            this.instance = instance;
        }

        @Override
        public Long getId() {
            return instance.getId();
        }

        @Override
        public Date getIssueTime() {
            return instance.getIssueTime();
        }

        @Override
        public Date getExpireTime() {
            return instance.getExpireTime();
        }

        @Override
        public String getCert() {
            return instance.getCertData();
        }

        @Override
        public String getKey() {
            return instance.getKeyData();
        }

        @Override
        List<Long> getVsIds() {
            return instance.getVsIds();
        }

        @Override
        List<String> getSlbServers() {
            return instance.getSlbServers();
        }

        @Override
        void setId(Long id) {
            instance.setId(id);
        }

        @Override
        void setIssueTime(Date time) {
            instance.setIssueTime(time);
        }

        @Override
        void setExpireTime(Date time) {
            instance.setExpireTime(time);
        }

        @Override
        void setCert(String cert) {
            instance.setCertData(cert);
        }

        @Override
        void setKey(String key) {
            instance.setKeyData(key);
        }

        @Override
        void setVsIds(List<Long> vsIds) {
            instance.getVsIds().clear();
            instance.getVsIds().addAll(vsIds);
        }

        @Override
        void setSlbServers(List<String> servers) {
            instance.getSlbServers().clear();
            instance.getSlbServers().addAll(servers);
        }

        @Override
        String getDomain() {
            return instance.getDomain();
        }

        @Override
        void setDomain(String domain) {
            instance.setDomain(domain);
        }

        @Override
        String getCid() {
            return instance.getCid();
        }

        @Override
        void setCid(String cid) {
            instance.setCid(cid);
        }

        @Override
        public void setTags(List<String> tags) {
            this.tags.clear();
            this.tags.addAll(tags);
        }

        @Override
        public void setProperties(List<Property> properties) {
            this.properties.clear();
            this.properties.addAll(properties);
        }

        @Override
        public List<String> getTags() {
            return tags;
        }

        @Override
        public List<Property> getProperties() {
            return properties;
        }

        @Override
        public Certificate getInstance() {
            return instance;
        }

        @Override
        public Comparable getValue(String property) {
            if (property == null) {
                return null;
            }

            switch (property) {
                case "id":
                    return getId();
                case "issue-time":
                    return getIssueTime();
                case "expire-time":
                    return getExpireTime();
                case "cert":
                    return getCert();
                case "key":
                    return getKey();
                case "domain":
                    return getDomain();
                case "cid":
                    return getCid();
                default: {
                    if (property.startsWith("property:")) {
                        String key = property.substring("property:".length());
                        for (Property property1 : getProperties()) {
                            if (key.equals(property1.getName())) {
                                return property1.getValue();
                            }
                        }
                    }

                    return null;
                }
            }
        }
    }
}
