package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class SlbVirtualServer {
    private Long id;

    private Long slbId;

    private String name;

    private String port;

    private Boolean isSsl;

    private Date createdTime;

    private Integer version;

    private Date datachangeLasttime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSlbId() {
        return slbId;
    }

    public void setSlbId(Long slbId) {
        this.slbId = slbId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port == null ? null : port.trim();
    }

    public Boolean getIsSsl() {
        return isSsl;
    }

    public void setIsSsl(Boolean isSsl) {
        this.isSsl = isSsl;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Date getDatachangeLasttime() {
        return datachangeLasttime;
    }

    public void setDatachangeLasttime(Date datachangeLasttime) {
        this.datachangeLasttime = datachangeLasttime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", slbId=").append(slbId);
        sb.append(", name=").append(name);
        sb.append(", port=").append(port);
        sb.append(", isSsl=").append(isSsl);
        sb.append(", createdTime=").append(createdTime);
        sb.append(", version=").append(version);
        sb.append(", datachangeLasttime=").append(datachangeLasttime);
        sb.append("]");
        return sb.toString();
    }

    public static SlbVirtualServer.Builder builder() {
        return new SlbVirtualServer.Builder();
    }

    public static class Builder {
        private SlbVirtualServer obj;

        public Builder() {
            this.obj = new SlbVirtualServer();
        }

        public Builder id(Long id) {
            obj.setId(id);
            return this;
        }

        public Builder slbId(Long slbId) {
            obj.setSlbId(slbId);
            return this;
        }

        public Builder name(String name) {
            obj.setName(name);
            return this;
        }

        public Builder port(String port) {
            obj.setPort(port);
            return this;
        }

        public Builder isSsl(Boolean isSsl) {
            obj.setIsSsl(isSsl);
            return this;
        }

        public Builder createdTime(Date createdTime) {
            obj.setCreatedTime(createdTime);
            return this;
        }

        public Builder version(Integer version) {
            obj.setVersion(version);
            return this;
        }

        public Builder datachangeLasttime(Date datachangeLasttime) {
            obj.setDatachangeLasttime(datachangeLasttime);
            return this;
        }

        public SlbVirtualServer build() {
            return this.obj;
        }
    }

    public enum Column {
        id("id", "id", "BIGINT", false),
        slbId("slb_id", "slbId", "BIGINT", false),
        name("name", "name", "VARCHAR", true),
        port("port", "port", "VARCHAR", false),
        isSsl("is_ssl", "isSsl", "BIT", false),
        createdTime("created_time", "createdTime", "TIMESTAMP", false),
        version("version", "version", "INTEGER", false),
        datachangeLasttime("DataChange_LastTime", "datachangeLasttime", "TIMESTAMP", false);

        private static final String BEGINNING_DELIMITER = "`";

        private static final String ENDING_DELIMITER = "`";

        private final String column;

        private final boolean isColumnNameDelimited;

        private final String javaProperty;

        private final String jdbcType;

        public String value() {
            return this.column;
        }

        public String getValue() {
            return this.column;
        }

        public String getJavaProperty() {
            return this.javaProperty;
        }

        public String getJdbcType() {
            return this.jdbcType;
        }

        Column(String column, String javaProperty, String jdbcType, boolean isColumnNameDelimited) {
            this.column = column;
            this.javaProperty = javaProperty;
            this.jdbcType = jdbcType;
            this.isColumnNameDelimited = isColumnNameDelimited;
        }

        public String desc() {
            return this.getEscapedColumnName() + " DESC";
        }

        public String asc() {
            return this.getEscapedColumnName() + " ASC";
        }

        public static Column[] excludes(Column ... excludes) {
            ArrayList<Column> columns = new ArrayList<>(Arrays.asList(Column.values()));
            if (excludes != null && excludes.length > 0) {
                columns.removeAll(new ArrayList<>(Arrays.asList(excludes)));
            }
            return columns.toArray(new Column[]{});
        }

        public String getEscapedColumnName() {
            if (this.isColumnNameDelimited) {
                return new StringBuilder().append(BEGINNING_DELIMITER).append(this.column).append(ENDING_DELIMITER).toString();
            } else {
                return this.column;
            }
        }

        public String getAliasedEscapedColumnName() {
            return this.getEscapedColumnName();
        }
    }
}