package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class AuthApprove {
    private Long id;

    private String applyBy;

    private Date applyTime;

    private String applyType;

    private String applyOps;

    private String applyTargets;

    private String approvedBy;

    private Date approvedTime;

    private Boolean approved;

    private Date datachangeLasttime;

    private byte[] context;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApplyBy() {
        return applyBy;
    }

    public void setApplyBy(String applyBy) {
        this.applyBy = applyBy == null ? null : applyBy.trim();
    }

    public Date getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(Date applyTime) {
        this.applyTime = applyTime;
    }

    public String getApplyType() {
        return applyType;
    }

    public void setApplyType(String applyType) {
        this.applyType = applyType == null ? null : applyType.trim();
    }

    public String getApplyOps() {
        return applyOps;
    }

    public void setApplyOps(String applyOps) {
        this.applyOps = applyOps == null ? null : applyOps.trim();
    }

    public String getApplyTargets() {
        return applyTargets;
    }

    public void setApplyTargets(String applyTargets) {
        this.applyTargets = applyTargets == null ? null : applyTargets.trim();
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy == null ? null : approvedBy.trim();
    }

    public Date getApprovedTime() {
        return approvedTime;
    }

    public void setApprovedTime(Date approvedTime) {
        this.approvedTime = approvedTime;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public Date getDatachangeLasttime() {
        return datachangeLasttime;
    }

    public void setDatachangeLasttime(Date datachangeLasttime) {
        this.datachangeLasttime = datachangeLasttime;
    }

    public byte[] getContext() {
        return context;
    }

    public void setContext(byte[] context) {
        this.context = context;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", applyBy=").append(applyBy);
        sb.append(", applyTime=").append(applyTime);
        sb.append(", applyType=").append(applyType);
        sb.append(", applyOps=").append(applyOps);
        sb.append(", applyTargets=").append(applyTargets);
        sb.append(", approvedBy=").append(approvedBy);
        sb.append(", approvedTime=").append(approvedTime);
        sb.append(", approved=").append(approved);
        sb.append(", datachangeLasttime=").append(datachangeLasttime);
        sb.append(", context=").append(context);
        sb.append("]");
        return sb.toString();
    }

    public static AuthApprove.Builder builder() {
        return new AuthApprove.Builder();
    }

    public static class Builder {
        private AuthApprove obj;

        public Builder() {
            this.obj = new AuthApprove();
        }

        public Builder id(Long id) {
            obj.setId(id);
            return this;
        }

        public Builder applyBy(String applyBy) {
            obj.setApplyBy(applyBy);
            return this;
        }

        public Builder applyTime(Date applyTime) {
            obj.setApplyTime(applyTime);
            return this;
        }

        public Builder applyType(String applyType) {
            obj.setApplyType(applyType);
            return this;
        }

        public Builder applyOps(String applyOps) {
            obj.setApplyOps(applyOps);
            return this;
        }

        public Builder applyTargets(String applyTargets) {
            obj.setApplyTargets(applyTargets);
            return this;
        }

        public Builder approved(Boolean approved) {
            obj.setApproved(approved);
            return this;
        }

        public Builder approvedBy(String approvedBy) {
            obj.setApprovedBy(approvedBy);
            return this;
        }

        public Builder approvedTime(Date approvedTime) {
            obj.setApprovedTime(approvedTime);
            return this;
        }

        public Builder datachangeLasttime(Date datachangeLasttime) {
            obj.setDatachangeLasttime(datachangeLasttime);
            return this;
        }

        public Builder context(byte[] context) {
            obj.setContext(context);
            return this;
        }

        public AuthApprove build() {
            return this.obj;
        }
    }

    public enum Column {
        id("id", "id", "BIGINT", false),
        applyBy("apply_by", "applyBy", "VARCHAR", false),
        applyTime("apply_time", "applyTime", "TIMESTAMP", false),
        applyType("apply_type", "applyType", "VARCHAR", false),
        applyOps("apply_ops", "applyOps", "VARCHAR", false),
        applyTargets("apply_targets", "applyTargets", "VARCHAR", false),
        approvedBy("approved_by", "approvedBy", "VARCHAR", false),
        approvedTime("approved_time", "approvedTime", "TIMESTAMP", false),
        approved("approved", "approved", "BIT", false),
        datachangeLasttime("DataChange_LastTime", "datachangeLasttime", "TIMESTAMP", false),
        context("context", "context", "LONGVARBINARY", false);

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