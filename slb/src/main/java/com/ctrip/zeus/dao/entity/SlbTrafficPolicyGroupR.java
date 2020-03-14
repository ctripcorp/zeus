package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class SlbTrafficPolicyGroupR {
    private Long id;

    private Long groupId;

    private Long policyId;

    private Integer policyVersion;

    private Integer weight;

    private Integer hash;

    private Date datachangeLasttime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
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

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
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
        sb.append(", groupId=").append(groupId);
        sb.append(", policyId=").append(policyId);
        sb.append(", policyVersion=").append(policyVersion);
        sb.append(", weight=").append(weight);
        sb.append(", hash=").append(hash);
        sb.append(", datachangeLasttime=").append(datachangeLasttime);
        sb.append("]");
        return sb.toString();
    }

    public static SlbTrafficPolicyGroupR.Builder builder() {
        return new SlbTrafficPolicyGroupR.Builder();
    }

    public static class Builder {
        private SlbTrafficPolicyGroupR obj;

        public Builder() {
            this.obj = new SlbTrafficPolicyGroupR();
        }

        public Builder id(Long id) {
            obj.setId(id);
            return this;
        }

        public Builder groupId(Long groupId) {
            obj.setGroupId(groupId);
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

        public Builder weight(Integer weight) {
            obj.setWeight(weight);
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

        public SlbTrafficPolicyGroupR build() {
            return this.obj;
        }
    }

    public enum Column {
        id("id", "id", "BIGINT", false),
        groupId("group_id", "groupId", "BIGINT", false),
        policyId("policy_id", "policyId", "BIGINT", false),
        policyVersion("policy_version", "policyVersion", "INTEGER", false),
        weight("weight", "weight", "INTEGER", false),
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