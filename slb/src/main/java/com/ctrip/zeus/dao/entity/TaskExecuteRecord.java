package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class TaskExecuteRecord {
    private Long id;

    private String taskKey;

    private Long lastExecuteTime;

    private Date datachangeLasttime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskKey() {
        return taskKey;
    }

    public void setTaskKey(String taskKey) {
        this.taskKey = taskKey == null ? null : taskKey.trim();
    }

    public Long getLastExecuteTime() {
        return lastExecuteTime;
    }

    public void setLastExecuteTime(Long lastExecuteTime) {
        this.lastExecuteTime = lastExecuteTime;
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
        sb.append(", taskKey=").append(taskKey);
        sb.append(", lastExecuteTime=").append(lastExecuteTime);
        sb.append(", datachangeLasttime=").append(datachangeLasttime);
        sb.append("]");
        return sb.toString();
    }

    public static TaskExecuteRecord.Builder builder() {
        return new TaskExecuteRecord.Builder();
    }

    public static class Builder {
        private TaskExecuteRecord obj;

        public Builder() {
            this.obj = new TaskExecuteRecord();
        }

        public Builder id(Long id) {
            obj.setId(id);
            return this;
        }

        public Builder taskKey(String taskKey) {
            obj.setTaskKey(taskKey);
            return this;
        }

        public Builder lastExecuteTime(Long lastExecuteTime) {
            obj.setLastExecuteTime(lastExecuteTime);
            return this;
        }

        public Builder datachangeLasttime(Date datachangeLasttime) {
            obj.setDatachangeLasttime(datachangeLasttime);
            return this;
        }

        public TaskExecuteRecord build() {
            return this.obj;
        }
    }

    public enum Column {
        id("id", "id", "BIGINT", false),
        taskKey("task_key", "taskKey", "VARCHAR", false),
        lastExecuteTime("last_execute_time", "lastExecuteTime", "BIGINT", false),
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