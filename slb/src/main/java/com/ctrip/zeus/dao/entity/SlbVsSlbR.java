package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class SlbVsSlbR {
    private Long id;

    private Long vsId;

    private Long slbId;

    private Integer vsVersion;

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

    public Long getSlbId() {
        return slbId;
    }

    public void setSlbId(Long slbId) {
        this.slbId = slbId;
    }

    public Integer getVsVersion() {
        return vsVersion;
    }

    public void setVsVersion(Integer vsVersion) {
        this.vsVersion = vsVersion;
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
        sb.append(", slbId=").append(slbId);
        sb.append(", vsVersion=").append(vsVersion);
        sb.append(", datachangeLasttime=").append(datachangeLasttime);
        sb.append("]");
        return sb.toString();
    }

    public static SlbVsSlbR.Builder builder() {
        return new SlbVsSlbR.Builder();
    }

    public static class Builder {
        private SlbVsSlbR obj;

        public Builder() {
            this.obj = new SlbVsSlbR();
        }

        public Builder id(Long id) {
            obj.setId(id);
            return this;
        }

        public Builder vsId(Long vsId) {
            obj.setVsId(vsId);
            return this;
        }

        public Builder slbId(Long slbId) {
            obj.setSlbId(slbId);
            return this;
        }

        public Builder vsVersion(Integer vsVersion) {
            obj.setVsVersion(vsVersion);
            return this;
        }

        public Builder datachangeLasttime(Date datachangeLasttime) {
            obj.setDatachangeLasttime(datachangeLasttime);
            return this;
        }

        public SlbVsSlbR build() {
            return this.obj;
        }
    }

    public enum Column {
        id("id", "id", "BIGINT", false),
        vsId("vs_id", "vsId", "BIGINT", false),
        slbId("slb_id", "slbId", "BIGINT", false),
        vsVersion("vs_version", "vsVersion", "INTEGER", false),
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