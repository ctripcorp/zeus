package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class SlbTrafficPolicyVsR {
    private Long id;

    private Long vsId;

    private Long policyId;

    private Integer policyVersion;

    private String path;

    private Integer priority;

    private Integer hash;

    private Date datachangeLasttime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVsId() {
        return vsId;
    }

    public void setVsId(Long vsId) {
        this.vsId = vsId;
    }

    public Long getPolicyId() {
        return policyId;
    }

    public void setPolicyId(Long policyId) {
        this.policyId = policyId;
    }

    public Integer getPolicyVersion() {
        return policyVersion;
    }

    public void setPolicyVersion(Integer policyVersion) {
        this.policyVersion = policyVersion;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path == null ? null : path.trim();
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getHash() {
        return hash;
    }

    public void setHash(Integer hash) {
        this.hash = hash;
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
        sb.append(", vsId=").append(vsId);
        sb.append(", policyId=").append(policyId);
        sb.append(", policyVersion=").append(policyVersion);
        sb.append(", path=").append(path);
        sb.append(", priority=").append(priority);
        sb.append(", hash=").append(hash);
        sb.append(", datachangeLasttime=").append(datachangeLasttime);
        sb.append("]");
        return sb.toString();
    }

    public static SlbTrafficPolicyVsR.Builder builder() {
        return new SlbTrafficPolicyVsR.Builder();
    }

    public static class Builder {
        private SlbTrafficPolicyVsR obj;

        public Builder() {
            this.obj = new SlbTrafficPolicyVsR();
        }

        public Builder id(Long id) {
            obj.setId(id);
            return this;
        }

        public Builder vsId(Long vsId) {
            obj.setVsId(vsId);
            return this;
        }

        public Builder policyId(Long policyId) {
            obj.setPolicyId(policyId);
            return this;
        }

        public Builder policyVersion(Integer policyVersion) {
            obj.setPolicyVersion(policyVersion);
            return this;
        }

        public Builder path(String path) {
            obj.setPath(path);
            return this;
        }

        public Builder priority(Integer priority) {
            obj.setPriority(priority);
            return this;
        }

        public Builder hash(Integer hash) {
            obj.setHash(hash);
            return this;
        }

        public Builder datachangeLasttime(Date datachangeLasttime) {
            obj.setDatachangeLasttime(datachangeLasttime);
            return this;
        }

        public SlbTrafficPolicyVsR build() {
            return this.obj;
        }
    }

    public enum Column {
        id("id", "id", "BIGINT", false),
        vsId("vs_id", "vsId", "BIGINT", false),
        policyId("policy_id", "policyId", "BIGINT", false),
        policyVersion("policy_version", "policyVersion", "INTEGER", false),
        path("path", "path", "VARCHAR", false),
        priority("priority", "priority", "INTEGER", false),
        hash("hash", "hash", "INTEGER", false),
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