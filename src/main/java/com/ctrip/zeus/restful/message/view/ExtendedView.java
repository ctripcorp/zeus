package com.ctrip.zeus.restful.message.view;

import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.tag.entity.Property;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

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

    class ExtendedGroup extends GroupView implements ExtendedView<Group> {
        private List<String> tags;
        private List<Property> properties;
        private Group instance;

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
    }

    class ExtendedVs extends VsView implements ExtendedView<VirtualServer> {
        private List<String> tags;
        private List<Property> properties;
        private VirtualServer instance;

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
        Long getSlbId() {
            return instance.getSlbId();
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
            return null;
        }
    }

    class ExtendedSlb extends SlbView implements ExtendedView<Slb> {
        private List<String> tags;
        private List<Property> properties;
        private Slb instance;

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
        List<VirtualServer> getVirtualServers() {
            return instance.getVirtualServers();
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
            return null;
        }
    }
}
