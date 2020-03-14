package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class TaskGlobalJob {
    private String jobKey;

    private String owner;

    private String status;

    private Date startTime;

    private Date finishTime;

    private Date datachangeLasttime;

    public String getJobKey() {
        return jobKey;
    }

    public void setJobKey(String jobKey) {
        this.jobKey = jobKey == null ? null : jobKey.trim();
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner == null ? null : owner.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
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
        sb.append(", jobKey=").append(jobKey);
        sb.append(", owner=").append(owner);
        sb.append(", status=").append(status);
        sb.append(", startTime=").append(startTime);
        sb.append(", finishTime=").append(finishTime);
        sb.append(", datachangeLasttime=").append(datachangeLasttime);
        sb.append("]");
        return sb.toString();
    }

    public static TaskGlobalJob.Builder builder() {
        return new TaskGlobalJob.Builder();
    }

    public static class Builder {
        private TaskGlobalJob obj;

        public Builder() {
            this.obj = new TaskGlobalJob();
        }

        public Builder jobKey(String jobKey) {
            obj.setJobKey(jobKey);
            return this;
        }

        public Builder owner(String owner) {
            obj.setOwner(owner);
            return this;
        }

        public Builder status(String status) {
            obj.setStatus(status);
            return this;
        }

        public Builder startTime(Date startTime) {
            obj.setStartTime(startTime);
            return this;
        }

        public Builder finishTime(Date finishTime) {
            obj.setFinishTime(finishTime);
            return this;
        }

        public Builder datachangeLasttime(Date datachangeLasttime) {
            obj.setDatachangeLasttime(datachangeLasttime);
            return this;
        }

        public TaskGlobalJob build() {
            return this.obj;
        }
    }

    public enum Column {
        jobKey("job_key", "jobKey", "VARCHAR", false),
        owner("owner", "owner", "VARCHAR", true),
        status("status", "status", "VARCHAR", true),
        startTime("start_time", "startTime", "TIMESTAMP", false),
        finishTime("finish_time", "finishTime", "TIMESTAMP", false),
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