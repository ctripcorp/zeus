package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class LogOperationLog {
    private Long id;

    private String type;

    private String targetId;

    private String operation;

    private String data;

    private String userName;

    private String clientIp;

    private Boolean success;

    private String errMsg;

    private Date datetime;

    private Date datachangeLasttime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId == null ? null : targetId.trim();
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation == null ? null : operation.trim();
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data == null ? null : data.trim();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName == null ? null : userName.trim();
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp == null ? null : clientIp.trim();
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg == null ? null : errMsg.trim();
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
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
        sb.append(", type=").append(type);
        sb.append(", targetId=").append(targetId);
        sb.append(", operation=").append(operation);
        sb.append(", data=").append(data);
        sb.append(", userName=").append(userName);
        sb.append(", clientIp=").append(clientIp);
        sb.append(", success=").append(success);
        sb.append(", errMsg=").append(errMsg);
        sb.append(", datetime=").append(datetime);
        sb.append(", datachangeLasttime=").append(datachangeLasttime);
        sb.append("]");
        return sb.toString();
    }

    public static LogOperationLog.Builder builder() {
        return new LogOperationLog.Builder();
    }

    public static class Builder {
        private LogOperationLog obj;

        public Builder() {
            this.obj = new LogOperationLog();
        }

        public Builder id(Long id) {
            obj.setId(id);
            return this;
        }

        public Builder type(String type) {
            obj.setType(type);
            return this;
        }

        public Builder targetId(String targetId) {
            obj.setTargetId(targetId);
            return this;
        }

        public Builder operation(String operation) {
            obj.setOperation(operation);
            return this;
        }

        public Builder data(String data) {
            obj.setData(data);
            return this;
        }

        public Builder datachangeLasttime(Date datachangeLasttime) {
            obj.setDatachangeLasttime(datachangeLasttime);
            return this;
        }

        public Builder userName(String userName) {
            obj.setUserName(userName);
            return this;
        }

        public Builder clientIp(String clientIp) {
            obj.setClientIp(clientIp);
            return this;
        }

        public Builder success(Boolean success) {
            obj.setSuccess(success);
            return this;
        }

        public Builder errMsg(String errMsg) {
            obj.setErrMsg(errMsg);
            return this;
        }

        public Builder datetime(Date datetime) {
            obj.setDatetime(datetime);
            return this;
        }

        public LogOperationLog build() {
            return this.obj;
        }
    }

    public enum Column {
        id("id", "id", "BIGINT", false),
        type("type", "type", "VARCHAR", true),
        targetId("target_id", "targetId", "VARCHAR", false),
        operation("operation", "operation", "VARCHAR", true),
        data("data", "data", "VARCHAR", true),
        userName("user_name", "userName", "VARCHAR", false),
        clientIp("client_ip", "clientIp", "VARCHAR", false),
        success("success", "success", "BIT", false),
        errMsg("err_msg", "errMsg", "VARCHAR", false),
        datetime("datetime", "datetime", "TIMESTAMP", true),
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