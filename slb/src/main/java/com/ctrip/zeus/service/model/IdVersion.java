package com.ctrip.zeus.service.model;

/**
 * Created by zhoumy on 2015/12/23.
 */
public class IdVersion implements Comparable<IdVersion> {

    private Long id;
    private Integer version;

    public IdVersion(Long id, Integer version) {
        this.id = id;
        this.version = version;
    }

    public IdVersion(String stringValue) {
        int split = stringValue.indexOf('_');
        this.id = Long.parseLong(stringValue.substring(0, split));
        this.version = Integer.parseInt(stringValue.substring(split + 1, stringValue.length()));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        return id.hashCode() * 31 + version;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IdVersion) {
            return compareTo((IdVersion) obj) == 0;
        }
        return false;
    }

    @Override
    public int compareTo(IdVersion o) {
        if (this.id < o.id) return -1;
        if (this.id.longValue() == o.id.longValue())
            return (this.version - o.version) < 0 ? -1 : ((this.version.intValue() == o.version.intValue()) ? 0 : 1);
        return 1;
    }

    @Override
    public String toString() {
        return id + "," + version;
    }
}