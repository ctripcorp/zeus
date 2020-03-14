package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class SlbGroupSlbR {
    private Long id;

    private Long groupId;

    private Long slbId;

    private Long slbVirtualServerId;

    private String path;

    private Integer priority;

    private Date createdTime;

    private Date datachangeLasttime;

    private String rewrite;

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

    public Long getSlbId() {
        return slbId;
    }

    public void setSlbId(Long slbId) {
        this.slbId = slbId;
    }

    public Long getSlbVirtualServerId() {
        return slbVirtualServerId;
    }

    public void setSlbVirtualServerId(Long slbVirtualServerId) {
        this.slbVirtualServerId = slbVirtualServerId;
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

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getDatachangeLasttime() {
        return datachangeLasttime;
    }

    public void setDatachangeLasttime(Date datachangeLasttime) {
        this.datachangeLasttime = datachangeLasttime;
    }

    public String getRewrite() {
        return rewrite;
    }

    public void setRewrite(String rewrite) {
        this.rewrite = rewrite == null ? null : rewrite.trim();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", groupId=").append(groupId);
        sb.append(", slbId=").append(slbId);
        sb.append(", slbVirtualServerId=").append(slbVirtualServerId);
        sb.append(", path=").append(path);
        sb.append(", priority=").append(priority);
        sb.append(", createdTime=").append(createdTime);
        sb.append(", datachangeLasttime=").append(datachangeLasttime);
        sb.append(", rewrite=").append(rewrite);
        sb.append("]");
        return sb.toString();
    }

    public static SlbGroupSlbR.Builder builder() {
        return new SlbGroupSlbR.Builder();
    }

    public static class Builder {
        private SlbGroupSlbR obj;

        public Builder() {
            this.obj = new SlbGroupSlbR();
        }

        public Builder id(Long id) {
            obj.setId(id);
            return this;
        }

        public Builder groupId(Long groupId) {
            obj.setGroupId(groupId);
            return this;
        }

        public Builder slbId(Long slbId) {
            obj.setSlbId(slbId);
            return this;
        }

        public Builder slbVirtualServerId(Long slbVirtualServerId) {
            obj.setSlbVirtualServerId(slbVirtualServerId);
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

        public Builder createdTime(Date createdTime) {
            obj.setCreatedTime(createdTime);
            return this;
        }

        public Builder datachangeLasttime(Date datachangeLasttime) {
            obj.setDatachangeLasttime(datachangeLasttime);
            return this;
        }

        public Builder rewrite(String rewrite) {
            obj.setRewrite(rewrite);
            return this;
        }

        public SlbGroupSlbR build() {
            return this.obj;
        }
    }

    public enum Column {
        id("id", "id", "BIGINT", false),
        groupId("group_id", "groupId", "BIGINT", false),
        slbId("slb_id", "slbId", "BIGINT", false),
        slbVirtualServerId("slb_virtual_server_id", "slbVirtualServerId", "BIGINT", false),
        path("path", "path", "VARCHAR", false),
        priority("priority", "priority", "INTEGER", false),
        createdTime("created_time", "createdTime", "TIMESTAMP", false),
        datachangeLasttime("DataChange_LastTime", "datachangeLasttime", "TIMESTAMP", false),
        rewrite("rewrite", "rewrite", "LONGVARCHAR", false);

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