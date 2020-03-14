package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class ReportSnapshot {
    private Long id;

    private String targetType;

    private String aggKey;

    private String aggValue;

    private Date createTime;

    private Date datachangeLasttime;

    private Long count;

    private String targetTag;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType == null ? null : targetType.trim();
    }

    public String getAggKey() {
        return aggKey;
    }

    public void setAggKey(String aggKey) {
        this.aggKey = aggKey == null ? null : aggKey.trim();
    }

    public String getAggValue() {
        return aggValue;
    }

    public void setAggValue(String aggValue) {
        this.aggValue = aggValue == null ? null : aggValue.trim();
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

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public String getTargetTag() {
        return targetTag;
    }

    public void setTargetTag(String targetTag) {
        this.targetTag = targetTag == null ? null : targetTag.trim();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", targetType=").append(targetType);
        sb.append(", aggKey=").append(aggKey);
        sb.append(", aggValue=").append(aggValue);
        sb.append(", createTime=").append(createTime);
        sb.append(", datachangeLasttime=").append(datachangeLasttime);
        sb.append(", count=").append(count);
        sb.append(", targetTag=").append(targetTag);
        sb.append("]");
        return sb.toString();
    }

    public static ReportSnapshot.Builder builder() {
        return new ReportSnapshot.Builder();
    }

    public static class Builder {
        private ReportSnapshot obj;

        public Builder() {
            this.obj = new ReportSnapshot();
        }

        public Builder id(Long id) {
            obj.setId(id);
            return this;
        }

        public Builder targetType(String targetType) {
            obj.setTargetType(targetType);
            return this;
        }

        public Builder aggKey(String aggKey) {
            obj.setAggKey(aggKey);
            return this;
        }

        public Builder aggValue(String aggValue) {
            obj.setAggValue(aggValue);
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

        public Builder count(Long count) {
            obj.setCount(count);
            return this;
        }

        public Builder targetTag(String targetTag) {
            obj.setTargetTag(targetTag);
            return this;
        }

        public ReportSnapshot build() {
            return this.obj;
        }
    }

    public enum Column {
        id("id", "id", "BIGINT", false),
        targetType("target_type", "targetType", "VARCHAR", false),
        aggKey("agg_key", "aggKey", "VARCHAR", false),
        aggValue("agg_value", "aggValue", "VARCHAR", false),
        createTime("create_time", "createTime", "TIMESTAMP", false),
        datachangeLasttime("DataChange_LastTime", "datachangeLasttime", "TIMESTAMP", false),
        count("count", "count", "BIGINT", true),
        targetTag("target_tag", "targetTag", "VARCHAR", false);

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