package com.ctrip.zeus.domain;


import java.util.List;
import java.util.Objects;

/**
 * @Discription
 **/
public class ClusterInfo {
    private String name;
    private List<String> ips;
    private List<Config> configs;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getIps() {
        return ips;
    }

    public void setIps(List<String> ips) {
        this.ips = ips;
    }

    public List<Config> getConfigs() {
        return configs;
    }

    public void setConfigs(List<Config> config) {
        this.configs = config;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClusterInfo)) return false;
        ClusterInfo cluster = (ClusterInfo) o;
        return Objects.equals(getName(), cluster.getName()) &&
                Objects.equals(getIps(), cluster.getIps()) &&
                Objects.equals(getConfigs(), cluster.getConfigs());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getIps(), getConfigs());
    }
}
