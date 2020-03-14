package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class SlbBuildTicket {
    private Long id;

    private Long slbId;

    private Integer pendingTicket;

    private Integer currentTicket;

    private Date createdTime;

    private Date datachangeLasttime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSlbId() {
        return slbId;
    }

    public void setSlbId(Long slbId) {
        this.slbId = slbId;
    }

    public Integer getPendingTicket() {
        return pendingTicket;
    }

    public void setPendingTicket(Integer pendingTicket) {
        this.pendingTicket = pendingTicket;
    }

    public Integer getCurrentTicket() {
        return currentTicket;
    }

    public void setCurrentTicket(Integer currentTicket) {
        this.currentTicket = currentTicket;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", slbId=").append(slbId);
        sb.append(", pendingTicket=").append(pendingTicket);
        sb.append(", currentTicket=").append(currentTicket);
        sb.append(", createdTime=").append(createdTime);
        sb.append(", datachangeLasttime=").append(datachangeLasttime);
        sb.append("]");
        return sb.toString();
    }

    public static SlbBuildTicket.Builder builder() {
        return new SlbBuildTicket.Builder();
    }

    public static class Builder {
        private SlbBuildTicket obj;

        public Builder() {
            this.obj = new SlbBuildTicket();
        }

        public Builder id(Long id) {
            obj.setId(id);
            return this;
        }

        public Builder slbId(Long slbId) {
            obj.setSlbId(slbId);
            return this;
        }

        public Builder pendingTicket(Integer pendingTicket) {
            obj.setPendingTicket(pendingTicket);
            return this;
        }

        public Builder currentTicket(Integer currentTicket) {
            obj.setCurrentTicket(currentTicket);
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

        public SlbBuildTicket build() {
            return this.obj;
        }
    }

    public enum Column {
        id("id", "id", "BIGINT", false),
        slbId("slb_id", "slbId", "BIGINT", false),
        pendingTicket("pending_ticket", "pendingTicket", "INTEGER", false),
        currentTicket("current_ticket", "currentTicket", "INTEGER", false),
        createdTime("created_time", "createdTime", "TIMESTAMP", false),
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