package com.ctrip.zeus.restful.message.view;

import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.query.sort.PropertySortable;
import com.ctrip.zeus.tag.entity.Property;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

import java.util.Date;
import java.util.List;

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
        Date getCreatedTime() {
            return instance.getCreatedTime();
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

    class ExtendedTrafficPolicy implements ExtendedView<TrafficPolicy> {
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
            return getInstance();
        }
    }
}
