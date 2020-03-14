package com.ctrip.zeus.domain;

import java.util.Objects;

/**
 * @Discription
 **/
public class Config {
    private String name;
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Config)) return false;
        Config config = (Config) o;
        return Objects.equals(getName(), config.getName()) &&
                Objects.equals(getValue(), config.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getValue());
    }
}
