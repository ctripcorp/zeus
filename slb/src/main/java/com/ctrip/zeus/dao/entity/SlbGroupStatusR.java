package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class SlbGroupStatusR {
    private Long id;

    private Long groupId;

    private Integer offlineVersion;

    private Integer onlineVersion;

    private Integer canaryVersion;

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

    public Integer getOfflineVersion() {
        return offlineVersion;
    }

    public void setOfflineVersion(Integer offlineVersion) {
        this.offlineVersion = offlineVersion;
    }

    public Integer getOnlineVersion() {
        return onlineVersion;
    }

    public void setOnlineVersion(Integer onlineVersion) {
        this.onlineVersion = onlineVersion;
    }

    public Integer getCanaryVersion() {
        return canaryVersion;
    }

    public void setCanaryVersion(Integer canaryVersion) {
        this.canaryVersion = canaryVersion;
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
        sb.append(", offlineVersion=").append(offlineVersion);
        sb.append(", onlineVersion=").append(onlineVersion);
        sb.append(", canaryVersion=").append(canaryVersion);
        sb.append(", datachangeLasttime=").append(datachangeLasttime);
        sb.append("]");
        return sb.toString();
    }

    public static SlbGroupStatusR.Builder builder() {
        return new SlbGroupStatusR.Builder();
    }

    public static class Builder {
        private SlbGroupStatusR obj;

        public Builder() {
            this.obj = new SlbGroupStatusR();
        }

        public Builder id(Long id) {
            obj.setId(id);
            return this;
        }

        public Builder groupId(Long groupId) {
            obj.setGroupId(groupId);
            return this;
        }

        public Builder offlineVersion(Integer offlineVersion) {
            obj.setOfflineVersion(offlineVersion);
            return this;
        }

        public Builder onlineVersion(Integer onlineVersion) {
            obj.setOnlineVersion(onlineVersion);
            return this;
        }

        public Builder canaryVersion(Integer canaryVersion) {
            obj.setCanaryVersion(canaryVersion);
            return this;
        }

        public Builder datachangeLasttime(Date datachangeLasttime) {
            obj.setDatachangeLasttime(datachangeLasttime);
            return this;
        }

        public SlbGroupStatusR build() {
            return this.obj;
        }
    }

    public enum Column {
        id("id", "id", "BIGINT", false),
        groupId("group_id", "groupId", "BIGINT", false),
        offlineVersion("offline_version", "offlineVersion", "INTEGER", false),
        onlineVersion("online_version", "onlineVersion", "INTEGER", false),
        canaryVersion("canary_version", "canaryVersion", "INTEGER", false),
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