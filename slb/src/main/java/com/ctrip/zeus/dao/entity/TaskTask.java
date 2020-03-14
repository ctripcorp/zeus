package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class TaskTask {
    private Long id;

    private String opsType;

    private Long groupId;

    private Long policyId;

    private Long slbId;

    private Long slbVirtualServerId;

    private String ipList;

    private Boolean up;

    private String status;

    private Long targetSlbId;

    private String resources;

    private Integer version;

    private Boolean skipValidate;

    private String failCause;

    private Date createTime;

    private Date datachangeLasttime;

    private Long drId;

    private byte[] taskList;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOpsType() {
        return opsType;
    }

    public void setOpsType(String opsType) {
        this.opsType = opsType == null ? null : opsType.trim();
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getPolicyId() {
        return policyId;
    }

    public void setPolicyId(Long policyId) {
        this.policyId = policyId;
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

    public String getIpList() {
        return ipList;
    }

    public void setIpList(String ipList) {
        this.ipList = ipList == null ? null : ipList.trim();
    }

    public Boolean getUp() {
        return up;
    }

    public void setUp(Boolean up) {
        this.up = up;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public Long getTargetSlbId() {
        return targetSlbId;
    }

    public void setTargetSlbId(Long targetSlbId) {
        this.targetSlbId = targetSlbId;
    }

    public String getResources() {
        return resources;
    }

    public void setResources(String resources) {
        this.resources = resources == null ? null : resources.trim();
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean getSkipValidate() {
        return skipValidate;
    }

    public void setSkipValidate(Boolean skipValidate) {
        this.skipValidate = skipValidate;
    }

    public String getFailCause() {
        return failCause;
    }

    public void setFailCause(String failCause) {
        this.failCause = failCause == null ? null : failCause.trim();
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

    public Long getDrId() {
        return drId;
    }

    public void setDrId(Long drId) {
        this.drId = drId;
    }

    public byte[] getTaskList() {
        return taskList;
    }

    public void setTaskList(byte[] taskList) {
        this.taskList = taskList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", opsType=").append(opsType);
        sb.append(", groupId=").append(groupId);
        sb.append(", policyId=").append(policyId);
        sb.append(", slbId=").append(slbId);
        sb.append(", slbVirtualServerId=").append(slbVirtualServerId);
        sb.append(", ipList=").append(ipList);
        sb.append(", up=").append(up);
        sb.append(", status=").append(status);
        sb.append(", targetSlbId=").append(targetSlbId);
        sb.append(", resources=").append(resources);
        sb.append(", version=").append(version);
        sb.append(", skipValidate=").append(skipValidate);
        sb.append(", failCause=").append(failCause);
        sb.append(", createTime=").append(createTime);
        sb.append(", datachangeLasttime=").append(datachangeLasttime);
        sb.append(", drId=").append(drId);
        sb.append(", taskList=").append(taskList);
        sb.append("]");
        return sb.toString();
    }

    public static TaskTask.Builder builder() {
        return new TaskTask.Builder();
    }

    public static class Builder {
        private TaskTask obj;

        public Builder() {
            this.obj = new TaskTask();
        }

        public Builder id(Long id) {
            obj.setId(id);
            return this;
        }

        public Builder opsType(String opsType) {
            obj.setOpsType(opsType);
            return this;
        }

        public Builder groupId(Long groupId) {
            obj.setGroupId(groupId);
            return this;
        }

        public Builder policyId(Long policyId) {
            obj.setPolicyId(policyId);
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

        public Builder ipList(String ipList) {
            obj.setIpList(ipList);
            return this;
        }

        public Builder up(Boolean up) {
            obj.setUp(up);
            return this;
        }

        public Builder status(String status) {
            obj.setStatus(status);
            return this;
        }

        public Builder targetSlbId(Long targetSlbId) {
            obj.setTargetSlbId(targetSlbId);
            return this;
        }

        public Builder resources(String resources) {
            obj.setResources(resources);
            return this;
        }

        public Builder version(Integer version) {
            obj.setVersion(version);
            return this;
        }

        public Builder skipValidate(Boolean skipValidate) {
            obj.setSkipValidate(skipValidate);
            return this;
        }

        public Builder failCause(String failCause) {
            obj.setFailCause(failCause);
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

        public Builder drId(Long drId) {
            obj.setDrId(drId);
            return this;
        }

        public Builder taskList(byte[] taskList) {
            obj.setTaskList(taskList);
            return this;
        }

        public TaskTask build() {
            return this.obj;
        }
    }

    public enum Column {
        id("id", "id", "BIGINT", false),
        opsType("ops_type", "opsType", "VARCHAR", false),
        groupId("group_id", "groupId", "BIGINT", false),
        policyId("policy_id", "policyId", "BIGINT", false),
        slbId("slb_id", "slbId", "BIGINT", false),
        slbVirtualServerId("slb_virtual_server_id", "slbVirtualServerId", "BIGINT", false),
        ipList("ip_list", "ipList", "VARCHAR", false),
        up("up", "up", "BIT", false),
        status("status", "status", "VARCHAR", true),
        targetSlbId("target_slb_id", "targetSlbId", "BIGINT", false),
        resources("resources", "resources", "VARCHAR", false),
        version("version", "version", "INTEGER", false),
        skipValidate("skip_validate", "skipValidate", "BIT", false),
        failCause("fail_cause", "failCause", "VARCHAR", false),
        createTime("create_time", "createTime", "TIMESTAMP", false),
        datachangeLasttime("DataChange_LastTime", "datachangeLasttime", "TIMESTAMP", false),
        drId("dr_id", "drId", "BIGINT", false),
        taskList("task_list", "taskList", "LONGVARBINARY", false);

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