package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class SlbBuildCommit {
    private Long id;

    private Long version;

    private Long slbId;

    private String vsIds;

    private String groupIds;

    private String taskIds;

    private String cleanvsIds;

    private String type;

    private Date datachangeLasttime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Long getSlbId() {
        return slbId;
    }

    public void setSlbId(Long slbId) {
        this.slbId = slbId;
    }

    public String getVsIds() {
        return vsIds;
    }

    public void setVsIds(String vsIds) {
        this.vsIds = vsIds == null ? null : vsIds.trim();
    }

    public String getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(String groupIds) {
        this.groupIds = groupIds == null ? null : groupIds.trim();
    }

    public String getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(String taskIds) {
        this.taskIds = taskIds == null ? null : taskIds.trim();
    }

    public String getCleanvsIds() {
        return cleanvsIds;
    }

    public void setCleanvsIds(String cleanvsIds) {
        this.cleanvsIds = cleanvsIds == null ? null : cleanvsIds.trim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
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
        sb.append(", version=").append(version);
        sb.append(", slbId=").append(slbId);
        sb.append(", vsIds=").append(vsIds);
        sb.append(", groupIds=").append(groupIds);
        sb.append(", taskIds=").append(taskIds);
        sb.append(", cleanvsIds=").append(cleanvsIds);
        sb.append(", type=").append(type);
        sb.append(", datachangeLasttime=").append(datachangeLasttime);
        sb.append("]");
        return sb.toString();
    }

    public static SlbBuildCommit.Builder builder() {
        return new SlbBuildCommit.Builder();
    }

    public static class Builder {
        private SlbBuildCommit obj;

        public Builder() {
            this.obj = new SlbBuildCommit();
        }

        public Builder id(Long id) {
            obj.setId(id);
            return this;
        }

        public Builder version(Long version) {
            obj.setVersion(version);
            return this;
        }

        public Builder slbId(Long slbId) {
            obj.setSlbId(slbId);
            return this;
        }

        public Builder vsIds(String vsIds) {
            obj.setVsIds(vsIds);
            return this;
        }

        public Builder groupIds(String groupIds) {
            obj.setGroupIds(groupIds);
            return this;
        }

        public Builder taskIds(String taskIds) {
            obj.setTaskIds(taskIds);
            return this;
        }

        public Builder cleanvsIds(String cleanvsIds) {
            obj.setCleanvsIds(cleanvsIds);
            return this;
        }

        public Builder type(String type) {
            obj.setType(type);
            return this;
        }

        public Builder datachangeLasttime(Date datachangeLasttime) {
            obj.setDatachangeLasttime(datachangeLasttime);
            return this;
        }

        public SlbBuildCommit build() {
            return this.obj;
        }
    }

    public enum Column {
        id("id", "id", "BIGINT", false),
        version("version", "version", "BIGINT", false),
        slbId("slb_id", "slbId", "BIGINT", false),
        vsIds("vs_ids", "vsIds", "VARCHAR", false),
        groupIds("group_ids", "groupIds", "VARCHAR", false),
        taskIds("task_ids", "taskIds", "VARCHAR", false),
        cleanvsIds("cleanvs_ids", "cleanvsIds", "VARCHAR", false),
        type("type", "type", "VARCHAR", false),
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