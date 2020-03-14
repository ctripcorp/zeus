package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class SlbGroupVsR {
    private Long id;

    private Long groupId;

    private Long vsId;

    private String path;

    private Integer priority;

    private Integer groupVersion;

    private Date datachangeLasttime;

    private byte[] content;

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

    public Long getVsId() {
        return vsId;
    }

    public void setVsId(Long vsId) {
        this.vsId = vsId;
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

    public Integer getGroupVersion() {
        return groupVersion;
    }

    public void setGroupVersion(Integer groupVersion) {
        this.groupVersion = groupVersion;
    }

    public Date getDatachangeLasttime() {
        return datachangeLasttime;
    }

    public void setDatachangeLasttime(Date datachangeLasttime) {
        this.datachangeLasttime = datachangeLasttime;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", groupId=").append(groupId);
        sb.append(", vsId=").append(vsId);
        sb.append(", path=").append(path);
        sb.append(", priority=").append(priority);
        sb.append(", groupVersion=").append(groupVersion);
        sb.append(", datachangeLasttime=").append(datachangeLasttime);
        sb.append(", content=").append(content);
        sb.append("]");
        return sb.toString();
    }

    public static SlbGroupVsR.Builder builder() {
        return new SlbGroupVsR.Builder();
    }

    public static class Builder {
        private SlbGroupVsR obj;

        public Builder() {
            this.obj = new SlbGroupVsR();
        }

        public Builder id(Long id) {
            obj.setId(id);
            return this;
        }

        public Builder groupId(Long groupId) {
            obj.setGroupId(groupId);
            return this;
        }

        public Builder vsId(Long vsId) {
            obj.setVsId(vsId);
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

        public Builder groupVersion(Integer groupVersion) {
            obj.setGroupVersion(groupVersion);
            return this;
        }

        public Builder datachangeLasttime(Date datachangeLasttime) {
            obj.setDatachangeLasttime(datachangeLasttime);
            return this;
        }

        public Builder content(byte[] content) {
            obj.setContent(content);
            return this;
        }

        public SlbGroupVsR build() {
            return this.obj;
        }
    }

    public enum Column {
        id("id", "id", "BIGINT", false),
        groupId("group_id", "groupId", "BIGINT", false),
        vsId("vs_id", "vsId", "BIGINT", false),
        path("path", "path", "VARCHAR", false),
        priority("priority", "priority", "INTEGER", false),
        groupVersion("group_version", "groupVersion", "INTEGER", false),
        datachangeLasttime("DataChange_LastTime", "datachangeLasttime", "TIMESTAMP", false),
        content("content", "content", "LONGVARBINARY", false);

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