package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class MessageQueue {
    private Long id;

    private String performer;

    private String type;

    private Long targetId;

    private String targetData;

    private String status;

    private Date createTime;

    private Date datachangeLasttime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPerformer() {
        return performer;
    }

    public void setPerformer(String performer) {
        this.performer = performer == null ? null : performer.trim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getTargetData() {
        return targetData;
    }

    public void setTargetData(String targetData) {
        this.targetData = targetData == null ? null : targetData.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
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
        sb.append(", performer=").append(performer);
        sb.append(", type=").append(type);
        sb.append(", targetId=").append(targetId);
        sb.append(", targetData=").append(targetData);
        sb.append(", status=").append(status);
        sb.append(", createTime=").append(createTime);
        sb.append(", datachangeLasttime=").append(datachangeLasttime);
        sb.append("]");
        return sb.toString();
    }

    public static MessageQueue.Builder builder() {
        return new MessageQueue.Builder();
    }

    public static class Builder {
        private MessageQueue obj;

        public Builder() {
            this.obj = new MessageQueue();
        }

        public Builder id(Long id) {
            obj.setId(id);
            return this;
        }

        public Builder performer(String performer) {
            obj.setPerformer(performer);
            return this;
        }

        public Builder type(String type) {
            obj.setType(type);
            return this;
        }

        public Builder targetId(Long targetId) {
            obj.setTargetId(targetId);
            return this;
        }

        public Builder targetData(String targetData) {
            obj.setTargetData(targetData);
            return this;
        }

        public Builder status(String status) {
            obj.setStatus(status);
            return this;
        }

        public Builder createTime(Date createTime) {
            obj.setCreateTime(createTime);
            return this;
        }

        public Builder datachangeLasttime(Date datachangeLasttime) {
            obj.setDatachangeLasttime(datachangeLasttime);
            return this;
        }

        public MessageQueue build() {
            return this.obj;
        }
    }

    public enum Column {
        id("id", "id", "BIGINT", false),
        performer("performer", "performer", "VARCHAR", false),
        type("type", "type", "VARCHAR", false),
        targetId("target_id", "targetId", "BIGINT", false),
        targetData("target_data", "targetData", "VARCHAR", false),
        status("status", "status", "VARCHAR", false),
        createTime("create_time", "createTime", "TIMESTAMP", false),
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