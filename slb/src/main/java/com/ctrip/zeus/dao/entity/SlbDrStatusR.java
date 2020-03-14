package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class SlbDrStatusR {
    private Long id;

    private Long drId;

    private Integer offlineVersion;

    private Integer onlineVersion;

    private Date datachangeLasttime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDrId() {
        return drId;
    }

    public void setDrId(Long drId) {
        this.drId = drId;
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
        sb.append(", drId=").append(drId);
        sb.append(", offlineVersion=").append(offlineVersion);
        sb.append(", onlineVersion=").append(onlineVersion);
        sb.append(", datachangeLasttime=").append(datachangeLasttime);
        sb.append("]");
        return sb.toString();
    }

    public static SlbDrStatusR.Builder builder() {
        return new SlbDrStatusR.Builder();
    }

    public static class Builder {
        private SlbDrStatusR obj;

        public Builder() {
            this.obj = new SlbDrStatusR();
        }

        public Builder id(Long id) {
            obj.setId(id);
            return this;
        }

        public Builder drId(Long drId) {
            obj.setDrId(drId);
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

        public Builder datachangeLasttime(Date datachangeLasttime) {
            obj.setDatachangeLasttime(datachangeLasttime);
            return this;
        }

        public SlbDrStatusR build() {
            return this.obj;
        }
    }

    public enum Column {
        id("id", "id", "BIGINT", false),
        drId("dr_id", "drId", "BIGINT", false),
        offlineVersion("offline_version", "offlineVersion", "INTEGER", false),
        onlineVersion("online_version", "onlineVersion", "INTEGER", false),
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